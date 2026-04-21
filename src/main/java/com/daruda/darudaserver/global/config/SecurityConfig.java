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
		// Auth
		"/api/v1/auth/login-url",

		// Board
		"/api/v1/board",
		"/api/v1/board/{board-id}",

		// Comment
		"/api/v1/comment",

		// Search
		"/api/v1/search/board",
		"/api/v1/search/tool",

		// Tool
		"/api/v1/tool",
		"/api/v1/tool/category",
		"/api/v1/tool/{tool-id}",
		"/api/v1/tool/{tool-id}/core-features",
		"/api/v1/tool/{tool-id}/plans",
		"/api/v1/tool/{tool-id}/alternatives",
		"/api/v1/tool/{tool-id}/blogs",

		// User
		"/api/v1/user/nickname",

		// Swagger
		"/swagger-ui/**",
		"/v3/api-docs/**",

		// System
		"/error",
		"/favicon.ico"
	};

	private static final String[] AUTH_WHITE_LIST = {
		"/api/v1/auth/login",
		"/api/v1/auth/sign-up",
		"/api/v1/auth/reissue"
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

				// Admin
				.requestMatchers("/api/v1/admin/**")
				.hasRole(Positions.ADMIN.getEngName())

				// Auth
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout")
				.authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/auth/withdraw")
				.authenticated()

				// Board
				.requestMatchers(HttpMethod.POST, "/api/v1/board", "/api/v1/board/{board-id}/scrap")
				.authenticated()
				.requestMatchers(HttpMethod.PATCH, "/api/v1/board/{board-id}")
				.authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/board/{board-id}")
				.authenticated()

				// Comment
				.requestMatchers(HttpMethod.POST, "/api/v1/comment")
				.authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/comment/{comment-id}")
				.authenticated()

				// Image
				.requestMatchers(HttpMethod.GET, "/api/v1/image/presigned-url")
				.authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/image")
				.authenticated()

				// Notification
				.requestMatchers(HttpMethod.GET, "/api/v1/notification", "/api/v1/notification/connect",
					"/api/v1/notification/recent")
				.authenticated()
				.requestMatchers(HttpMethod.PATCH, "/api/v1/notification/read/{notification-id}")
				.authenticated()
				.requestMatchers(HttpMethod.POST, "/api/v1/notification/notice", "/api/v1/notification/block-notice")
				.authenticated()

				// Report
				.requestMatchers(HttpMethod.POST, "/api/v1/reports")
				.authenticated()
				.requestMatchers(HttpMethod.PATCH, "/api/v1/reports/{reportId}")
				.authenticated()

				// Tool
				.requestMatchers(HttpMethod.POST, "/api/v1/tool/{tool-id}/scrap")
				.authenticated()

				// User
				.requestMatchers(HttpMethod.GET, "/api/v1/user/scrap-tools", "/api/v1/user/boards",
					"/api/v1/user/profile", "/api/v1/user/scrap-boards")
				.authenticated()
				.requestMatchers(HttpMethod.PATCH, "/api/v1/user/profile")
				.authenticated()

				// White List
				.requestMatchers(HttpMethod.GET, WHITE_LIST)
				.permitAll()
				.requestMatchers(HttpMethod.POST, AUTH_WHITE_LIST)
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
