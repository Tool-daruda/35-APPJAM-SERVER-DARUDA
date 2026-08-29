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

	private static final int TITLE_MAX_LENGTH = 500;
	private static final int THUMBNAIL_URL_MAX_LENGTH = 2000;
	private static final int SUMMARY_MAX_LENGTH = 2000;
	private static final int SITE_NAME_MAX_LENGTH = 200;
	private static final int FAVICON_URL_MAX_LENGTH = 2000;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tool_blog_id")
	private Long id;

	@Column(name = "blog_url", nullable = false, length = 50000)
	private String blogUrl;

	@Column(name = "title", length = TITLE_MAX_LENGTH)
	private String title;

	@Column(name = "thumbnail_url", length = THUMBNAIL_URL_MAX_LENGTH)
	private String thumbnailUrl;

	@Column(name = "summary", length = SUMMARY_MAX_LENGTH)
	private String summary;

	@Column(name = "site_name", length = SITE_NAME_MAX_LENGTH)
	private String siteName;

	@Column(name = "favicon_url", length = FAVICON_URL_MAX_LENGTH)
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
			builder.title(clampText(metadata.title(), TITLE_MAX_LENGTH))
				.thumbnailUrl(clampUrl(metadata.thumbnailUrl(), THUMBNAIL_URL_MAX_LENGTH))
				.summary(clampText(metadata.summary(), SUMMARY_MAX_LENGTH))
				.siteName(clampText(metadata.siteName(), SITE_NAME_MAX_LENGTH))
				.faviconUrl(clampUrl(metadata.faviconUrl(), FAVICON_URL_MAX_LENGTH));
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
		this.title = clampText(metadata.title(), TITLE_MAX_LENGTH);
		this.thumbnailUrl = clampUrl(metadata.thumbnailUrl(), THUMBNAIL_URL_MAX_LENGTH);
		this.summary = clampText(metadata.summary(), SUMMARY_MAX_LENGTH);
		this.siteName = clampText(metadata.siteName(), SITE_NAME_MAX_LENGTH);
		this.faviconUrl = clampUrl(metadata.faviconUrl(), FAVICON_URL_MAX_LENGTH);
	}

	public boolean needsMetadataBackfill() {
		return this.metadataFetchedAt == null;
	}

	// 컬럼 길이를 초과한 텍스트는 잘라서 저장한다(플러시 시 DB 에러 방지).
	private static String clampText(final String value, final int max) {
		if (value == null) {
			return null;
		}
		return value.length() > max ? value.substring(0, max) : value;
	}

	// 컬럼 길이를 초과한 URL 은 잘라 쓰면 깨진 링크가 되므로 통째로 버린다.
	private static String clampUrl(final String value, final int max) {
		return (value == null || value.length() > max) ? null : value;
	}
}
