package com.daruda.darudaserver.domain.user.dto.request;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMyRequest(
	@Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
	@Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영어, 숫자만 허용됩니다.")
	String nickname,
	@Nullable
	Positions positions
) {
	public static UpdateMyRequest of(String nickname, String positions) {
		return new UpdateMyRequest(nickname, positions);
	}
}
