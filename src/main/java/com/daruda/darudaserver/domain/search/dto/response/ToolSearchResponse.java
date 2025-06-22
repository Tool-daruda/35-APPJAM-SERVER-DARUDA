package com.daruda.darudaserver.domain.search.dto.response;

import com.daruda.darudaserver.domain.search.document.ToolDocument;

public record ToolSearchResponse(
	String toolId,
	String toolMainName,
	String toolSubName,
	String description
) {
	public static ToolSearchResponse from(ToolDocument document) {
		return new ToolSearchResponse(
			document.getId(),
			document.getToolMainName(),
			document.getToolSubName(),
			document.getDescription()
		);
	}
}
