package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolPlanRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Getter
@Builder
@Table(name = "tool_plan")
public class Plan {

	// TODO: 재구조화 필요 (연간, 월간, isDollar 정책 확립)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long planId;

	@Column(name = "plan_name", nullable = false)
	private String planName;

	@Column(name = "price_monthly", nullable = false)
	private Long priceMonthly;

	@Column(name = "price_annual")
	private Long priceAnnual;

	@Column(name = "description", nullable = false, length = 500)
	private String description;

	@Column(name = "is_dollar", nullable = false)
	private Boolean isDollar;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	public static Plan create(CreateToolPlanRequest toolPlanRequest, Tool tool) {
		return Plan.builder()
			.planName(toolPlanRequest.planName())
			.priceMonthly(toolPlanRequest.planPrice())
			.description(toolPlanRequest.planDescription())
			.isDollar(false)
			.tool(tool)
			.build();
	}
}
