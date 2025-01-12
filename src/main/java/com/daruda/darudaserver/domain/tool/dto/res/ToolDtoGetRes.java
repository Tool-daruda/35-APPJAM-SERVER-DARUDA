package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record ToolDtoGetRes(
        Long toolId,
        String toolName,
        String toolLogo,
        String description,
        License license,
        List<String> keywords
) {
    public static ToolDtoGetRes of(Tool tool, List<String> keywords) {
        return ToolDtoGetRes.builder()
                .toolId(tool.getToolId())
                .toolName(tool.getToolMainName())
                .toolLogo(tool.getToolLogo())
                .description(tool.getDescription())
                .license(tool.getLicense())
                .keywords(keywords)
                .build();
    }
}
