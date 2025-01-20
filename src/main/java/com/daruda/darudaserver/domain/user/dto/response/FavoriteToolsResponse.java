package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.tool.dto.res.ToolDtoGetRes;

import java.util.List;

public record FavoriteToolsResponse(
        List<ToolDtoGetRes> toolList
) {
    public static FavoriteToolsResponse of(List<ToolDtoGetRes> toolList){
        return new FavoriteToolsResponse(toolList);
    }
}
