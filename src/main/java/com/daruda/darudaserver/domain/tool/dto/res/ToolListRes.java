package com.daruda.darudaserver.domain.tool.dto.res;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record ToolListRes (
        List<ToolDtoGetRes> tools,
        boolean hasNext
){
    public static ToolListRes of(List<ToolDtoGetRes> tools, boolean hasNext){
        return ToolListRes.builder()
                .tools(tools)
                .hasNext(hasNext)
                .build();
    }
}
