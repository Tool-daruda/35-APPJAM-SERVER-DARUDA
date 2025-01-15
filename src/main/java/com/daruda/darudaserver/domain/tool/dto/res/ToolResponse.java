package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import lombok.AccessLevel;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record ToolResponse (
    Long toolId,
    String toolName,
    String toolLogo,
    String description,
    License license,
    List<String> keywords,
    int scrapCount, // 스크랩 수
    int popularityScore, // 인기 점수
    Timestamp createdAt // 생성일
) {
        public static ToolResponse of(Tool tool, List<String> keywords, int scrapCount, int popularityScore) {
            return ToolResponse.builder()
                    .toolId(tool.getToolId())
                    .toolName(tool.getToolMainName())
                    .toolLogo(tool.getToolLogo())
                    .description(tool.getDescription())
                    .license(tool.getLicense())
                    .keywords(keywords)
                    .scrapCount(scrapCount)
                    .popularityScore(popularityScore)
                    .createdAt(tool.getCreatedAt())
                    .build();
        }
}
