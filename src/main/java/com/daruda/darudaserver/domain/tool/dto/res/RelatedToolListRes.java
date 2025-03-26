package com.daruda.darudaserver.domain.tool.dto.res;

import java.util.List;

import lombok.Builder;

@Builder
public record RelatedToolListRes(
	List<RelatedToolRes> relatedToolResList
) {
	public static RelatedToolListRes of(List<RelatedToolRes> relatedToolRes) {
		return RelatedToolListRes.builder()
			.relatedToolResList(relatedToolRes)
			.build();
	}
}
