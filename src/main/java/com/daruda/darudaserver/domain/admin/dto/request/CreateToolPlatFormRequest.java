package com.daruda.darudaserver.domain.admin.dto.request;

import org.springframework.context.annotation.Description;

public record CreateToolPlatFormRequest(
	@Description("웹 지원 여부")
	Boolean supportWeb,
	@Description("Windows 지원 여부")
	Boolean supportWindows,
	@Description("Mac OS 지원 여부")
	Boolean supportMac
) {
}
