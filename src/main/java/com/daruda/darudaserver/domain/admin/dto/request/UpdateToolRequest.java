package com.daruda.darudaserver.domain.admin.dto.request;

import com.daruda.darudaserver.domain.tool.entity.Plan;
import com.daruda.darudaserver.domain.tool.entity.ToolCore;
import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateToolRequest(
	String toolMainName,
	String toolSubName,
	String category,
	String toolLink,
	String description,
	String license,
	Boolean supportKorea,
	String detailDescription,
	String planLink,
	String bgColor,
	Boolean fontColor,
	String toolLogo,
	@Nullable ToolPlatForm platform,
	List<String> keywords,
	List<ToolCore> cores,
	List<Plan> plans,
	List<String> images,
	List<String> videos,
	List<Integer> relatedToolIds
) {
}


