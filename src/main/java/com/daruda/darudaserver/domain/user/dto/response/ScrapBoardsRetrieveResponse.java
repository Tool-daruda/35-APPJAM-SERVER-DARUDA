package com.daruda.darudaserver.domain.user.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record ScrapBoardsRetrieveResponse(
	Long userId,
	List<ScrapBoardsResponse> boardList,
	PagenationDto pageInfo
) {
	public static ScrapBoardsRetrieveResponse of(Long userId, List<ScrapBoardsResponse> boardList,
		PagenationDto pageInfo) {

		return ScrapBoardsRetrieveResponse.builder()
			.userId(userId)
			.boardList(List.copyOf(boardList))
			.pageInfo(pageInfo)
			.build();
	}
}
