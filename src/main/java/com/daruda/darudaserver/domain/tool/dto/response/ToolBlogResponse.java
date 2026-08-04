package com.daruda.darudaserver.domain.tool.dto.response;

import com.daruda.darudaserver.domain.tool.entity.ToolBlog;

import lombok.Builder;

@Builder
public record ToolBlogResponse(
	Long blogId,
	String blogUrl
) {
	public static ToolBlogResponse from(ToolBlog toolBlog) {
		return ToolBlogResponse.builder()
			.blogId(toolBlog.getBlogId())
			.blogUrl(toolBlog.getBlogUrl())
			.build();
	}
}
