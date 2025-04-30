package com.daruda.darudaserver.domain.comment.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentResponse;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentRetrieveResponse;
import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationCollection;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

	private final CommentRepository commentRepository;
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;

	public CreateCommentResponse postComment(
		Long userId, Long boardId, CreateCommentRequest request
	) {
		// 사용자 및 게시글 유효성 검사
		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

		// 엔티티 생성 및 저장
		CommentEntity comment = CommentEntity.of(
			request.content(),
			request.photoUrl(),
			user,
			board
		);

		commentRepository.save(comment);

		// 응답 DTO 반환
		return CreateCommentResponse.of(
			comment.getId(),
			comment.getContent(),
			comment.getCreatedAt(),
			comment.getPhotoUrl(),
			user.getNickname()
		);
	}

	public void deleteComment(Long userId, Long commentId) {
		//사용자 존재 여부 검사
		userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		log.debug("사용자를 성공적으로 조회하였습니다. {}", userId);
		//댓글 존재 여부 검사 및 entity 반환
		CommentEntity commentEntity = commentRepository.findById(commentId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
		log.debug("댓글을 성공적으로 조회하였습니다. {}", commentId);
		//댓글 삭제
		commentRepository.delete(commentEntity);
	}

	public GetCommentRetrieveResponse getComments(Long boardId, int size, Long lastCommentId) {
		Long cursor = (lastCommentId == null) ? Long.MAX_VALUE : lastCommentId;
		PageRequest pageRequest = PageRequest.of(0, size + 1);

		List<CommentEntity> commentEntityList = commentRepository.findCommentsByBoardId(boardId, cursor, pageRequest);

		ScrollPaginationCollection<CommentEntity> commentCursor = ScrollPaginationCollection.of(commentEntityList,
			size);

		List<GetCommentResponse> commentResponse = commentCursor.getCurrentScrollItems().stream()
			.map(commentEntity -> GetCommentResponse.builder()
				.content(commentEntity.getContent())
				.image(commentEntity.getPhotoUrl())
				.commentId(commentEntity.getId())
				.nickname(commentEntity.getUser().getNickname())
				.updatedAt(commentEntity.getUpdatedAt())
				.build())
			.toList();

		// ScrollPaginationCollection을 이용한 페이지네이션 처리
		// 다음 페이지를 위한 커서 계산
		long nextCursor = commentCursor.isLastScroll() ? -1L : commentCursor.getNextCursor().getId();

		// ScrollPaginationDto 생성
		ScrollPaginationDto scrollPaginationDto = ScrollPaginationDto.of(commentCursor.getTotalElements(), nextCursor);

		// 최종 결과 반환
		return new GetCommentRetrieveResponse(commentResponse, scrollPaginationDto);
	}

}
