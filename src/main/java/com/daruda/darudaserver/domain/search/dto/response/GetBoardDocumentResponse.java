package com.daruda.darudaserver.domain.search.dto.response;

import java.util.List;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

public record GetBoardDocumentResponse(
	List<BoardSearchResponse> contents,
	ScrollPaginationDto scrollPaginationDto) {
	private static final long LAST_CURSOR = -1L;

	public static GetBoardDocumentResponse of(List<BoardSearchResponse> boardSearchResponses,
		ScrollPaginationDto scrollPaginationDto) {
		return new GetBoardDocumentResponse(boardSearchResponses, scrollPaginationDto);
	}
}
