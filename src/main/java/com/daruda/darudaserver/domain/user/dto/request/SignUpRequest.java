package com.daruda.darudaserver.domain.user.dto.request;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
	@NotNull(message = "닉네임은 필수 입력값입니다")
	@Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
	@Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영어, 숫자만 허용됩니다.")
	String nickname,

	@NotNull(message = "소속은 필수 입력값입니다")
	Positions positions,

	@NotNull(message = "이메일은 필수 입력값입니다")
	@Email(message = "이메일 형식에 맞지 않습니다")
	String email
) {
}
