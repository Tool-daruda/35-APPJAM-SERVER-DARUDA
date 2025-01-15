package com.daruda.darudaserver.domain.user.dto.response;

import java.util.List;

public record FavoriteBoardsRetrieveResponse(
        Long userId,
        List<FavoriteBoardsResponse> favoriteBoardsResponseList
) {
    public static FavoriteBoardsRetrieveResponse of(Long userId, List<FavoriteBoardsResponse>favoriteBoardsResponseList){
        return new FavoriteBoardsRetrieveResponse(userId, favoriteBoardsResponseList);
    }
}
