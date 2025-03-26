package com.daruda.darudaserver.domain.user.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialType {
	KAKAO("카카오");

	private final String name;
}
