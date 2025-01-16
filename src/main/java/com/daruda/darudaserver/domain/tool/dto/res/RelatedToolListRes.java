package com.daruda.darudaserver.domain.tool.dto.res;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder
public record RelatedToolListRes(
        List<RelatedToolRes> relatedToolResList
        ) {
    public static RelatedToolListRes of(List<RelatedToolRes> relatedToolRes){
        return RelatedToolListRes.builder()
                .relatedToolResList(relatedToolRes)
                .build();
    }
}
