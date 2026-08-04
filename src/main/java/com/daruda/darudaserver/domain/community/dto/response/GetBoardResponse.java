package com.daruda.darudaserver.domain.community.dto.response;

import java.util.List;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

public record GetBoardResponse(
	List<BoardResponse> contents,
	ScrollPaginationDto scrollPaginationDto,
	Long nextScrapCount
) {

	public static GetBoardResponse of(List<BoardResponse> boardResList, ScrollPaginationDto scrollPaginationDto) {
		return new GetBoardResponse(boardResList, scrollPaginationDto, null);
	}

	public static GetBoardResponse of(List<BoardResponse> boardResList, ScrollPaginationDto scrollPaginationDto,
		Long nextScrapCount) {
		return new GetBoardResponse(boardResList, scrollPaginationDto, nextScrapCount);
	}
}
