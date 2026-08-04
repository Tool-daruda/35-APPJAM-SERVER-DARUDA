package com.daruda.darudaserver.domain.tool.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record RelatedToolListResponse(
	List<RelatedToolResponse> relatedToolResList
) {
	public static RelatedToolListResponse of(List<RelatedToolResponse> relatedToolRes) {
		return RelatedToolListResponse.builder()
			.relatedToolResList(relatedToolRes)
			.build();
	}
}
