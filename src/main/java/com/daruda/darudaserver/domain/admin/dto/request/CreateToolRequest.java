package com.daruda.darudaserver.domain.admin.dto.request;

import com.daruda.darudaserver.domain.tool.entity.Plan;
import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateToolRequest(
	String toolMainName,
	String toolSubName,
	String category,
	String toolLink,
	String description,
	String license,
	boolean supportKorea,
	String detailDescription,
	String planLink,
	String bgColor,
	boolean fontColor,
	String toolLogo,
	ToolPlatForm platform,
	List<String> keywords,
	List<ToolCore> cores,
	List<Plan> plans,
	List<String> images,
	List<String> videos,
	List<Integer> relatedToolIds
) {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ToolPlatform(
		boolean web,
		boolean windows,
		boolean mac
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ToolCore(
		String coreTitle,
		String coreContent
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ToolPlan(
		String planName,
		int monthlyPrice,
		int annualPrice,
		String description,
		boolean isDollar
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Image(
		String imageUrl
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Video(
		String videoUrl
	) {
	}
}
