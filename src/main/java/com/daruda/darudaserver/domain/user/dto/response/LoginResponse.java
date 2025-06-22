package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
	Long userId,
	String email,
	boolean isUser,
	String nickname,
	Positions positions
) {
	public static LoginResponse from(LoginSuccessResponse loginSuccessResponse) {
		return LoginResponse.builder()
			.userId(loginSuccessResponse.userId())
			.email(loginSuccessResponse.email())
			.isUser(loginSuccessResponse.isUser())
			.nickname(loginSuccessResponse.nickname())
			.positions(loginSuccessResponse.positions())
			.build();
	}
}
