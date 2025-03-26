package com.daruda.darudaserver.domain.user.dto.response;

import java.util.List;

import com.daruda.darudaserver.domain.tool.dto.res.ToolDtoGetRes;

public record FavoriteToolsResponse(
	List<ToolDtoGetRes> toolList
) {
	public static FavoriteToolsResponse of(List<ToolDtoGetRes> toolList) {
		return new FavoriteToolsResponse(toolList);
	}
}
