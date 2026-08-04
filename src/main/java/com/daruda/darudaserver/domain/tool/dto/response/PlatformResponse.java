package com.daruda.darudaserver.domain.tool.dto.response;

import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;

import lombok.Builder;

@Builder
public record PlatformResponse(
	Boolean Web,
	Boolean Windows,
	Boolean Mac
) {
	public static PlatformResponse of(ToolPlatForm platForm) {
		return PlatformResponse.builder()
			.Web(platForm.getWeb())
			.Windows(platForm.getWindows())
			.Mac(platForm.getMac())
			.build();
	}
}
