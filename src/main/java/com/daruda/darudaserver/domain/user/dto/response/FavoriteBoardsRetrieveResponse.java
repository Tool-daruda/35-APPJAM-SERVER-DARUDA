package com.daruda.darudaserver.domain.user.dto.response;

import java.util.List;

public record FavoriteBoardsRetrieveResponse(
        Long userId,
        List<FavoriteBoardsResponse> boardList,
        PagenationDto pageInfo
) {
    public static FavoriteBoardsRetrieveResponse of(Long userId, List<FavoriteBoardsResponse> boardList, PagenationDto pageInfo){
        return new FavoriteBoardsRetrieveResponse(userId, boardList, pageInfo);
    }
}
