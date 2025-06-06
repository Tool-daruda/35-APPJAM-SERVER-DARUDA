package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;

import lombok.Builder;

@Builder
public record MyProfileResponse(
	Long userid,
	String nickname,
	Positions positions
) {
	public static MyProfileResponse of(UserEntity userEntity) {
		return MyProfileResponse.builder()
			.userid(userEntity.getId())
			.nickname(userEntity.getNickname())
			.positions(userEntity.getPositions())
			.build();
	}
}
