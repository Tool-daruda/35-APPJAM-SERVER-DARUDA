package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.tool.dto.res.ToolDtoGetRes;

import java.util.List;

public record FavoriteToolsResponse(
        List<ToolDtoGetRes> toolDtoGetResList,
        PagenationDto pagenationDto
) {
    public static FavoriteToolsResponse of(List<ToolDtoGetRes> toolDtoGetResList, PagenationDto pagenationDto){
        return new FavoriteToolsResponse(toolDtoGetResList, pagenationDto);
    }
}
