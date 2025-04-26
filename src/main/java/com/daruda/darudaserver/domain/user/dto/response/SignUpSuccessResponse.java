package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

public record SignUpSuccessResponse(
	String nickname,
	Positions positions,
	String email,
	JwtTokenResponse jwtTokenResponse
) {
	public static SignUpSuccessResponse of(String nickname, Positions positions, String email,
		JwtTokenResponse jwtTokenResponse) {
		return new SignUpSuccessResponse(nickname, positions, email, jwtTokenResponse);
	}
}
