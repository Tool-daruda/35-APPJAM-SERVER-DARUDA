package com.daruda.darudaserver.domain.tool.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record PlanListResponse(
	String planLink,
	List<PlanResponse> toolPlans
) {
	public static PlanListResponse of(String planLink, List<PlanResponse> planRes) {
		return PlanListResponse.builder()
			.planLink(planLink)
			.toolPlans(planRes)
			.build();
	}
}
