package com.daruda.darudaserver.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

	@Value("${kakao.client_id}")
	private String clientId;

	@Value("${kakao.redirect_uri}")
	private String redirectUri;

	private final KakaoService kakaoService;

	public String getLoginUrl(SocialType socialType) {
		return switch (socialType) {
			case KAKAO ->
				"https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri="
					+ redirectUri;
			// case GOOGLE -> "google redirect url";
			default -> throw new BadRequestException(ErrorCode.SOCIAL_TYPE_BAD_REQUEST);
		};
	}

	public SocialService findSocialService(SocialType socialType) {
		return switch (socialType) {
			case KAKAO -> kakaoService;
			// case GOOGLE -> googleSocialService;
			default -> throw new BadRequestException(ErrorCode.SOCIAL_TYPE_BAD_REQUEST);
		};
	}
}
