package com.daruda.darudaserver.domain.user.service;

import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {
	private final KakaoService kakaoService;

	public SocialService findSocialService(SocialType socialType) {
		return switch (socialType) {
			case KAKAO -> kakaoService;
			// case GOOGLE -> googleSocialService;
			default -> throw new BadRequestException(ErrorCode.SOCIAL_TYPE_BAD_REQUEST);
		};
	}
}
