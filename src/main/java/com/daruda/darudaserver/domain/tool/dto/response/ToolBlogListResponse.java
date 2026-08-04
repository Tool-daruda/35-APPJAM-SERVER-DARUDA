package com.daruda.darudaserver.domain.tool.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record ToolBlogListResponse(
	List<ToolBlogResponse> toolBlogs
) {
	public static ToolBlogListResponse of(List<ToolBlogResponse> toolBlogs) {
		return ToolBlogListResponse.builder()
			.toolBlogs(toolBlogs)
			.build();
	}
}
