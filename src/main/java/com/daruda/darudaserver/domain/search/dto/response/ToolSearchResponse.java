package com.daruda.darudaserver.domain.search.dto.response;

import java.util.List;

import com.daruda.darudaserver.domain.search.document.ToolDocument;

public record ToolSearchResponse(
	String toolId,
	String toolName,
	String toolLogo,
	String description,
	String license,
	List<String> keywords,
	boolean isScraped,
	String bgColor,
	boolean fontColor
) {
	public static ToolSearchResponse from(ToolDocument document, List<String> keywords, boolean isScrapped) {
		return new ToolSearchResponse(
			document.getId(),
			document.getToolMainName(),
			document.getToolLogo(),
			document.getDescription(),
			document.getLicense(),
			keywords,
			isScrapped,
			document.getBgColor(),
			document.isFontColor()
		);
	}
}
