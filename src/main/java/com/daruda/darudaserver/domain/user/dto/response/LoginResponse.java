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
	public static LoginResponse of(String email, boolean isUser, String nickname) {
		return LoginResponse.builder()
			.email(email)
			.isUser(isUser)
			.nickname(nickname)
			.build();
	}
}
