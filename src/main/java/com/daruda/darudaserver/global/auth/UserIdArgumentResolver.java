package com.daruda.darudaserver.global.auth;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		boolean hasUserIdAnnotation = parameter.hasParameterAnnotation(UserId.class); //UserId 어노테이션을 파라미터가 가지고 있는지 확인
		boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType()); //파라미터 타입이 Long 인지 확인
		return hasUserIdAnnotation && isLongType;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		return SecurityContextHolder.getContext()
			.getAuthentication()
			.getPrincipal();
	}
}
