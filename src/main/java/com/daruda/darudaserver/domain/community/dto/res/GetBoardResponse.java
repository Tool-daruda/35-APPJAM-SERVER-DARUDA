package com.daruda.darudaserver.domain.community.dto.res;

import java.util.List;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

public record GetBoardResponse(
	List<BoardRes> contents,
	ScrollPaginationDto scrollPaginationDto) {
	private static final long LAST_CURSOR = -1L;

	public static GetBoardResponse of(List<BoardRes> boardResList, ScrollPaginationDto scrollPaginationDto) {
		return new GetBoardResponse(boardResList, scrollPaginationDto);
	}
}

