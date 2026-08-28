package com.daruda.darudaserver.domain.tool.entity;

import java.time.LocalDateTime;

import com.daruda.darudaserver.global.scraper.OgMetadata;

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

	@Column(name = "title", length = 500)
	private String title;

	@Column(name = "thumbnail_url", length = 2000)
	private String thumbnailUrl;

	@Column(name = "summary", length = 2000)
	private String summary;

	@Column(name = "site_name", length = 200)
	private String siteName;

	@Column(name = "favicon_url", length = 2000)
	private String faviconUrl;

	@Column(name = "metadata_fetched_at")
	private LocalDateTime metadataFetchedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	public static ToolBlog create(final String blogUrl, final Tool tool, final OgMetadata metadata) {
		// AdminService 는 create 전에 항상 fetch() 를 시도하므로 metadata 가 null 이어도 "시도했음"으로 본다.
		ToolBlogBuilder builder = ToolBlog.builder()
			.blogUrl(blogUrl)
			.tool(tool)
			.metadataFetchedAt(LocalDateTime.now());
		if (metadata != null) {
			builder.title(metadata.title())
				.thumbnailUrl(metadata.thumbnailUrl())
				.summary(metadata.summary())
				.siteName(metadata.siteName())
				.faviconUrl(metadata.faviconUrl());
		}
		return builder.build();
	}

	/**
	 * 조회 시점 지연 백필용. 이미 영속 상태인 엔티티에 스크래핑 결과를 반영한다.
	 * 스크래핑이 실패해(metadata == null) 값이 비어도 "시도 시각"은 기록하므로 재조회 시 다시 긁지 않는다.
	 */
	public void applyMetadata(final OgMetadata metadata) {
		this.metadataFetchedAt = LocalDateTime.now();
		if (metadata == null) {
			return;
		}
		this.title = metadata.title();
		this.thumbnailUrl = metadata.thumbnailUrl();
		this.summary = metadata.summary();
		this.siteName = metadata.siteName();
		this.faviconUrl = metadata.faviconUrl();
	}

	public boolean needsMetadataBackfill() {
		return this.metadataFetchedAt == null;
	}
}
