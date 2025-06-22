package com.daruda.darudaserver.domain.search.dto.response;

import java.util.List;

import com.daruda.darudaserver.domain.search.document.BoardDocument;

public record BoardSearchResponse(
	String id,
	String title,
	String content,
	String toolName,
	String createdAt,
	Long toolId,
	List<String> imageUrl
) {
	public static BoardSearchResponse from(BoardDocument doc) {
		return new BoardSearchResponse(
			doc.getId().toString(),
			doc.getTitle(),
			doc.getContent(),
			doc.getToolName(),
			doc.getCreatedAt(),
			doc.getToolId(),
			doc.getImageUrl()
		);
	}
}
