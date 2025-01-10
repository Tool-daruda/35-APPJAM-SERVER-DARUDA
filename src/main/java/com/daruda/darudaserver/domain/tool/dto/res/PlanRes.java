package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.Plan;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PlanRes(
        Long planId,
        String planName,
        int monthlyPrice,
        int annualPrice,
        String description
) {
    public static PlanRes of (final Plan plan){
        return PlanRes.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .monthlyPrice(plan.getPriceMonthly())
                .annualPrice(plan.getPriceAnnual())
                .description(plan.getDescription())
                .build();
    }
}
