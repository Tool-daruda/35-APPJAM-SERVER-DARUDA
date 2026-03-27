package com.daruda.darudaserver.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateToolPlanRequest(
	@Schema(
		description = "플랜명",
		example = "Basic",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotBlank(message = "플랜명은 필수입니다.")
	String planName,
	@Schema(
		description = "월간 플랜 가격(원화)",
		example = "10000",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotNull(message = "월간 플랜 가격은 필수입니다.")
	@PositiveOrZero(message = "플랜 가격은 0 이상이어야 합니다.")
	Long priceMonthly,
	@Schema(
		description = "연간 플랜 가격(원화)",
		example = "10000",
		requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@PositiveOrZero(message = "플랜 가격은 0 이상이어야 합니다.")
	Long priceAnnual,
	@Schema(
		description = "플랜 상세 설명 (최대 500자, 필수)",
		example = "기본 기능을 포함한 플랜입니다. 월 10,000원으로 이용 가능합니다.",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotBlank(message = "플랜 상세 설명은 필수입니다.")
	@Size(max = 500, message = "플랜 상세 설명은 500자 이내로 작성해주세요.")
	String planDescription
) {
}
