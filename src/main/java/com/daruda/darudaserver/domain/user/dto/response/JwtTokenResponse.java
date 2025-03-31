package com.daruda.darudaserver.domain.user.dto.response;

import lombok.Builder;

@Builder
public record JwtTokenResponse(
	String accessToken,
	String refreshToken
) {
	public static JwtTokenResponse of(String accessToken, String refreshToken) {
		return JwtTokenResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}
}
