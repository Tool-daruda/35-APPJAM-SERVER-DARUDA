package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Getter
@Builder
@Table(name = "tool_blog")
public class ToolBlog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tool_blog_id")
	private Long blogId;

	@Column(name = "blog_url", nullable = false, length = 50000)
	private String blogUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	public static ToolBlog create(String blogUrl, Tool tool) {
		return ToolBlog.builder()
			.blogUrl(blogUrl)
			.tool(tool)
			.build();
	}
}
