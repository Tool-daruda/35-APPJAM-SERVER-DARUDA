package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

import lombok.Builder;

@Builder
public record SignUpResponse(
	Long userId,
	String nickname,
	Positions positions,
	String email
) {
	public static SignUpResponse from(SignUpSuccessResponse signUpSuccessResponse) {
		return SignUpResponse.builder()
			.userId(signUpSuccessResponse.userId())
			.nickname(signUpSuccessResponse.nickname())
			.positions(signUpSuccessResponse.positions())
			.email(signUpSuccessResponse.email())
			.build();
	}
}
