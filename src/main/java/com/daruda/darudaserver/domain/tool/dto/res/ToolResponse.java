package com.daruda.darudaserver.domain.tool.dto.res;

import java.util.List;

import com.daruda.darudaserver.domain.tool.entity.Tool;

import lombok.Builder;

@Builder
public record ToolResponse(
	Long toolId,
	String toolName,
	String toolLogo,
	String description,
	String license,
	List<String> keywords,
	Boolean isScraped,
	String bgColor,
	boolean fontColor
) {
	public static ToolResponse of(Tool tool, List<String> keywords, Boolean isScraped) {
		return ToolResponse.builder()
			.toolId(tool.getToolId())
			.toolName(tool.getToolMainName())
			.toolLogo(tool.getToolLogo())
			.description(tool.getDescription())
			.license(tool.getLicense().getKoreanName())
			.keywords(keywords)
			.isScraped(isScraped)
			.bgColor(tool.getBgColor())
			.fontColor(tool.isFontColor())
			.build();
	}
}
