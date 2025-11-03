package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.Plan;

import lombok.Builder;

@Builder
public record PlanRes(
	Long planId,
	String planName,
	Long price,
	String description
) {
	public static PlanRes of(final Plan plan) {
		return PlanRes.builder()
			.planId(plan.getPlanId())
			.planName(plan.getPlanName())
			.price(plan.getPrice())
			.description(plan.getDescription())
			.build();
	}
}
