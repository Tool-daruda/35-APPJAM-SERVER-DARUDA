package com.daruda.darudaserver.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry corsRegistry) {
		corsRegistry.addMapping("/**")
			.allowedOriginPatterns("http://localhost:5173", "https://daruda.shop", "http://localhost:8080",
				"https://kauth.kakao.com", "https://www.daruda.site", "https://daruda.shop/api/v1/users/token/**")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
			.allowedHeaders("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "Location",
				"status", "code")
			.allowCredentials(true);
	}
}
