package com.daruda.darudaserver.global.auth.security;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
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
    private static final List<String> EXCLUDE_URL = Arrays.asList("/api/v1/users/signin",
            "/api/v1/users/token",
            "/api/v1/users/signup",
            "/api/v1/users/nickname",
            "/api/v1/users/kakao/login-url",
            "/api/v1/tools/category"
//            "/api/v1/tools/**"
//            "/api/v1/boards/board/**"
    );

    @Override
    protected void doFilterInternal( HttpServletRequest request,  HttpServletResponse response,  FilterChain filterChain) throws ServletException, IOException {
        log.debug("JwtAuthenticationFilter 시작: 요청 URL - {}", request.getRequestURI());
        try {
            final String accessToken = getAccessToken(request);
            log.debug("추출된 AccessToken: {}", accessToken);

            if ( StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken) == JwtValidationType.VALID_JWT) {
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
    protected boolean shouldNotFilter( HttpServletRequest request) throws ServletException {

        String path = request.getServletPath();
        String method = request.getMethod();
//        // GET 요청만 인증 우회
//        if (path.startsWith("/api/v1/boards/board/**") && method.equals("GET")) {
//            return true;
//        }
        return EXCLUDE_URL.stream().anyMatch(exclude -> new AntPathMatcher().match(exclude, path));
    }

    private String getAccessToken( HttpServletRequest request){
        try {
            String accessToken = request.getHeader("Authorization");
            if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer")) {
                return accessToken.substring("Bearer".length());
            }
            throw new UnauthorizedException(ErrorCode.EMPTY_OR_INVALID_TOKEN);
        }catch(Exception e){
            log.error("AccessToken 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    private void doAuthentication( HttpServletRequest request, final  Long userId) {
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


    private void createAndSetWebAuthenticationDetails(HttpServletRequest request, UserAuthentication authentication){
        WebAuthenticationDetailsSource webAuthenticationDetailsSource = new WebAuthenticationDetailsSource();
        WebAuthenticationDetails webAuthenticationDetails = webAuthenticationDetailsSource.buildDetails(request);
        authentication.setDetails(webAuthenticationDetails);
    }
}
