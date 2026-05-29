package com.daruda.darudaserver.domain.user.dto.response;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ScrapBoardsResponse(
	Long boardId,
	String title,
	String content,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	Timestamp updatedAt,
	String toolName,
	String toolLogo,
	Boolean isScrapped
) {
	public static ScrapBoardsResponse of(Long boardId, String title, String content, Timestamp updatedAt,
		String toolName, String toolLogo, Boolean isScrapped) {
		return ScrapBoardsResponse.builder()
			.boardId(boardId)
			.title(title)
			.content(content)
			.updatedAt(updatedAt)
			.toolName(toolName)
			.toolLogo(toolLogo)
			.isScrapped(isScrapped)
			.build();
	}
}
