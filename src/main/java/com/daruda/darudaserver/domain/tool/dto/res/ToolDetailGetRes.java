package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;

@Builder
public record ToolDetailGetRes(
        Long toolId,
        String toolLogo,
        String toolMainName,
        String toolSubName,
        String description,
        String license,
        List<String> keywords,
        String category,
        String toolLink,
        Boolean supportKorea,
        List<PlatformRes> platform,
        String detailDescription,
        List<String> videos,
        List<String> images,
        String bgColor,
        Boolean fontColor,
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy.MM.dd",timezone = "Asia/Seoul")
        Timestamp updatedAt,
        Boolean isScrapped
) {
    public static ToolDetailGetRes of( Tool tool, List<PlatformRes> platform, String toolLogo, List<String> keywords , List<String> images ,List<String> videos,Boolean isScrapped){

        return ToolDetailGetRes.builder()
                .toolId(tool.getToolId())
                .toolLogo(tool.getToolLogo())
                .toolMainName(tool.getToolMainName())
                .toolSubName(tool.getToolSubName())
                .description(tool.getDescription())
                .license(tool.getLicense().getKoreanName())
                .keywords(keywords)
                .category(tool.getCategory().getKoreanName())
                .toolLink(tool.getToolLink())
                .supportKorea(tool.getSupportKorea())
                .platform(platform)
                .detailDescription(tool.getDetailDescription())
                .updatedAt(tool.getUpdatedAt())
                .images(images)
                .videos(videos)
                .bgColor(tool.getBgColor())
                .fontColor(tool.isFontColor())
                .isScrapped(isScrapped)
                .build();
    }
}
