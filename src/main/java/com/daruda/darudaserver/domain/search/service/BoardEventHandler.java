package com.daruda.darudaserver.domain.search.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.event.BoardCreatedEvent;
import com.daruda.darudaserver.domain.community.event.BoardUpdatedEvent;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.service.BoardImageService;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardEventHandler {

	private final BoardRepository boardRepository;
	private final BoardSearchRepository boardSearchRepository;
	private final BoardImageService boardImageService;
	private final BoardService boardService;
	private final CommentRepository commentRepository;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Retryable(
		value = Exception.class,
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000)
	)
	public void indexBoard(BoardCreatedEvent event) {

		Board board = boardRepository.findById(event.boardId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

		int commentCount =
			boardService.getCommentCount(board.getId());

		BoardDocument document =
			BoardDocument.from(
				board,
				boardImageService.getBoardImageUrls(board.getId()),
				commentCount,
				false
			);

		boardSearchRepository.save(document);
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Retryable(
		value = Exception.class,
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000)
	)
	public void updateBoardIndex(BoardUpdatedEvent event) {

		Board board = boardRepository.findById(event.boardId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

		int commentCount =
			commentRepository.countByBoardId(board.getId());

		BoardDocument document =
			BoardDocument.from(
				board,
				boardImageService.getBoardImageUrls(board.getId()),
				commentCount,
				false
			);

		boardSearchRepository.save(document);
	}

	@Recover
	public void recover(Exception exception, BoardCreatedEvent event) {
		log.error("Board ES 색인 최종 실패 - boardId={}",
			event.boardId(), exception);
	}
}
