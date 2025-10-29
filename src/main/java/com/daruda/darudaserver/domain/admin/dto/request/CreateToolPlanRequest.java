package com.daruda.darudaserver.domain.admin.dto.request;

import org.springframework.context.annotation.Description;

import jakarta.validation.constraints.Size;

public record CreateToolPlanRequest(
	@Description("플랜명")
	String planName,
	@Description("플랜 가격(원화)")
	Long planPrice,
	@Description("플랜 상세 설명")
	@Size(max = 500, message = "플랜 상세 설명은 500자 이내로 작성해주세요.")
	String planDescription
) {
}
