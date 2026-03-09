package com.daruda.darudaserver.domain.comment.event;

public record CommentCreatedEvent(
	Long commentId,
	Long boardId
) {

}
