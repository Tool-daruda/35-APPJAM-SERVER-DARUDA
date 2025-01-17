package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import java.util.List;


public record ToolDtoGetRes(
        Long toolId,
        String toolName,
        String toolLogo,
        String description,
        String license,
        List<String> keywords,
        Boolean isScraped
) {
    // 정적 팩토리 메서드
    public static ToolDtoGetRes from(Tool tool, List<String> keywords,Boolean isScraped) {
        return new ToolDtoGetRes(
                tool.getToolId(),
                tool.getToolMainName(),
                tool.getToolLogo(),
                tool.getDescription(),
                tool.getLicense().getKoreanName(),
                keywords,
                isScraped
        );
    }
}
