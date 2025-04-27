package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

public record MyProfileResponse(
	String nickname,
	Positions positions
) {
	public static MyProfileResponse of(String nickname, Positions positions) {
		return new MyProfileResponse(nickname, positions);
	}
}
