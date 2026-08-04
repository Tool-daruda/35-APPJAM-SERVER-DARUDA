package com.daruda.darudaserver.domain.community.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BoardResponse {
	private Long boardId;
	private String toolName;
	private String toolLogo;
	private String author;
	private String title;
	private String content;
	private List<String> images;
	private Boolean isScraped;
	private Long toolId;
	private Long scrapCount;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	private LocalDateTime updatedAt;
	private int commentCount;

	public static BoardResponse of(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Long toolId) {
		return createBoardRes(board, toolName, toolLogo, commentCount, images, toolId);
	}

	public static BoardResponse of(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Boolean isScraped) {
		return getBoardRes(board, toolName, toolLogo, commentCount, images, isScraped);
	}

	public static BoardResponse of(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Boolean isScraped, final Long toolId) {
		return getBoardResWithToolId(board, toolName, toolLogo, commentCount, images, isScraped, toolId);
	}

	public static BoardResponse of(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Boolean isScraped, final Long toolId,
		final Long scrapCount) {
		return getBoardResWithToolIdAndScrapCount(board, toolName, toolLogo, commentCount, images, isScraped, toolId,
			scrapCount);
	}

	public static BoardResponse createBoardRes(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Long toolId) {
		return BoardResponse.builder()
			.boardId(board.getId())
			.toolName(toolName)
			.toolLogo(toolLogo)
			.author(board.getUser().getNickname())
			.title(board.getTitle())
			.content(board.getContent())
			.images(images)
			.commentCount(commentCount)
			.updatedAt(board.getUpdatedAt())
			.isScraped(false)
			.toolId(toolId)
			.build();
	}

	public static BoardResponse getBoardRes(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Boolean isScraped) {
		return BoardResponse.builder()
			.boardId(board.getId())
			.toolName(toolName)
			.toolLogo(toolLogo)
			.author(board.getUser().getNickname())
			.title(board.getTitle())
			.content(board.getContent())
			.images(images)
			.commentCount(commentCount)
			.updatedAt(board.getUpdatedAt())
			.isScraped(isScraped)
			.build();
	}

	public static BoardResponse getBoardResWithToolId(final Board board, final String toolName, final String toolLogo,
		final int commentCount, final List<String> images, final Boolean isScraped, final Long toolId) {
		return BoardResponse.builder()
			.boardId(board.getId())
			.toolName(toolName)
			.toolLogo(toolLogo)
			.author(board.getUser().getNickname())
			.title(board.getTitle())
			.content(board.getContent())
			.images(images)
			.commentCount(commentCount)
			.updatedAt(board.getUpdatedAt())
			.isScraped(isScraped)
			.toolId(toolId)
			.build();
	}

	public static BoardResponse getBoardResWithToolIdAndScrapCount(final Board board, final String toolName,
		final String toolLogo, final int commentCount, final List<String> images, final Boolean isScraped,
		final Long toolId, final Long scrapCount) {
		return BoardResponse.builder()
			.boardId(board.getId())
			.toolName(toolName)
			.toolLogo(toolLogo)
			.author(board.getUser().getNickname())
			.title(board.getTitle())
			.content(board.getContent())
			.images(images)
			.commentCount(commentCount)
			.updatedAt(board.getUpdatedAt())
			.isScraped(isScraped)
			.toolId(toolId)
			.scrapCount(scrapCount)
			.build();
	}

}
