package com.daruda.darudaserver.domain.user.dto.response;

public record PagenationDto(
        int pageNo,
        int size
) {
    public static PagenationDto of(int pageNo, int size){
        return new PagenationDto(pageNo, size);
    }
}
