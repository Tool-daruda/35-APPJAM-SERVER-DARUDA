package com.daruda.darudaserver.domain.community.dto.res;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record BoardScrapRes(
	Long boardId,
	Boolean scrap
) {
	public static BoardScrapRes of(final Long boardId, final Boolean scrap) {
		return BoardScrapRes.builder()
			.boardId(boardId)
			.scrap(scrap)
			.build();
	}
}
