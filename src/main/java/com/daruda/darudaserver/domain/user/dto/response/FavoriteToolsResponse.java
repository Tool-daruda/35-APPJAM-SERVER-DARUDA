package com.daruda.darudaserver.domain.user.dto.response;

import java.util.List;

import com.daruda.darudaserver.domain.tool.dto.response.ToolDtoGetResponse;

public record FavoriteToolsResponse(
	List<ToolDtoGetResponse> toolList
) {
	public static FavoriteToolsResponse of(List<ToolDtoGetResponse> toolList) {
		return new FavoriteToolsResponse(toolList);
	}
}
