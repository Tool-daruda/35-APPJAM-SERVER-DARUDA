package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.ToolKeyword;
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
}
