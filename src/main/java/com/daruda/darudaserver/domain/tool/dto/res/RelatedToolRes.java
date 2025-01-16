package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import lombok.AccessLevel;
import lombok.Builder;
import java.util.List;

@Builder
public record RelatedToolRes (
        Long toolId,
        String toolName,
        String toolLogo,
        String license,
        List<String> keywords
){

    public static RelatedToolRes of(Tool tool,List<String> keywords) {
        return RelatedToolRes.builder()
                .toolId(tool.getToolId())
                .toolName(tool.getToolMainName())
                .toolLogo(tool.getToolLogo())
                .license(tool.getLicense().getKoreanName())
                .keywords(keywords)
                .build();
    }
}
