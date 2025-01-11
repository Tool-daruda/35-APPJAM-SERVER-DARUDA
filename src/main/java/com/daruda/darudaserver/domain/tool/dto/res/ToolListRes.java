package com.daruda.darudaserver.domain.tool.dto.res;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record ToolListRes (
        List<ToolDtoGetRes> tools
){
    public static ToolListRes of(List<ToolDtoGetRes> tools){
        return ToolListRes.builder()
                .tools(tools)
                .build();
    }
}
