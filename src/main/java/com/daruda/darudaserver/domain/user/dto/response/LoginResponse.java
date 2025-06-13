package com.daruda.darudaserver.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
	String email,
	boolean isUser,
	String nickname
) {
	public static LoginResponse from(LoginSuccessResponse loginSuccessResponse) {
		return LoginResponse.builder()
			.email(loginSuccessResponse.email())
			.isUser(loginSuccessResponse.isUser())
			.nickname(loginSuccessResponse.nickname())
			.build();
	}
}
