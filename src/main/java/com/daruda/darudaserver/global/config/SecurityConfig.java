package com.daruda.darudaserver.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.security.ExceptionHandlerFilter;
import com.daruda.darudaserver.global.auth.security.JwtAuthenticationEntryPoint;
import com.daruda.darudaserver.global.auth.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtTokenProvider jwtTokenProvider;

	private static final String[] WHITE_LIST = {
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/api/v1/user/nickname",
		"/api/v1/comment/{board-id}",
		"/api/v1/auth/sign-up",
		"/api/v1/auth/login",
		"/api/v1/users/kakao/login-url",
		"api/v1/tool",
		"/api/v1/tool/{tool-id}",
		"/api/v1/tool/{tool-id}/plans",
		"/api/v1/tool/{tool-id}/core-features",
		"/api/v1/tool/{tool-id}/alternatives",
		"/api/v1/tool/category",
		"/api/v1/board",
		"/api/v1/board/{board-id}"
	};

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
					.authenticationEntryPoint(jwtAuthenticationEntryPoint))
			.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
				authorizationManagerRequestMatcherRegistry
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() //OPTION 추가
					.anyRequest()
					.authenticated())
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new ExceptionHandlerFilter(), JwtAuthenticationFilter.class);

		http.cors(cors -> cors.configurationSource(CorsConfig.configurationSource()));

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(WHITE_LIST);
	}
}
