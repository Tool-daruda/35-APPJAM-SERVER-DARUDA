package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

import lombok.Builder;

@Builder
public record SignUpResponse(
	String nickname,
	Positions positions,
	String email
) {
	public static SignUpResponse from(SignUpSuccessResponse signUpSuccessResponse) {
		return SignUpResponse.builder()
			.nickname(signUpSuccessResponse.nickname())
			.positions(signUpSuccessResponse.positions())
			.email(signUpSuccessResponse.email())
			.build();
	}
}
