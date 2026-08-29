package com.daruda.darudaserver.domain.tool.dto.response;

import com.daruda.darudaserver.domain.tool.entity.Plan;

import lombok.Builder;

@Builder
public record PlanResponse(
	Long planId,
	String planName,
	Long priceAnnual,
	Long priceMonthly,
	String description
) {
	public static PlanResponse of(final Plan plan) {
		return PlanResponse.builder()
			.planId(plan.getId())
			.planName(plan.getPlanName())
			.priceAnnual(plan.getPriceAnnual())
			.priceMonthly(plan.getPriceMonthly())
			.description(plan.getDescription())
			.build();
	}
}
