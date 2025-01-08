package com.daruda.darudaserver.global.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;



@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException){
        String path = request.getRequestURI();
        String method = request.getMethod();
        setResponse(response);
    }

    private void setResponse(HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
