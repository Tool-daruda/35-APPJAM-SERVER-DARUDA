package com.daruda.darudaserver.domain.community.repository;

import java.sql.Timestamp;

public interface ScrapBoardProjection {
	Long getBoardId();

	String getTitle();

	String getContent();

	Timestamp getUpdatedAt();

	String getToolName();

	String getToolLogo();

	Long getScrapCount();
}
