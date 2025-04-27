package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

import lombok.Builder;

@Builder
public record UpdateMyResponse(
	String nickname,
	Positions positions
) {
	public static UpdateMyResponse of(String nickname, Positions positions) {
		return UpdateMyResponse.builder()
			.nickname(nickname)
			.positions(positions)
			.build();
	}
}
