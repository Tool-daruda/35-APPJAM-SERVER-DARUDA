package com.daruda.darudaserver.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
	String email,
	boolean isUser,
	String nickname,
	JwtTokenResponse jwtTokenResponse
) {
	//등록된 회원이 아닌 경우
	public static LoginResponse ofNonRegisteredUser(String email) {
		return new LoginResponse(email, false, null, null);
	}

	//등록된 회원인 경우
	public static LoginResponse ofRegisteredUser(JwtTokenResponse jwtTokenResponse, String nickname) {
		return new LoginResponse(null, true, nickname, jwtTokenResponse);
	}
}
