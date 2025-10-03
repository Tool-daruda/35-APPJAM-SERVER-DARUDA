package com.daruda.darudaserver.domain.comment.dto.response;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record CreateCommentResponse(
	Long commentId,
	String content,
	String image,
	String nickname,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	Timestamp updatedAt
) {

	public static CreateCommentResponse of(
		Long commentId, String content, Timestamp updatedAt, String image, String nickname
	) {
		return CreateCommentResponse.builder()
			.commentId(commentId)
			.content(content)
			.image(image)
			.nickname(nickname)
			.updatedAt(updatedAt)
			.build();
	}
}
