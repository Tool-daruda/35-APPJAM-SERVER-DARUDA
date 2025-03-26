package com.daruda.darudaserver.domain.tool.dto.res;

import java.util.List;

import com.daruda.darudaserver.domain.tool.entity.Tool;

import lombok.Builder;

@Builder
public record RelatedToolRes(
	Long toolId,
	String toolName,
	String toolLogo,
	String license,
	List<String> keywords
) {

	public static RelatedToolRes of(Tool tool, List<String> keywords) {
		return RelatedToolRes.builder()
			.toolId(tool.getToolId())
			.toolName(tool.getToolMainName())
			.toolLogo(tool.getToolLogo())
			.license(tool.getLicense().getKoreanName())
			.keywords(keywords)
			.build();
	}
}
