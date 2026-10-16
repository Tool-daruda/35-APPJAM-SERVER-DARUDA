package com.daruda.darudaserver.domain.tool.dto.response;

import com.daruda.darudaserver.domain.tool.entity.ToolCore;

import lombok.Builder;

@Builder
public record ToolCoreResponse(
	Long coreId,
	String coreTitle,
	String coreContent
) {
	public static ToolCoreResponse of(ToolCore toolCore) {
		return ToolCoreResponse.builder()
			.coreId(toolCore.getId())
			.coreTitle(toolCore.getCoreTitle())
			.coreContent(toolCore.getCoreContent())
			.build();
	}
}
