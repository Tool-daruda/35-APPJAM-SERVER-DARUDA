package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;

import lombok.Builder;

@Builder
public record PlatformRes(
	Boolean Web,
	Boolean Windows,
	Boolean Mac
) {
	public static PlatformRes of(ToolPlatForm platForm) {
		return PlatformRes.builder()
			.Web(platForm.getWeb())
			.Windows(platForm.getWindows())
			.Mac(platForm.getMac())
			.build();
	}
}
