package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

public record SignUpSuccessResponse(
	Long userId,
	String nickname,
	Positions positions,
	String email,
	JwtTokenResponse jwtTokenResponse
) {
	public static SignUpSuccessResponse of(Long userId, String nickname, Positions positions, String email,
		JwtTokenResponse jwtTokenResponse) {
		return new SignUpSuccessResponse(userId, nickname, positions, email, jwtTokenResponse);
	}
}
