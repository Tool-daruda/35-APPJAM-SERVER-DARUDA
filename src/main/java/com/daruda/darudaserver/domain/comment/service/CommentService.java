package com.daruda.darudaserver.domain.comment.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentResponse;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentRetrieveResponse;
import com.daruda.darudaserver.domain.comment.entity.Comment;
import com.daruda.darudaserver.domain.comment.event.CommentCreatedEvent;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.notification.repository.NotificationRepository;
import com.daruda.darudaserver.domain.notification.service.NotificationService;
import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationCollection;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.ForbiddenException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentService {

	private final CommentRepository commentRepository;
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final NotificationRepository notificationRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public CreateCommentResponse postComment(
		Long userId, Long boardId, CreateCommentRequest request
	) {
		// 사용자 및 게시글 유효성 검사
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		// 제재 상태 확인
		if (user.isSuspended()) {
			throw new ForbiddenException(ErrorCode.USER_SUSPENDED);
		}

		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

		// 엔티티 생성 및 저장
		Comment comment = Comment.of(
			request.content(),
			request.photoUrl(),
			user,
			board
		);

		commentRepository.save(comment);
		notificationService.sendCommentNotification(comment);

		//BoardDocument commentCount 업데이트 및 색인
		eventPublisher.publishEvent(
			new CommentCreatedEvent(comment.getId(), boardId)
		);

		// 응답 DTO 반환
		return CreateCommentResponse.of(
			comment.getId(),
			comment.getContent(),
			comment.getCreatedAt(),
			comment.getPhotoUrl(),
			user.getNickname()
		);
	}

	public GetCommentRetrieveResponse getComments(Long boardId, int size, Long lastCommentId) {
		// 커서 계산 및 댓글 조회
		Long cursor = (lastCommentId == null) ? Long.MAX_VALUE : lastCommentId;
		PageRequest pageRequest = PageRequest.of(0, size + 1);

		List<Comment> rows = commentRepository.findCommentsByBoardId(boardId, cursor, pageRequest);

		ScrollPaginationCollection<Comment> scroll = ScrollPaginationCollection.of(rows, size);

		List<Comment> currentRows = scroll.getCurrentScrollItems();

		// DTO 매핑 및 페이지 정보 생성
		List<GetCommentResponse> items = currentRows.stream()
			.map(c -> GetCommentResponse.builder()
				.commentId(c.getId())
				.content(c.getContent())
				.image(c.getPhotoUrl())
				.nickname(c.getUser().getNickname())
				.updatedAt(c.getUpdatedAt())
				.build())
			.toList();

		long nextCursor = scroll.isLastScroll()   // 마지막 페이지 여부
			? -1L
			: scroll.getNextCursor().getId();    // 다음 페이지 커서

		ScrollPaginationDto pageInfo = ScrollPaginationDto.of(items.size(), nextCursor);  // 현재 페이지 건수만 전달

		return new GetCommentRetrieveResponse(items, pageInfo);
	}

	public int getCommentCount(final Long boardId) {
		return commentRepository.findCommentsByBoardId(boardId).size();
	}

	@Transactional
	public void deleteComment(Long userId, Long commentId) {
		// 댓글 유효성 검사
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

		// 댓글을 작성한 사용자와 요청한 사용자가 일치하는지 확인
		if (!comment.getUser().getId().equals(userId)) {
			throw new ForbiddenException(ErrorCode.NO_PERMISSION_TO_DELETE);
		}

		notificationRepository.deleteAllByComment(comment);
		commentRepository.delete(comment);
	}
}
