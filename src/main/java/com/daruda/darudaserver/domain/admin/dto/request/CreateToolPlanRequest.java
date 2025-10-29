package com.daruda.darudaserver.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record CreateToolPlanRequest(
	@Schema(description = "플랜명")
	String planName,
	@Schema(description = "플랜 가격(원화)")
	Long planPrice,
	@Schema(description = "플랜 상세 설명")
	@Size(max = 500, message = "플랜 상세 설명은 500자 이내로 작성해주세요.")
	String planDescription
) {
}
