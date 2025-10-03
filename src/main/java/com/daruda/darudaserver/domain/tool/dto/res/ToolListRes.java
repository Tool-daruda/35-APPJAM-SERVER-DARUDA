package com.daruda.darudaserver.domain.tool.dto.res;

import java.util.List;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

import lombok.Builder;

@Builder
public record ToolListRes(
	List<ToolResponse> tools,
	ScrollPaginationDto scrollPaginationDto
) {
	public static ToolListRes of(List<ToolResponse> tools, ScrollPaginationDto scrollPaginationDto) {
		return new ToolListRes(tools, scrollPaginationDto);
	}
}
