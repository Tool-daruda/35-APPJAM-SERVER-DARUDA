package com.daruda.darudaserver.global.auth.security;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.dto.ErrorResponse;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (UnauthorizedException e) {
			handleUnauthorizedException(response, e);
		} catch (Exception e) {
			handleException(response, e);
		}
	}

	public void handleUnauthorizedException(HttpServletResponse response, Exception ex) throws IOException {
		UnauthorizedException ue = (UnauthorizedException)ex;
		ErrorCode errorCode = ue.getErrorCode();
		HttpStatus httpStatus = errorCode.getHttpStatus();
		setResponse(response, httpStatus, errorCode);
		log.debug("handleUnauthorizedException" + ue.getMessage());
	}

	private void handleException(HttpServletResponse response, Exception ex) throws IOException {
		log.error(">>> Exception Handler Filter : ", ex);
		setResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR);
	}

	private void setResponse(HttpServletResponse response, HttpStatus httpStatus, ErrorCode errorCode) throws
		IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("utf-8");
		response.setStatus(httpStatus.value());
		PrintWriter writer = response.getWriter();
		writer.write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode)));
	}

}
