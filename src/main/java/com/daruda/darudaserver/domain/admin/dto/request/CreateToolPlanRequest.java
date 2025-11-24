package com.daruda.darudaserver.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record CreateToolPlanRequest(
	@Schema(
		description = "플랜명",
		example = "Basic",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	String planName,
	@Schema(
		description = "플랜 가격(원화)",
		example = "10000",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	Long planPrice,
	@Schema(
		description = "플랜 상세 설명 (최대 500자, 필수)",
		example = "기본 기능을 포함한 플랜입니다. 월 10,000원으로 이용 가능합니다.",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@Size(max = 500, message = "플랜 상세 설명은 500자 이내로 작성해주세요.")
	String planDescription
) {
}
