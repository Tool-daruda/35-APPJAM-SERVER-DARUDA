package com.daruda.darudaserver.domain.comment.dto.response;

import java.sql.Timestamp;

import lombok.Builder;

@Builder
public record GetCommentResponse(
	String content,
	Long commentId,
	String nickname,
	String image,
	Timestamp updatedAt
) {

	public static GetCommentResponse of(String content, Long commentId, String nickname, String image,
		Timestamp updatedAt) {
		return GetCommentResponse.builder()
			.content(content)
			.commentId(commentId)
			.nickname(nickname)
			.image(image)
			.updatedAt(updatedAt)
			.build();
	}
}
