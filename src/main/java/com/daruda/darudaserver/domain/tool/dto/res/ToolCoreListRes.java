package com.daruda.darudaserver.domain.tool.dto.res;


import lombok.Builder;

import java.util.List;
@Builder
public record ToolCoreListRes (
        List<ToolCoreRes> toolCoreResList
){
    public static ToolCoreListRes of (List<ToolCoreRes> toolCoreResList){
        return ToolCoreListRes.builder()
                .toolCoreResList(toolCoreResList)
                .build();
    }
}
