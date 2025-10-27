package com.daruda.darudaserver.global.auth.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) {
		String path = request.getRequestURI();
		String method = request.getMethod();

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			log.warn("접근 거부: Method: {}, Path: {}, principal: {}, authenticated: {}, authorities: {}, Message: {}",
				method, path, auth.getPrincipal(), auth.isAuthenticated(), auth.getAuthorities(),
				accessDeniedException.getMessage());
		} else {
			log.warn("접근 거부: Method: {}, Path: {}, 인증정보 없음, Message: {}", method, path,
				accessDeniedException.getMessage());
		}

		setResponse(response);
	}

	private void setResponse(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	}
}
