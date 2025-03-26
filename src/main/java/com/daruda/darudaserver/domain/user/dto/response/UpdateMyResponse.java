package com.daruda.darudaserver.domain.user.dto.response;

import lombok.Builder;

@Builder
public record UpdateMyResponse(
	String nickname,
	String positions
) {
	public static UpdateMyResponse of(String nickname, String positions) {
		return UpdateMyResponse.builder()
			.nickname(nickname)
			.positions(positions)
			.build();
	}
}
