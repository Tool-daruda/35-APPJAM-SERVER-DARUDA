package com.daruda.darudaserver.domain.comment.dto.response;

public record PagenationDto(
        int pageNo,
        int size,
        int totalPages
) {
    public static PagenationDto of(int pageNo, int size, int totalPages){
        return new PagenationDto(pageNo, size, totalPages);
    }
}
