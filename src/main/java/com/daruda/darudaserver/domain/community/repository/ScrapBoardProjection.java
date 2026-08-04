package com.daruda.darudaserver.domain.community.repository;

import java.time.LocalDateTime;

public interface ScrapBoardProjection {
	Long getBoardId();

	String getTitle();

	String getContent();

	LocalDateTime getUpdatedAt();

	String getToolName();

	String getToolLogo();

	Long getScrapCount();
}
