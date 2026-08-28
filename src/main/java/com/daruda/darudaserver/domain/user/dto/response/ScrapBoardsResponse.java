package com.daruda.darudaserver.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ScrapBoardsResponse(
	Long boardId,
	String toolName,
	String toolLogo,
	String author,
	String title,
	String content,
	List<String> images,
	Boolean isScraped,
	Long toolId,
	Long scrapCount,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	LocalDateTime updatedAt,
	int commentCount
) {
	public static ScrapBoardsResponse of(Long boardId, String toolName, String toolLogo, String author, String title,
		String content, List<String> images, Boolean isScraped, Long toolId, Long scrapCount, LocalDateTime updatedAt,
		int commentCount) {
		return ScrapBoardsResponse.builder()
			.boardId(boardId)
			.toolName(toolName)
			.toolLogo(toolLogo)
			.author(author)
			.title(title)
			.content(content)
			.images(images)
			.isScraped(isScraped)
			.toolId(toolId)
			.scrapCount(scrapCount)
			.updatedAt(updatedAt)
			.commentCount(commentCount)
			.build();
	}
}
