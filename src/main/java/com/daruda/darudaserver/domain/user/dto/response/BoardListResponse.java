package com.daruda.darudaserver.domain.user.dto.response;

import com.daruda.darudaserver.domain.community.dto.res.BoardRes;

import java.util.List;

public record BoardListResponse(
        List<BoardRes> boardList,
        Long userId
) {
    public static BoardListResponse of(List<BoardRes> boardList, Long userId){
        return new BoardListResponse(boardList, userId);
    }
}
