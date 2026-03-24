package com.daruda.darudaserver.domain.search.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.daruda.darudaserver.domain.comment.event.CommentCreatedEvent;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentEventHandler {

	private final CommentRepository commentRepository;
	private final BoardSearchRepository boardSearchRepository;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Retryable(
		value = Exception.class,
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000)
	)
	public void handle(CommentCreatedEvent event) {
		int commentCount = commentRepository.countByBoardId(event.boardId());

		boardSearchRepository.findById(event.boardId().toString())
			.ifPresent(boardDoc -> {
				boardDoc.updateCommentCount(commentCount);
				boardSearchRepository.save(boardDoc);
			});
	}

	@Recover
	public void recover(Exception exception, CommentCreatedEvent event) {
		log.error("댓글 이벤트 최종 실패 - boardId={}", event.boardId(), exception);
	}
}
