package com.daruda.darudaserver.domain.tool.dto.res;

import java.util.List;

import lombok.Builder;

@Builder
public record PlanListRes(
	String planLink,
	List<PlanRes> toolPlans
) {
	public static PlanListRes of(String planLink, List<PlanRes> planRes) {
		return PlanListRes.builder()
			.planLink(planLink)
			.toolPlans(planRes)
			.build();
	}
}
