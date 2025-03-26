package com.daruda.darudaserver.global.config;

import java.util.ArrayList;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CorsConfig {
	public static CorsConfigurationSource configurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		ArrayList<String> allowedOriginPatters = new ArrayList<>();
		allowedOriginPatters.add("http://localhost:5173");
		allowedOriginPatters.add("https://daruda.shop");
		allowedOriginPatters.add("https://daruda.shop/api/v1/users/token/**");
		allowedOriginPatters.add("http://localhost:8080");
		allowedOriginPatters.add("https://www.daruda.site");
		configuration.setAllowedOrigins(allowedOriginPatters);

		ArrayList<String> allowedHeaders = new ArrayList<>();
		allowedHeaders.add("Authorization");
		allowedHeaders.add("Content-Type");
		allowedHeaders.add("Accept");
		allowedHeaders.add("Origin");
		allowedHeaders.add("X-Requested-With");
		configuration.setAllowedHeaders(allowedHeaders);

		ArrayList<String> allowedHttpMethods = new ArrayList<>();
		allowedHttpMethods.add("GET");
		allowedHttpMethods.add("POST");
		allowedHttpMethods.add("PUT");
		allowedHttpMethods.add("DELETE");
		allowedHttpMethods.add("PATCH");
		allowedHttpMethods.add("OPTIONS");
		configuration.setAllowedMethods(allowedHttpMethods);

		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

}
