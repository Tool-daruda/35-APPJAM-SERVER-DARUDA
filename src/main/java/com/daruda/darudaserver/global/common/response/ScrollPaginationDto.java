package com.daruda.darudaserver.global.common.response;

public record ScrollPaginationDto(
         long totalElements,
         long nextCursor
) {
    public static ScrollPaginationDto of(long totalElements, long  nextCursor ){
        return new ScrollPaginationDto(totalElements, nextCursor);
    }
}
