package com.daruda.darudaserver.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NoticeRequest(
	@NotBlank(message = "제목은 필수 입력값입니다")
	@Size(max = 100, message = "제목은 100자 이내로 입력해주세요")
	String title,

	@NotBlank(message = "URL은 필수 입력값입니다")
	@Pattern(regexp = "^(http|https)://.*$", message = "올바른 URL 형식이 아닙니다")
	String url
) {
}
