package com.daruda.darudaserver.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.security.CustomAccessDeniedHandler;
import com.daruda.darudaserver.global.auth.security.ExceptionHandlerFilter;
import com.daruda.darudaserver.global.auth.security.JwtAuthenticationEntryPoint;
import com.daruda.darudaserver.global.auth.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
	private static final String[] WHITE_LIST = {
		// Swagger & Docs
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/swagger-resources/**",
		"/webjars/**",

		// Auth
		"/api/v1/auth/login-url",
		"/api/v1/auth/login",
		"/api/v1/auth/sign-up",
		"/api/v1/auth/reissue",

		// Tool (Public 정보)
		"/api/v1/tool/category",
		"/api/v1/tool/{tool-id}/core-features",
		"/api/v1/tool/{tool-id}/plans",
		"/api/v1/tool/{tool-id}/alternatives",
		"/api/v1/tool/{tool-id}/blogs",

		// Search
		"/api/v1/search/**",

		// User (Public 정보)
		"/api/v1/user/nickname",
		"/api/v1/user/boards",
		"/api/v1/user/scrap-boards",

		// Board & Comment (조회는 기본적으로 허용)
		"/api/v1/board",
		"/api/v1/board/{board-id}",
		"/api/v1/comment",

		// System
		"/error",
		"/favicon.ico",
		"/"
	};

	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtTokenProvider jwtTokenProvider;
	private final CorsConfig corsConfig;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(sessionManagementConfigurer ->
				sessionManagementConfigurer
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exceptionHandlingConfigurer ->
				exceptionHandlingConfigurer
					.authenticationEntryPoint(jwtAuthenticationEntryPoint)
					.accessDeniedHandler(customAccessDeniedHandler))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**")
				.permitAll()

				// 관리자 API
				.requestMatchers("/api/v1/admin/**")
				.hasRole(Positions.ADMIN.getEngName())

				// 인증이 필수인 상태 변경/개인화 API (POST, PATCH, DELETE 등)
				.requestMatchers(HttpMethod.POST, "/api/v1/comment/**", "/api/v1/board/**", "/api/v1/notification/**",
					"/api/v1/tool/*/scrap", "/api/v1/reports")
				.authenticated()
				.requestMatchers(HttpMethod.PATCH, "/api/v1/notification/**", "/api/v1/user/**", "/api/v1/board/**")
				.authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/comment/**", "/api/v1/board/**", "/api/v1/auth/withdraw")
				.authenticated()
				.requestMatchers("/api/v1/auth/logout", "/api/v1/user/profile", "/api/v1/user/scrap-tools",
					"/api/v1/notification/connect")
				.authenticated()

				// 나머지는 WHITE_LIST 허용
				.requestMatchers(WHITE_LIST)
				.permitAll()

				// 그 외 정의되지 않은 모든 요청은 무시
				.anyRequest()
				.denyAll())
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new ExceptionHandlerFilter(), JwtAuthenticationFilter.class);

		http.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()));

		return http.build();
	}
}
