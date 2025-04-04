package com.daruda.darudaserver.domain.user.service;

import com.daruda.darudaserver.domain.user.dto.response.UserInfo;

public interface SocialService {
	String getLoginUrl();

	UserInfo getInfo(String code);
}
