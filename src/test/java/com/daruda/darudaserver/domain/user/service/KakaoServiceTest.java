package com.daruda.darudaserver.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.daruda.darudaserver.domain.user.dto.response.UserInformationResponse;
import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoAccount;
import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoUserDto;
import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoUserProfile;
import com.daruda.darudaserver.global.auth.client.KakaoAPiFeignClient;
import com.daruda.darudaserver.global.auth.client.KakaoFeignClient;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class KakaoServiceTest {

	@Mock
	private KakaoFeignClient kakaoFeignClient;

	@Mock
	private KakaoAPiFeignClient kakaoAPiFeignClient;

	@InjectMocks
	private KakaoService kakaoService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(kakaoService, "clientId", "test-client-id");
		ReflectionTestUtils.setField(kakaoService, "redirectUri", "http://localhost:8080/callback");
	}

	@Test
	@DisplayName("로그인 URL 생성 성공")
	void getLoginUrl_ShouldReturnLoginUrl() {
		// given
		String clientId = "test-client-id";
		String redirectUri = "http://localhost:8080/callback";

		// when
		String loginUrl = kakaoService.getLoginUrl();

		// then
		assertThat(loginUrl).isEqualTo(
			"https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri="
				+ redirectUri
		);
	}

	@Test
	@DisplayName("사용자 정보 가져오기 성공")
	void getInfo_ShouldReturnUserInfo() {
		// given
		String code = "1234";
		String nickname = "tester";
		String email = "test@test";
		KakaoTokenResponse tokenResponse = new KakaoTokenResponse("test", "accessToken", "refreshToken");
		KakaoUserProfile userProfile = new KakaoUserProfile(nickname);
		KakaoAccount kakaoAccount = new KakaoAccount(userProfile, email);
		KakaoUserDto kakaoUserDto = new KakaoUserDto(12345L, kakaoAccount);

		when(kakaoAPiFeignClient.getAccessToken(anyString(), anyString(), anyString(), anyString(), anyString()))
			.thenReturn(tokenResponse);
		when(kakaoFeignClient.getUserInformation(anyString(), anyString()))
			.thenReturn(kakaoUserDto);

		// when
		UserInformationResponse userInformationResponse = kakaoService.getInfo(code);

		// then
		assertThat(userInformationResponse.id()).isEqualTo(12345L);
		assertThat(userInformationResponse.email()).isEqualTo(email);
		assertThat(userInformationResponse.nickname()).isEqualTo(nickname);
	}

	@Test
	@DisplayName("사용자 정보 가져오기 실패")
	void getInfo_ShouldThrowException_WhenErrorOccurs() {
		// given
		String clientId = "test-client-id";
		String redirectUri = "http://localhost:8080/callback";
		String code = "test-code";
		String accessToken = "test-access-token";
		KakaoTokenResponse tokenResponse = new KakaoTokenResponse("test", "accessToken", "refreshToken");

		when(kakaoAPiFeignClient.getAccessToken(
			eq("authorization_code"), eq(clientId), eq(redirectUri), eq(code), anyString()))
			.thenReturn(tokenResponse);
		when(kakaoFeignClient.getUserInformation(anyString(), anyString()))
			.thenThrow(new RuntimeException("API Error"));

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () -> kakaoService.getInfo(code));
		assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
	}
}
