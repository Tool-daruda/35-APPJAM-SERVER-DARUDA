package com.daruda.darudaserver.domain.tool.dto.res;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;
@Builder(access = AccessLevel.PRIVATE)
public record ToolCoreListRes (
        List<ToolCoreRes> toolCoreResList
){
    public static ToolCoreListRes of (List<ToolCoreRes> toolCoreResList){
        return ToolCoreListRes.builder()
                .toolCoreResList(toolCoreResList)
                .build();
    }
}
