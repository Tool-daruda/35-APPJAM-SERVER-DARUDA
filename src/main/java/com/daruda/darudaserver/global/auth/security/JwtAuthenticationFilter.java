package com.daruda.darudaserver.global.auth.security;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("JwtAuthenticationFilter 시작: 요청 URL - {}", request.getRequestURI());
        try {
            final String accessToken = getAccessToken(request);
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken) == JwtValidationType.VALID_JWT) {
                Long userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
                doAuthentication(request, userId);
                log.info("JWT 인증 성공 - 사용자 ID: {}", userId);
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
        }
        log.debug("JwtAuthenticationFilter 종료: 요청 URL - {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }


    private static final List<String> EXCLUDE_URL = Arrays.asList("/api/users/**", "/api/v1/tools/**");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String path = request.getServletPath();
        boolean isExcluded = EXCLUDE_URL.stream().anyMatch(exclude -> new AntPathMatcher().match(exclude, path));
        log.debug("Checking shouldNotFilter for path: {}, excluded: {}", path, isExcluded);
        return isExcluded;
    }



    private String getAccessToken(HttpServletRequest request){
        String accessToken = request.getHeader("Authorization");
        if(StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer")){
            return accessToken.substring("Bearer".length());
        }
        throw new RuntimeException("유효하지 않은 토큰입니다");
    }

    private void doAuthentication(HttpServletRequest request, Long userId){
        UserAuthentication authentication = UserAuthentication.createUserAuthentication(userId);
        createAndSetWebAuthenticationDetails(request, authentication);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

    }

    private void createAndSetWebAuthenticationDetails(HttpServletRequest request, UserAuthentication authentication){
        WebAuthenticationDetailsSource webAuthenticationDetailsSource = new WebAuthenticationDetailsSource();
        WebAuthenticationDetails webAuthenticationDetails = webAuthenticationDetailsSource.buildDetails(request);
        authentication.setDetails(webAuthenticationDetails);
    }
}
