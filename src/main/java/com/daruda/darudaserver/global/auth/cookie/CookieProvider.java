package com.daruda.darudaserver.global.auth.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieProvider {

	private static final String ACCESS_TOKEN = "accessToken";
	private static final String REFRESH_TOKEN = "refreshToken";

	private static final int ACCESS_TOKEN_MAX_AGE = 3 * 60 * 60;
	private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60;

	@Value("${cookie.domain}")
	private String cookieDomain;

	public void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
		ResponseCookie accessTokenCookie = createTokenCookie(ACCESS_TOKEN, accessToken, ACCESS_TOKEN_MAX_AGE);
		ResponseCookie refreshTokenCookie = createTokenCookie(REFRESH_TOKEN, refreshToken, REFRESH_TOKEN_MAX_AGE);

		response.addHeader("Set-Cookie", accessTokenCookie.toString());
		response.addHeader("Set-Cookie", refreshTokenCookie.toString());
	}

	public void deleteTokenCookies(HttpServletResponse response) {
		ResponseCookie accessTokenCookie = createTokenCookie(ACCESS_TOKEN, "", 0);
		ResponseCookie refreshTokenCookie = createTokenCookie(REFRESH_TOKEN, "", 0);

		response.addHeader("Set-Cookie", accessTokenCookie.toString());
		response.addHeader("Set-Cookie", refreshTokenCookie.toString());
	}

	private ResponseCookie createTokenCookie(String name, String value, int maxAge) {
		return ResponseCookie.from(name, value)
			.maxAge(maxAge)
			.path("/")
			.domain(cookieDomain)
			.secure(true)
			.sameSite("None")
			.httpOnly(true)
			.build();
	}
}
