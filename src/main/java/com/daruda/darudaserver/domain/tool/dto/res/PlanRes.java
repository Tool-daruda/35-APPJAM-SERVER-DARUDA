package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.Plan;
import lombok.Builder;

@Builder
public record PlanRes(
        Long planId,
        String planName,
        Long monthlyPrice,
        Long annualPrice,
        String description,
        Boolean isDollar
) {
    public static PlanRes of (final Plan plan){
        return PlanRes.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .monthlyPrice(plan.getPriceMonthly())
                .annualPrice(plan.getPriceAnnual())
                .description(plan.getDescription())
                .isDollar(plan.getIsDollar())
                .build();
    }
}
