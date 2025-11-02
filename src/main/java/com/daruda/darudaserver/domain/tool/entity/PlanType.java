package com.daruda.darudaserver.domain.tool.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlanType {
	FREE("무료"),
	MONTHLY("월간"),
	PURCHASE("구매"),
	MONTHLY_ANNUAL("월간 & 연간");

	private final String type;

	public static PlanType formString(String type) {
		for (PlanType planType : PlanType.values()) {
			if (planType.getType().equals(type)) {
				return planType;
			}
		}
		throw new IllegalArgumentException("Invalid PlanType: " + type);
	}
}
