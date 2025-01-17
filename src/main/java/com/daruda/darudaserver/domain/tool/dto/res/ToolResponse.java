package com.daruda.darudaserver.domain.tool.dto.res;


import com.daruda.darudaserver.domain.tool.entity.Tool;

import lombok.Builder;
import java.util.List;

@Builder
public record ToolResponse (
    Long toolId,
    String toolName,
    String toolLogo,
    String description,
    String license,
    List<String> keywords,
    Boolean isScraped
) {
        public static ToolResponse of(Tool tool, List<String> keywords,Boolean isScraped) {
            return ToolResponse.builder()
                    .toolId(tool.getToolId())
                    .toolName(tool.getToolMainName())
                    .toolLogo(tool.getToolLogo())
                    .description(tool.getDescription())
                    .license(tool.getLicense().getKoreanName())
                    .keywords(keywords)
                    .isScraped(isScraped)
                    .build();
        }
}
