package com.daruda.darudaserver.domain.tool.dto.res;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder
public record PlanListRes(
        List<PlanRes> toolCoreResList
){
    public static PlanListRes of (List<PlanRes> planRes){
        return PlanListRes.builder()
                .toolCoreResList(planRes)
                .build();
    }
}
