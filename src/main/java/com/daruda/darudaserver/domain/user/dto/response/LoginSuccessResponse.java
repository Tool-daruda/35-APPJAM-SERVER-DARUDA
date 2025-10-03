package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginSuccessResponse(
	Long userId,
	String email,
	boolean isUser,
	String nickname,
	Positions positions,
	JwtTokenResponse jwtTokenResponse
) {
	//등록된 회원이 아닌 경우
	public static LoginSuccessResponse ofNonRegisteredUser(String email) {
		return new LoginSuccessResponse(null, email, false, null, null, null);
	}

	//등록된 회원인 경우
	public static LoginSuccessResponse ofRegisteredUser(JwtTokenResponse jwtTokenResponse, UserEntity userEntity) {
		return new LoginSuccessResponse(userEntity.getId(), null, true, userEntity.getNickname(),
			userEntity.getPositions(), jwtTokenResponse);
	}
}
