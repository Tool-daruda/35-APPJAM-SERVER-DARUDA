package com.daruda.darudaserver.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateToolPlatFormRequest(
	@Schema(description = "웹 지원 여부")
	Boolean supportWeb,
	@Schema(description = "Windows 지원 여부")
	Boolean supportWindows,
	@Schema(description = "Mac OS 지원 여부")
	Boolean supportMac
) {
}
