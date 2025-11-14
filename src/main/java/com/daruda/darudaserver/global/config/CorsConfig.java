package com.daruda.darudaserver.global.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CorsConfig {

	@Value("${admin.page.origin}")
	private static String adminPageOrigin;

	public static CorsConfigurationSource configurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		List<String> allowedOriginPatterns = List.of(
			"http://localhost:5173",
			"http://localhost:8080",
			"https://daruda.shop",
			"https://api.daruda.shop",
			"https://www.daruda.shop",
			"https://daruda.site",
			"https://api.daruda.site",
			"https://www.daruda.site",
			adminPageOrigin
		);
		configuration.setAllowedOriginPatterns(allowedOriginPatterns);

		configuration.setAllowedHeaders(List.of(
			"Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"
		));

		configuration.setAllowedMethods(List.of(
			"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
		));

		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
