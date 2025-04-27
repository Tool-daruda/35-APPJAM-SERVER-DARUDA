package com.daruda.darudaserver.domain.user.service;

import com.daruda.darudaserver.domain.user.dto.response.UserInformationResponse;

public interface SocialService {
	String getLoginUrl();

	UserInformationResponse getInfo(String code);
}
