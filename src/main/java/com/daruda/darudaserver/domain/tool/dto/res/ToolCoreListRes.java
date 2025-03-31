package com.daruda.darudaserver.domain.tool.dto.res;

import java.util.List;

import lombok.Builder;

@Builder
public record ToolCoreListRes(
	List<ToolCoreRes> toolCoreResList
) {
	public static ToolCoreListRes of(List<ToolCoreRes> toolCoreResList) {
		return ToolCoreListRes.builder()
			.toolCoreResList(toolCoreResList)
			.build();
	}
}
