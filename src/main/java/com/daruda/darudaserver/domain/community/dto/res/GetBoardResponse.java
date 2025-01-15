package com.daruda.darudaserver.domain.community.dto.res;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.global.common.response.ScrollPaginationCollection;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

import java.util.List;
import java.util.stream.Collectors;


public record GetBoardResponse(
         List<BoardRes> contents,
         ScrollPaginationDto scrollPaginationDto) {
    private static final long LAST_CURSOR = -1L;

    public static GetBoardResponse of (List<BoardRes> boardResList, ScrollPaginationDto scrollPaginationDto){
        return new GetBoardResponse(boardResList, scrollPaginationDto);
    }
}

