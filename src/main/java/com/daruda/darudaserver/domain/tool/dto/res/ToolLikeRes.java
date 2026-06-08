package com.daruda.darudaserver.domain.tool.dto.res;

import lombok.Builder;

@Builder
public record ToolLikeRes(
	Long toolId,
	boolean liked,
	int likeCount
) {
	public static ToolLikeRes of(final Long toolId, final boolean liked, final int likeCount) {
		return ToolLikeRes.builder()
			.toolId(toolId)
			.liked(liked)
			.likeCount(likeCount)
			.build();
	}
}
