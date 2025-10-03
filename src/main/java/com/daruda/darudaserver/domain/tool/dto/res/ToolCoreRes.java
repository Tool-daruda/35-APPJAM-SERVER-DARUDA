package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.ToolCore;

import lombok.Builder;

@Builder
public record ToolCoreRes(
	Long coreId,
	String coreTitle,
	String coreContent
) {
	public static ToolCoreRes of(ToolCore toolCore) {
		return ToolCoreRes.builder()
			.coreId(toolCore.getCoreId())
			.coreTitle(toolCore.getCoreTitle())
			.coreContent(toolCore.getCoreContent())
			.build();
	}
}
