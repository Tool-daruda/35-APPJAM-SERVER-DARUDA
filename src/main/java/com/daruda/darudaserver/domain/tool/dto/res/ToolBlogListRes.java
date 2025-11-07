package com.daruda.darudaserver.domain.tool.dto.res;

import java.util.List;

import lombok.Builder;

@Builder
public record ToolBlogListRes(
	List<ToolBlogRes> toolBlogs
) {
	public static ToolBlogListRes of(List<ToolBlogRes> toolBlogs) {
		return ToolBlogListRes.builder()
			.toolBlogs(toolBlogs)
			.build();
	}
}
