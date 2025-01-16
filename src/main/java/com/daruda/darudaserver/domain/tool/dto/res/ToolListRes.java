package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ToolListRes (
        List<ToolResponse> tools,
        ScrollPaginationDto scrollPaginationDto
){
    public static ToolListRes of(List<ToolResponse> tools, ScrollPaginationDto scrollPaginationDto){
        return new ToolListRes(tools, scrollPaginationDto);
    }
}
