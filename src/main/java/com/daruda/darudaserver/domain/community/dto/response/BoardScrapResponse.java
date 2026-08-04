package com.daruda.darudaserver.domain.community.dto.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record BoardScrapResponse(
	Long boardId,
	Boolean scrap
) {
	public static BoardScrapResponse of(final Long boardId, final Boolean scrap) {
		return BoardScrapResponse.builder()
			.boardId(boardId)
			.scrap(scrap)
			.build();
	}
}
