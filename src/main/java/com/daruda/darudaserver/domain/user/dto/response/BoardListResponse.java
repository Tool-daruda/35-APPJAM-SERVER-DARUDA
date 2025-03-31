package com.daruda.darudaserver.domain.user.dto.response;

import java.util.List;

import com.daruda.darudaserver.domain.community.dto.res.BoardRes;

public record BoardListResponse(
	List<BoardRes> boardList,
	Long userId,
	PagenationDto pageInfo
) {
	public static BoardListResponse of(List<BoardRes> boardList, Long userId, PagenationDto pageInfo) {
		return new BoardListResponse(boardList, userId, pageInfo);
	}
}
