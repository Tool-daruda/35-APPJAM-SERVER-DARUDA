package com.daruda.darudaserver.domain.tool.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record ToolCoreListResponse(
	List<ToolCoreResponse> toolCoreResList
) {
	public static ToolCoreListResponse of(List<ToolCoreResponse> toolCoreResList) {
		return ToolCoreListResponse.builder()
			.toolCoreResList(toolCoreResList)
			.build();
	}
}
