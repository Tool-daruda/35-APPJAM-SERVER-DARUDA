package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.ToolBlog;

import lombok.Builder;

@Builder
public record ToolBlogRes(
	Long blogId,
	String blogUrl
) {
	public static ToolBlogRes from(ToolBlog toolBlog) {
		return ToolBlogRes.builder()
			.blogId(toolBlog.getBlogId())
			.blogUrl(toolBlog.getBlogUrl())
			.build();
	}
}
