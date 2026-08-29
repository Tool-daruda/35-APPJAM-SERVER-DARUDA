package com.daruda.darudaserver.domain.community.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.daruda.darudaserver.domain.comment.service.CommentService;
import com.daruda.darudaserver.domain.community.dto.response.BoardScrapResponse;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.community.repository.ScrapBoardProjection;
import com.daruda.darudaserver.domain.community.util.ValidateBoard;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.domain.user.dto.response.ScrapBoardsRetrieveResponse;
import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BoardScrapServiceTest {

	@InjectMocks
	private BoardScrapService boardScrapService;

	@Mock
	private BoardScrapRepository boardScrapRepository;

	@Mock
	private BoardRepository boardRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BoardSearchRepository boardSearchRepository;

	@Mock
	private ValidateBoard validateBoard;

	@Mock
	private BoardScrapInternalService boardScrapInternalService;

	@Mock
	private BoardImageService boardImageService;

	@Mock
	private CommentService commentService;

	@Test
	@DisplayName("게시글 스크랩 토글 - 새로운 스크랩 생성 성공")
	void toggleScrap_Create_Success() {
		// given
		Long userId = 1L;
		Long boardId = 1L;
		User user = mock(User.class);
		Board board = mock(Board.class);
		BoardScrap boardScrap = BoardScrap.builder().user(user).board(board).build();

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(boardRepository.findByIdAndDelYn(boardId, false)).willReturn(Optional.of(board));
		given(boardScrapRepository.existsByUserIdAndBoardId(userId, boardId)).willReturn(false);
		given(boardScrapInternalService.saveIfAbsent(any(BoardScrap.class))).willReturn(true);
		given(boardSearchRepository.findById(boardId.toString())).willReturn(Optional.empty());

		// when
		BoardScrapResponse result = boardScrapService.toggleScrap(userId, boardId);

		// then
		assertThat(result.scrap()).isTrue();
		verify(boardScrapInternalService).saveIfAbsent(any(BoardScrap.class));
		verify(boardSearchRepository).findById(boardId.toString());
	}

	@Test
	@DisplayName("게시글 스크랩 토글 - 기존 스크랩 취소(삭제) 성공")
	void toggleScrap_Delete_Success() {
		// given
		Long userId = 1L;
		Long boardId = 1L;
		User user = mock(User.class);
		Board board = mock(Board.class);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(boardRepository.findByIdAndDelYn(boardId, false)).willReturn(Optional.of(board));
		given(boardScrapRepository.existsByUserIdAndBoardId(userId, boardId)).willReturn(true);
		given(boardSearchRepository.findById(boardId.toString())).willReturn(Optional.empty());

		// when
		BoardScrapResponse result = boardScrapService.toggleScrap(userId, boardId);

		// then
		assertThat(result.scrap()).isFalse();
		verify(boardScrapRepository).deleteByUserIdAndBoardId(userId, boardId);
		verify(boardSearchRepository).findById(boardId.toString());
	}

	@Test
	@DisplayName("스크랩 여부 확인 - userId가 존재하고 스크랩한 경우")
	void isScraped_UserIdExistsAndScraped() {
		// given
		User user = mock(User.class);
		Board board = mock(Board.class);
		given(user.getId()).willReturn(1L);
		given(board.getId()).willReturn(1L);
		given(boardScrapRepository.existsByUserIdAndBoardId(1L, 1L)).willReturn(true);

		// when
		boolean result = boardScrapService.isScraped(user, board);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("스크랩 여부 확인 - userId가 null인 경우")
	void isScraped_UserIdNull() {
		// given
		User user = null;
		Board board = mock(Board.class);
		given(board.getId()).willReturn(1L);

		// when
		boolean result = boardScrapService.isScraped(user, board);

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("즐겨찾기(스크랩) 게시글 목록 조회 성공")
	void getScrapBoards_Success() {
		// given
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		ScrapBoardProjection projection = mock(ScrapBoardProjection.class);
		given(projection.getBoardId()).willReturn(1L);
		given(projection.getTitle()).willReturn("Test Title");
		given(projection.getContent()).willReturn("Test Content");
		given(projection.getUpdatedAt()).willReturn(LocalDateTime.now());
		given(projection.getAuthor()).willReturn("작성자");
		given(projection.getToolName()).willReturn("ToolName");
		given(projection.getToolLogo()).willReturn("ToolLogo");
		given(projection.getToolId()).willReturn(7L);
		given(projection.getScrapCount()).willReturn(5L);

		Page<ScrapBoardProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);

		doNothing().when(validateBoard).validateUser(userId);
		given(boardScrapRepository.findScrapBoardsWithCount(userId, pageable)).willReturn(projectionPage);
		given(boardImageService.getBoardImageUrls(1L)).willReturn(List.of("https://image1.png"));
		given(commentService.getCommentCountMap(anyList())).willReturn(Map.of(1L, 3L));

		// when
		ScrapBoardsRetrieveResponse result = boardScrapService.getScrapBoards(userId, pageable);

		// then
		assertThat(result.userId()).isEqualTo(userId);
		assertThat(result.boardList()).hasSize(1);
		assertThat(result.boardList().get(0).boardId()).isEqualTo(1L);
		assertThat(result.boardList().get(0).title()).isEqualTo("Test Title");
		assertThat(result.boardList().get(0).author()).isEqualTo("작성자");
		assertThat(result.boardList().get(0).images()).containsExactly("https://image1.png");
		assertThat(result.boardList().get(0).commentCount()).isEqualTo(3);
		assertThat(result.boardList().get(0).isScraped()).isTrue();
		assertThat(result.boardList().get(0).toolId()).isEqualTo(7L);
		assertThat(result.boardList().get(0).scrapCount()).isEqualTo(5L);
		assertThat(result.pageInfo().totalPages()).isEqualTo(1);
	}

	@Test
	@DisplayName("즐겨찾기(스크랩) 게시글 목록 조회 - 자유 게시글은 toolName이 자유, toolId는 null")
	void getScrapBoards_FreeBoard() {
		// given
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		ScrapBoardProjection projection = mock(ScrapBoardProjection.class);
		given(projection.getBoardId()).willReturn(2L);
		given(projection.getTitle()).willReturn("자유 글");
		given(projection.getContent()).willReturn("자유 내용");
		given(projection.getUpdatedAt()).willReturn(LocalDateTime.now());
		given(projection.getAuthor()).willReturn("자유작성자");
		given(projection.getToolName()).willReturn(null);
		given(projection.getToolLogo()).willReturn(null);
		given(projection.getToolId()).willReturn(null);
		given(projection.getScrapCount()).willReturn(0L);

		Page<ScrapBoardProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);

		doNothing().when(validateBoard).validateUser(userId);
		given(boardScrapRepository.findScrapBoardsWithCount(userId, pageable)).willReturn(projectionPage);
		given(boardImageService.getBoardImageUrls(2L)).willReturn(List.of());
		given(commentService.getCommentCountMap(anyList())).willReturn(Map.of());

		// when
		ScrapBoardsRetrieveResponse result = boardScrapService.getScrapBoards(userId, pageable);

		// then
		assertThat(result.boardList().get(0).toolName()).isEqualTo("자유");
		assertThat(result.boardList().get(0).toolId()).isNull();
		assertThat(result.boardList().get(0).author()).isEqualTo("자유작성자");
		assertThat(result.boardList().get(0).isScraped()).isTrue();
	}
}
