package com.daruda.darudaserver.domain.community.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record BoardResponse(
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

	public static BoardResponse of(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Long toolId) {
		return baseBuilder(board, toolName, toolLogo, commentCount, images)
			.isScraped(false)
			.toolId(toolId)
			.build();
	}

	public static BoardResponse of(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Boolean isScraped) {
		return baseBuilder(board, toolName, toolLogo, commentCount, images)
			.isScraped(isScraped)
			.build();
	}

	public static BoardResponse of(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Boolean isScraped, final Long toolId) {
		return baseBuilder(board, toolName, toolLogo, commentCount, images)
			.isScraped(isScraped)
			.toolId(toolId)
			.build();
	}

	public static BoardResponse of(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Boolean isScraped, final Long toolId,
		final Long scrapCount) {
		return baseBuilder(board, toolName, toolLogo, commentCount, images)
			.isScraped(isScraped)
			.toolId(toolId)
			.scrapCount(scrapCount)
			.build();
	}

	private static BoardResponseBuilder baseBuilder(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images) {
		return BoardResponse.builder()
			.boardId(board.getId())
			.toolName(toolName)
			.toolLogo(toolLogo)
			.author(board.getUser().getNickname())
			.title(board.getTitle())
			.content(board.getContent())
			.images(images)
			.commentCount(commentCount)
			.updatedAt(board.getUpdatedAt());
	}
}
