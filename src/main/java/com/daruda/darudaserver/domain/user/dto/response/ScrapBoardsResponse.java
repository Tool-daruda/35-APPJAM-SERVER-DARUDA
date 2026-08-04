package com.daruda.darudaserver.domain.user.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ScrapBoardsResponse(
	Long boardId,
	String title,
	String content,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	LocalDateTime updatedAt,
	String toolName,
	String toolLogo,
	Boolean isScrapped,
	Long scrapCount
) {
	public static ScrapBoardsResponse of(Long boardId, String title, String content, LocalDateTime updatedAt,
		String toolName, String toolLogo, Boolean isScrapped, Long scrapCount) {
		return ScrapBoardsResponse.builder()
			.boardId(boardId)
			.title(title)
			.content(content)
			.updatedAt(updatedAt)
			.toolName(toolName)
			.toolLogo(toolLogo)
			.isScrapped(isScrapped)
			.scrapCount(scrapCount)
			.build();
	}
}
