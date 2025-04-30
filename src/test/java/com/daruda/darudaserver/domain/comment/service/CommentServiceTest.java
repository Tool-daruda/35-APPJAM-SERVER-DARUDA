package com.daruda.darudaserver.domain.comment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentRetrieveResponse;
import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.ForbiddenException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

	@Mock
	CommentRepository commentRepository;

	@Mock
	BoardRepository boardRepository;

	@Mock
	UserRepository userRepository;

	@InjectMocks
	CommentService commentService;

	private UserEntity author;
	private UserEntity stranger;
	private Board board;
	private CommentEntity comment;

	private final Validator validator =
		Validation.buildDefaultValidatorFactory().getValidator();

	@BeforeEach
	void setUp() {
		author = UserEntity.builder()
			.email("writer@test.com")
			.nickname("작성자")
			.positions(Positions.WORKER)
			.build();
		ReflectionTestUtils.setField(author, "id", 1L);

		stranger = UserEntity.builder()
			.email("other@test.com")
			.nickname("다른 사용자")
			.positions(Positions.WORKER)
			.build();
		ReflectionTestUtils.setField(stranger, "id", 2L);

		Tool tool = Tool.builder().toolMainName("툴").build();
		board = Board.create(tool, author, "제목", "본문");
		ReflectionTestUtils.setField(board, "id", 10L);

		comment = CommentEntity.of("내용", null, author, board);
		ReflectionTestUtils.setField(comment, "id", 100L);
	}

	@Nested
	@DisplayName("댓글 생성")
	class Create {

		@Test
		@DisplayName("정상 생성")
		void create_success() {
			// given
			given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
			given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
			given(commentRepository.save(any(CommentEntity.class)))
				.willAnswer(inv -> {
					CommentEntity c = inv.getArgument(0);
					ReflectionTestUtils.setField(c, "id", 100L);
					return c;
				});

			CreateCommentRequest req = new CreateCommentRequest("내용", null);

			// when
			var result = commentService.postComment(author.getId(), board.getId(), req);

			// then
			assertThat(result.commentId()).isEqualTo(100L);
		}

		@Test
		@DisplayName("content, photoUrl이 모두 null → BadRequestException")
		void create_dto_validation_fail() {
			CreateCommentRequest req = new CreateCommentRequest(null, null);

			assertThat(validator.validate(req)).isNotEmpty();
		}

		@Test
		@DisplayName("없는 사용자 → NotFoundException")
		void create_user_not_found() {
			given(userRepository.findById(anyLong())).willReturn(Optional.empty());

			assertThatThrownBy(() ->
				commentService.postComment(99L, board.getId(),
					new CreateCommentRequest("c", null)))
				.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("댓글 조회")
	class Read {

		@Test
		@DisplayName("첫 페이지 조회")
		void read_first_page() {
			PageRequest pr = PageRequest.of(0, 11);
			given(commentRepository.findCommentsByBoardId(board.getId(), Long.MAX_VALUE, pr))
				.willReturn(List.of(comment));

			GetCommentRetrieveResponse res =
				commentService.getComments(board.getId(), 10, null);

			assertThat(res.commentList()).hasSize(1);
			assertThat(res.pageInfo().nextCursor()).isEqualTo(-1L);
		}
	}

	@Nested
	@DisplayName("댓글 삭제")
	class Delete {

		@Test @DisplayName("정상 삭제")
		void delete_success() {
			given(commentRepository.findById(comment.getId()))
				.willReturn(Optional.of(comment));

			commentService.deleteComment(author.getId(), comment.getId());

			then(commentRepository).should().delete(comment);
		}

		@Test
		@DisplayName("작성자가 아닌 사용자가 삭제 시도 → ForbiddenException")
		void delete_not_author() {
			given(commentRepository.findById(comment.getId()))
				.willReturn(Optional.of(comment));

			assertThatThrownBy(() ->
				commentService.deleteComment(stranger.getId(), comment.getId()))
				.isInstanceOf(ForbiddenException.class);

			then(commentRepository).shouldHaveNoMoreInteractions();
		}

		@Test
		@DisplayName("댓글이 없으면 NotFoundException")
		void delete_comment_not_found() {
			given(commentRepository.findById(anyLong()))
				.willReturn(Optional.empty());

			assertThatThrownBy(() ->
				commentService.deleteComment(author.getId(), 999L))
				.isInstanceOf(NotFoundException.class);
		}
	}
}
