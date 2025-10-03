package com.daruda.darudaserver.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeRequest(
	@NotBlank(message = "제목은 필수 입력값입니다")
	@Size(max = 100, message = "제목은 100자 이내로 입력해주세요")
	String title,

	@NotBlank(message = "내용은 필수 입력값입니다")
	@Size(max = 1000, message = "내용은 1000자 이내로 입력해주세요")
	String content
) {
}
