package com.daruda.darudaserver.domain.tool.dto.response;

import com.daruda.darudaserver.domain.tool.entity.ToolBlog;

import lombok.Builder;

@Builder
public record ToolBlogResponse(
	Long blogId,
	String blogUrl,
	String title,
	String thumbnailUrl,
	String summary,
	String siteName,
	String faviconUrl
) {
	public static ToolBlogResponse from(ToolBlog toolBlog) {
		return ToolBlogResponse.builder()
			.blogId(toolBlog.getBlogId())
			.blogUrl(toolBlog.getBlogUrl())
			.title(toolBlog.getTitle())
			.thumbnailUrl(toolBlog.getThumbnailUrl())
			.summary(toolBlog.getSummary())
			.siteName(toolBlog.getSiteName())
			.faviconUrl(toolBlog.getFaviconUrl())
			.build();
	}
}
