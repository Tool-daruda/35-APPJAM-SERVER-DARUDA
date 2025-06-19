package com.daruda.darudaserver.domain.search.dto.response;

import java.util.List;

public record SearchAllResponse(
	List<BoardSearchResponse> boards,
	List<ToolSearchResponse> tools
) {
	public static SearchAllResponse of(
		List<BoardSearchResponse> boards,
		List<ToolSearchResponse> tools
	) {
		return new SearchAllResponse(boards, tools);
	}
}
