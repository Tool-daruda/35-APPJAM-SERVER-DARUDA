package com.daruda.darudaserver.domain.community.dto.res;

import java.util.List;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

public record GetBoardResponse(
	List<BoardRes> contents,
	ScrollPaginationDto scrollPaginationDto,
	Long nextScrapCount
) {

	public static GetBoardResponse of(List<BoardRes> boardResList, ScrollPaginationDto scrollPaginationDto) {
		return new GetBoardResponse(boardResList, scrollPaginationDto, null);
	}

	public static GetBoardResponse of(List<BoardRes> boardResList, ScrollPaginationDto scrollPaginationDto,
		Long nextScrapCount) {
		return new GetBoardResponse(boardResList, scrollPaginationDto, nextScrapCount);
	}
}
