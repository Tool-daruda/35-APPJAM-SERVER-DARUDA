package com.daruda.darudaserver.domain.tool.dto.res;

import lombok.Builder;

import java.util.List;

@Builder
public record PlanListRes(
        List<PlanRes> toolPlans
){
    public static PlanListRes of (List<PlanRes> planRes){
        return PlanListRes.builder()
                .toolPlans(planRes)
                .build();
    }
}
