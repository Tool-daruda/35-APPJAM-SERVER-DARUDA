package com.daruda.darudaserver.domain.comment.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record GetCommentResponse(
	String content,
	Long commentId,
	String nickname,
	String image,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	LocalDateTime updatedAt
) {

	public static GetCommentResponse of(String content, Long commentId, String nickname, String image,
		LocalDateTime updatedAt) {
		return GetCommentResponse.builder()
			.content(content)
			.commentId(commentId)
			.nickname(nickname)
			.image(image)
			.updatedAt(updatedAt)
			.build();
	}
}
