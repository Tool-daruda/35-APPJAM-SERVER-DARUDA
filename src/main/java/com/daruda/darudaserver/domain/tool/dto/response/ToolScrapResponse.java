package com.daruda.darudaserver.domain.tool.dto.response;

import lombok.Builder;

@Builder
public record ToolScrapResponse(
	Long toolId,
	Boolean scarp
) {
	public static ToolScrapResponse of(final Long toolId, final Boolean scarp) {
		return ToolScrapResponse.builder()
			.toolId(toolId)
			.scarp(scarp)
			.build();
	}
}
