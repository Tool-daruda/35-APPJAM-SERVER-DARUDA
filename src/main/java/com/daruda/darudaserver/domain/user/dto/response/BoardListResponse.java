package com.daruda.darudaserver.domain.user.dto.response;

import java.util.List;

import com.daruda.darudaserver.domain.community.dto.response.BoardResponse;

public record BoardListResponse(
	List<BoardResponse> boardList,
	Long userId,
	PagenationDto pageInfo
) {
	public static BoardListResponse of(List<BoardResponse> boardList, Long userId, PagenationDto pageInfo) {
		return new BoardListResponse(boardList, userId, pageInfo);
	}
}
