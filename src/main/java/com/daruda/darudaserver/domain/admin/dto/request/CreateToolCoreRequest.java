package com.daruda.darudaserver.domain.admin.dto.request;

import org.springframework.context.annotation.Description;

import jakarta.validation.constraints.Size;

public record CreateToolCoreRequest(
	@Description("핵심 기능명")
	String coreName,
	@Description("핵심 기능 상세 설명")
	@Size(max = 500, message = "핵심 기능 상세 설명은 500자 이내로 작성해주세요.")
	String coreContent
) {
}
