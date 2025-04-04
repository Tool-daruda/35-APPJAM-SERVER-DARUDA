package com.daruda.darudaserver.domain.user.service;

import com.daruda.darudaserver.domain.user.dto.response.UserInfo;

public interface SocialService {
	String getAccessToken(String code);

	UserInfo getInfo(String code);
}
