package com.daruda.darudaserver.domain.tool.dto.response;

import java.util.List;

import com.daruda.darudaserver.domain.tool.entity.Tool;

public record ToolDtoGetResponse(
	Long toolId,
	String toolName,
	String toolLogo,
	String description,
	String license,
	List<String> keywords,
	Boolean isScraped
) {
	// 정적 팩토리 메서드
	public static ToolDtoGetResponse from(Tool tool, List<String> keywords, Boolean isScraped) {
		return new ToolDtoGetResponse(
			tool.getId(),
			tool.upperMainName(tool.getToolMainName()),
			tool.getToolLogo(),
			tool.getDescription(),
			tool.getLicense().getKoreanName(),
			keywords,
			isScraped
		);
	}
}
