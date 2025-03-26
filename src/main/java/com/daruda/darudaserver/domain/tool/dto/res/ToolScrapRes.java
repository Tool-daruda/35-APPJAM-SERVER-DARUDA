package com.daruda.darudaserver.domain.tool.dto.res;

import lombok.Builder;

@Builder
public record ToolScrapRes(
	Long toolId,
	Boolean scarp
) {
	public static ToolScrapRes of(final Long toolId, final Boolean scarp) {
		return ToolScrapRes.builder()
			.toolId(toolId)
			.scarp(scarp)
			.build();
	}
}
