package com.daruda.darudaserver.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
	String email,
	boolean isUser,
	String nickname,
	String accessToken
) {
	public static LoginResponse of(String email, boolean isUser, String nickname, String accessToken) {
		return LoginResponse.builder()
			.email(email)
			.isUser(isUser)
			.nickname(nickname)
			.accessToken(accessToken)
			.build();
	}
}
