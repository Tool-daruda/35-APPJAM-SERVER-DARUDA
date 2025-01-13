package com.daruda.darudaserver.global.auth.security;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final List<String> EXCLUDE_URL = Arrays.asList("/api/v1/users/signin", "/api/v1/users/token","/api/v1/users/reissue","/api/v1/users/signup","/api/v1/users/nickname","/api/v1/tools/**");

    @Override
    protected void doFilterInternal( HttpServletRequest request,  HttpServletResponse response,  FilterChain filterChain) throws ServletException, IOException {
        log.debug("JwtAuthenticationFilter 시작: 요청 URL - {}", request.getRequestURI());
        try {
            final String accessToken = getAccessToken(request);
            log.debug("추출된 AccessToken: {}", accessToken);

            if ( jwtTokenProvider.validateToken(accessToken) == JwtValidationType.VALID_JWT) {
                log.debug("AccessToken 유효성 검사 성공");

                Long userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
                log.debug("JWT에서 추출된 UserID: {}", userId);

                doAuthentication(request, userId);
                log.info("JWT 인증 성공 - 사용자 ID: {}", userId);
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage(), e);
        }
        log.debug("JwtAuthenticationFilter 종료: 요청 URL - {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter( HttpServletRequest request) throws ServletException {

        String path = request.getServletPath();
        return EXCLUDE_URL.stream().anyMatch(exclude -> new AntPathMatcher().match(exclude, path));
    }

    private String getAccessToken( HttpServletRequest request){
        String accessToken = request.getHeader("Authorization");
        if(StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer")){
            return accessToken.substring("Bearer".length());
        }
        throw new RuntimeException("유효하지 않은 토큰입니다");
    }

    private void doAuthentication( HttpServletRequest request, final  Long userId) {
        if (userId == null) {
            throw new BadRequestException(ErrorCode.REFRESH_TOKEN_USER_ID_MISMATCH_ERROR);
        }
        log.debug("SecurityContextHolder : "+userId);
        UserAuthentication authentication = UserAuthentication.createUserAuthentication(userId);
        createAndSetWebAuthenticationDetails(request, authentication);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        log.debug("SecurityContext에 인증 정보 설정 완료 - UserID: {}", userId);
    }


    private void createAndSetWebAuthenticationDetails(HttpServletRequest request, UserAuthentication authentication){
        WebAuthenticationDetailsSource webAuthenticationDetailsSource = new WebAuthenticationDetailsSource();
        WebAuthenticationDetails webAuthenticationDetails = webAuthenticationDetailsSource.buildDetails(request);
        authentication.setDetails(webAuthenticationDetails);
    }
}
