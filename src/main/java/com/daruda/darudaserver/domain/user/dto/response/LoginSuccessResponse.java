package com.daruda.darudaserver.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginSuccessResponse(
	String email,
	boolean isUser,
	String nickname,
	JwtTokenResponse jwtTokenResponse
) {
	//등록된 회원이 아닌 경우
	public static LoginSuccessResponse ofNonRegisteredUser(String email) {
		return new LoginSuccessResponse(email, false, null, null);
	}

	//등록된 회원인 경우
	public static LoginSuccessResponse ofRegisteredUser(JwtTokenResponse jwtTokenResponse, String nickname) {
		return new LoginSuccessResponse(null, true, nickname, jwtTokenResponse);
	}
}
