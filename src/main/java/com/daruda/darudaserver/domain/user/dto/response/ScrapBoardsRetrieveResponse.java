package com.daruda.darudaserver.domain.user.dto.response;

import java.util.List;

public record ScrapBoardsRetrieveResponse(
	Long userId,
	List<ScrapBoardsResponse> boardList,
	PagenationDto pageInfo
) {
	public static ScrapBoardsRetrieveResponse of(Long userId, List<ScrapBoardsResponse> boardList,
		PagenationDto pageInfo) {
		return new ScrapBoardsRetrieveResponse(userId, boardList, pageInfo);
	}
}
