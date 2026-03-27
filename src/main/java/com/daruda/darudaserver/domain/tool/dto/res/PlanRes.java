package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.Plan;

import lombok.Builder;

@Builder
public record PlanRes(
	Long planId,
	String planName,
	Long priceAnnual,
	Long priceMonthly,
	String description
) {
	public static PlanRes of(final Plan plan) {
		return PlanRes.builder()
			.planId(plan.getPlanId())
			.planName(plan.getPlanName())
			.priceAnnual(plan.getPriceAnnual())
			.priceMonthly(plan.getPriceMonthly())
			.description(plan.getDescription())
			.build();
	}
}
