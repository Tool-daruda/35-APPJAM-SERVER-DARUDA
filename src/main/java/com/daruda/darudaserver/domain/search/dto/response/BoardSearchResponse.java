package com.daruda.darudaserver.domain.search.dto.response;

import java.util.Date;
import java.util.List;

import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

public record BoardSearchResponse(
	Long boardId,
	String toolName,
	String toolLogo,
	String author,
	String title,
	String content,
	List<String> imageUrl,
	boolean isScraped,
	Long toolId,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	Date updatedAt,
	int commentCount
) {
	public static BoardSearchResponse from(BoardDocument doc) {
		return new BoardSearchResponse(
			Long.valueOf(doc.getId()),
			doc.getToolMainName(),
			doc.getToolLogo(),
			doc.getAuthor(),
			doc.getTitle(),
			doc.getContent(),
			doc.getImageUrl(),
			doc.isScraped(),
			doc.getToolId(),
			doc.getUpdatedAt(),
			doc.getCommentCount()
		);
	}
}
