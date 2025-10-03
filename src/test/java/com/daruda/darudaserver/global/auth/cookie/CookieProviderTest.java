package com.daruda.darudaserver.global.auth.cookie;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class CookieProviderTest {

	@Mock
	private HttpServletResponse response;

	@InjectMocks
	private CookieProvider cookieProvider;

	@Test
	@DisplayName("토큰 쿠키를 성공적으로 설정한다")
	void setTokenCookies() {
		// given
		String accessToken = "testAccessToken";
		String refreshToken = "testRefreshToken";

		// when
		cookieProvider.setTokenCookies(response, accessToken, refreshToken);

		// then
		verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
	}
}
