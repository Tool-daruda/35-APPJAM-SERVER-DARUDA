package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;

import lombok.Builder;

@Builder
public record MyProfileResponse(
	Long userId,
	String nickname,
	Positions positions
) {
	public static MyProfileResponse of(User userEntity) {
		return MyProfileResponse.builder()
			.userId(userEntity.getId())
			.nickname(userEntity.getNickname())
			.positions(userEntity.getPositions())
			.build();
	}
}
