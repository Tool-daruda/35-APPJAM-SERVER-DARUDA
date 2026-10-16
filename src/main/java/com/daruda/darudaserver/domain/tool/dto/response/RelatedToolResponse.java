package com.daruda.darudaserver.domain.tool.dto.response;

import java.util.List;

import com.daruda.darudaserver.domain.tool.entity.Tool;

import lombok.Builder;

@Builder
public record RelatedToolResponse(
	Long toolId,
	String toolName,
	String toolLogo,
	String license,
	List<String> keywords
) {

	public static RelatedToolResponse of(Tool tool, List<String> keywords) {
		return RelatedToolResponse.builder()
			.toolId(tool.getId())
			.toolName(tool.getToolMainName())
			.toolLogo(tool.getToolLogo())
			.license(tool.getLicense().getKoreanName())
			.keywords(keywords)
			.build();
	}
}
