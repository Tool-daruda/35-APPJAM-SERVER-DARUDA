package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record ToolDetailGetRes(
        Long toolId,
        String toolMainName,
        String toolSubName,
        String description,
        License license,
        List<String> keywords,
        Category category,
        String toolLink,
        Boolean supportKorea,
        List<PlatformRes> platform,
        String detailDescription,
        List<String> videos,
        List<String> images,
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy.MM.dd",timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static ToolDetailGetRes of( Tool tool, List<PlatformRes> platform, List<String> keywords , List<String> images ,List<String> videos){

        return ToolDetailGetRes.builder()
                .toolId(tool.getToolId())
                .toolMainName(tool.getToolMainName())
                .toolSubName(tool.getToolSubName())
                .description(tool.getDescription())
                .license(tool.getLicense())
                .keywords(keywords)
                .category(tool.getCategory())
                .toolLink(tool.getToolLink())
                .supportKorea(tool.getSupportKorea())
                .platform(platform)
                .detailDescription(tool.getDetailDescription())
                .updatedAt(tool.getUpdatedAt())
                .images(images)
                .videos(videos)
                .build();
    }
}
