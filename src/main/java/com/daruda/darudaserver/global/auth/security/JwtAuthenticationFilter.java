package com.daruda.darudaserver.global.auth.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final List<String> EXCLUDE_URL = Arrays.asList(
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/api/v1/user/nickname",
		"/api/v1/comment",
		"/api/v1/auth/sign-up",
		"/api/v1/auth/login",
		"/api/v1/auth/login-url",
		"/api/v1/auth/reissue",
		"/api/v1/tool",
		"/api/v1/tool/{tool-id}/plans",
		"/api/v1/tool/{tool-id}/core-features",
		"/api/v1/tool/{tool-id}/alternatives",
		"/api/v1/tool/category",
		"/api/v1/board",
		"/api/v1/image/**",
		"/api/v1/board/{board-id}"
	);

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		log.debug("JwtAuthenticationFilter 시작: 요청 URL - {}", request.getRequestURI());
		try {
			final String accessToken = getAccessToken(request);
			log.debug("추출된 AccessToken: {}", accessToken);

			if (StringUtils.hasText(accessToken)
				&& jwtTokenProvider.validateToken(accessToken) == JwtValidationType.VALID_JWT) {
				Long userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
				doAuthentication(request, userId);
				log.info("JWT 인증 성공 - 사용자 ID: {}", userId);
			}
		} catch (Exception e) {
			log.error("JWT 인증 실패: {}", e.getMessage(), e);
			throw new UnauthorizedException(ErrorCode.EMPTY_OR_INVALID_TOKEN);
		}
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {

		String path = request.getServletPath();
		String method = request.getMethod();

		if (method.equals(HttpMethod.GET.name())) {
			return EXCLUDE_URL.stream().anyMatch(exclude -> new AntPathMatcher().match(exclude, path));
		}
		return false;
	}

	private String getAccessToken(HttpServletRequest request) {
		try {
			return
				Arrays.stream(request.getCookies())
					.filter(cookie -> "accessToken".equals(cookie.getName()))
					.map(Cookie::getValue)
					.findFirst()
					.orElseThrow(() -> new UnauthorizedException(ErrorCode.EMPTY_OR_INVALID_TOKEN));
		} catch (Exception e) {
			log.warn("AccessToken 추출 실패: {}", e.getMessage());
			return null;
		}
	}

	private void doAuthentication(HttpServletRequest request, final Long userId) {
		if (userId == null) {
			throw new BadRequestException(ErrorCode.USER_NOT_FOUND);
		}
		log.debug("SecurityContextHolder : {}", userId);
		UserAuthentication authentication = UserAuthentication.createUserAuthentication(userId);
		createAndSetWebAuthenticationDetails(request, authentication);

		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);
		log.debug("SecurityContext에 인증 정보 설정 완료 - UserID: {}", userId);
	}

	private void createAndSetWebAuthenticationDetails(HttpServletRequest request, UserAuthentication authentication) {
		WebAuthenticationDetailsSource webAuthenticationDetailsSource = new WebAuthenticationDetailsSource();
		WebAuthenticationDetails webAuthenticationDetails = webAuthenticationDetailsSource.buildDetails(request);
		authentication.setDetails(webAuthenticationDetails);
	}
}
