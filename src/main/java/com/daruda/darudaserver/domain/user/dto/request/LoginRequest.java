package com.daruda.darudaserver.domain.user.dto.request;

import com.daruda.darudaserver.domain.user.entity.enums.SocialType;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
	@NotNull(message = "소셜 로그인 종류가 입력되지 않았습니다.")
	SocialType socialType
) {
}
