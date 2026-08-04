package com.daruda.darudaserver.domain.tool.dto.response;

import java.util.List;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

import lombok.Builder;

@Builder
public record ToolListResponse(
	List<ToolResponse> tools,
	ScrollPaginationDto scrollPaginationDto
) {
	public static ToolListResponse of(List<ToolResponse> tools, ScrollPaginationDto scrollPaginationDto) {
		return new ToolListResponse(tools, scrollPaginationDto);
	}
}
