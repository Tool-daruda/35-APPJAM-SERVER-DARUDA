package com.daruda.darudaserver.domain.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.daruda.darudaserver.domain.user.dto.request.LoginRequest;
import com.daruda.darudaserver.domain.user.dto.request.SignUpRequest;
import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.LoginSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.SignUpSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.UserInformationResponse;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.domain.user.service.AuthService;
import com.daruda.darudaserver.domain.user.service.SocialService;
import com.daruda.darudaserver.global.auth.cookie.CookieProvider;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.jwt.service.TokenService;
import com.daruda.darudaserver.global.auth.security.JwtAuthenticationFilter;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private AuthService authService;

	@Mock
	private TokenService tokenService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private CookieProvider cookieProvider;

	@InjectMocks
	private AuthController authController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(authController)
			.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
			.addFilters(new JwtAuthenticationFilter(jwtTokenProvider))
			.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("소셜 로그인 URL 반환 성공")
	void requestLoginUrl() throws Exception {
		// given
		String redirectUrl = "https://example.com/login";
		SocialType socialType = SocialType.KAKAO;
		SocialService socialService = mock(SocialService.class);

		// when
		when(authService.findSocialService(socialType)).thenReturn(socialService);
		when(socialService.getLoginUrl()).thenReturn(redirectUrl);

		// then
		mockMvc.perform(get("/api/v1/auth/login-url")
				.param("socialType", socialType.name()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value(redirectUrl))
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_REDIRECT.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_REDIRECT.getMessage()));

		// verify
		verify(authService).findSocialService(socialType);
		verify(socialService).getLoginUrl();
	}

	@Test
	@DisplayName("소셜 로그인 성공")
	void login() throws Exception {
		// given
		Long userId = 1L;
		String code = "test.code";
		String nickname = "testUser";
		String email = "test@example.com";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", userId);

		LoginRequest loginRequest = new LoginRequest(SocialType.KAKAO);
		UserInformationResponse userInformationResponse = UserInformationResponse.of(userId, email, nickname);
		JwtTokenResponse jwtTokenResponse = JwtTokenResponse.of("accessToken", "refreshToken");
		LoginSuccessResponse loginSuccessResponse = LoginSuccessResponse.ofRegisteredUser(jwtTokenResponse, userEntity);
		SocialService socialService = mock(SocialService.class);

		// when
		when(authService.findSocialService(loginRequest.socialType())).thenReturn(socialService);
		when(socialService.getInfo(code)).thenReturn(userInformationResponse);
		when(authService.login(userInformationResponse)).thenReturn(loginSuccessResponse);
		doNothing().when(cookieProvider).setTokenCookies(any(), anyString(), anyString());
		// then
		mockMvc.perform(post("/api/v1/auth/login")
				.param("code", code)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userId").value(userId))
			.andExpect(jsonPath("$.data.email").doesNotExist())
			.andExpect(jsonPath("$.data.isUser").value(true))
			.andExpect(jsonPath("$.data.positions").value(positions.toString()))
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_LOGIN.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_LOGIN.getMessage()));

		// verify
		verify(authService).findSocialService(loginRequest.socialType());
		verify(socialService).getInfo(code);
		verify(authService).login(userInformationResponse);
		verify(cookieProvider).setTokenCookies(any(), eq("accessToken"), eq("refreshToken"));
	}

	@Test
	@DisplayName("회원 가입 성공")
	void register() throws Exception {
		// given
		Long userId = 1L;
		String nickname = "tester";
		String email = "test@example.com";
		Positions positions = Positions.STUDENT;
		JwtTokenResponse jwtTokenResponse = new JwtTokenResponse("accessToken", "refreshToken");
		SignUpRequest signUpRequest = new SignUpRequest(nickname, positions.getName(), email);
		SignUpSuccessResponse mockResponse = SignUpSuccessResponse.of(userId, nickname, positions, email,
			jwtTokenResponse);

		// when
		when(authService.register(signUpRequest.email(), signUpRequest.nickname(), signUpRequest.positions()))
			.thenReturn(mockResponse);
		doNothing().when(cookieProvider).setTokenCookies(any(), anyString(), anyString());
		// then
		mockMvc.perform(post("/api/v1/auth/sign-up")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userId").value(userId))
			.andExpect(jsonPath("$.data.nickname").value(nickname))
			.andExpect(jsonPath("$.data.email").value(email))
			.andExpect(jsonPath("$.data.positions").value(positions.toString()))
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_CREATE.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_CREATE.getMessage()));

		// verify
		verify(authService).register(signUpRequest.email(), signUpRequest.nickname(), signUpRequest.positions());
		verify(cookieProvider).setTokenCookies(any(), eq("accessToken"), eq("refreshToken"));
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logout() throws Exception {
		// given
		Long userId = 1L;
		Authentication authentication = UserAuthentication.createUserAuthentication(userId);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		// when
		doReturn(userId).when(authService).logout(userId);

		// then
		mockMvc.perform(post("/api/v1/auth/logout"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value(userId))
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_LOGOUT.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_LOGOUT.getMessage()));

		verify(authService).logout(userId);
	}

	@Test
	@DisplayName("Access Token 재발급 성공")
	void reissueToken() throws Exception {
		JwtTokenResponse jwtTokenResponse = new JwtTokenResponse("newAccessToken", "newRefreshToken");

		// when
		when(tokenService.reissueToken(any(HttpServletRequest.class))).thenReturn(jwtTokenResponse);
		doNothing().when(cookieProvider).setTokenCookies(any(), anyString(), anyString());

		// then
		mockMvc.perform(post("/api/v1/auth/reissue")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_REISSUE.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_REISSUE.getMessage()));

		// verify
		verify(tokenService).reissueToken(any(HttpServletRequest.class));
		verify(cookieProvider).setTokenCookies(any(), eq("newAccessToken"), eq("newRefreshToken"));
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	void withdraw() throws Exception {
		// given
		Long userId = 1L;
		Authentication authentication = UserAuthentication.createUserAuthentication(userId);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		// when
		doNothing().when(authService).withdraw(userId);

		// then
		mockMvc.perform(delete("/api/v1/auth/withdraw"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_WITHDRAW.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_WITHDRAW.getMessage()));

		verify(authService).withdraw(userId);
	}
}
