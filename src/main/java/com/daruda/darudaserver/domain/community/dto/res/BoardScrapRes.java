package com.daruda.darudaserver.domain.community.dto.res;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record BoardScrapRes(
	Long boardId,
	Boolean scarp
) {
	public static BoardScrapRes of(final Long boardId, final Boolean scarp) {
		return BoardScrapRes.builder()
			.boardId(boardId)
			.scarp(scarp)
			.build();
	}
}
