package com.daruda.darudaserver.global.auth.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			final String accessToken = getAccessToken(request);
			log.debug("추출된 AccessToken: {}", accessToken);

			if (StringUtils.hasText(accessToken)
				&& jwtTokenProvider.validateToken(accessToken) == JwtValidationType.VALID_JWT) {
				Long userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
				String role = jwtTokenProvider.getRoleFromJwt(accessToken);
				doAuthentication(request, userId, role);
			}
		} catch (Exception e) {
			log.error("JWT 인증 에러: {}", e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	private String getAccessToken(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}
		return Arrays.stream(request.getCookies())
			.filter(cookie -> "accessToken".equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElse(null);
	}

	private void doAuthentication(HttpServletRequest request, final Long userId, final String role) {
		if (userId == null) {
			throw new BadRequestException(ErrorCode.USER_NOT_FOUND);
		}
		log.debug("SecurityContextHolder : {}", userId);
		UserAuthentication authentication = UserAuthentication.createUserAuthentication(userId, role);
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
