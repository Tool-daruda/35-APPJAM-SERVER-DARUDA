package com.daruda.darudaserver.domain.tool.dto.response;

import lombok.Builder;

@Builder
public record ToolLikeResponse(
	Long toolId,
	boolean liked,
	int likeCount
) {
	public static ToolLikeResponse of(final Long toolId, final boolean liked, final int likeCount) {
		return ToolLikeResponse.builder()
			.toolId(toolId)
			.liked(liked)
			.likeCount(likeCount)
			.build();
	}
}
