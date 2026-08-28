package com.daruda.darudaserver.domain.community.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.community.util.ValidateBoard;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.dto.response.BoardListResponse;
import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.image.service.ImageService;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

	@InjectMocks
	private BoardService boardService;

	@Mock
	private ImageService imageService;

	@Mock
	private BoardImageService boardImageService;

	@Mock
	private BoardImageRepository boardImageRepository;

	@Mock
	private BoardRepository boardRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BoardScrapRepository boardScrapRepository;

	@Mock
	private BoardScrapService boardScrapService;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private ValidateBoard validateBoard;

	@Mock
	private Board board;

	@Test
	@DisplayName("processImages: 이미지가 있을 때 정상적으로 처리되는지 테스트")
	void processImages_WithImages_Success() throws Exception {
		// given
		Long boardId = 1L;
		List<String> imageList = List.of("image1.png", "image2.png");
		List<Long> imageIds = List.of(10L, 20L);
		List<String> imageUrls = List.of("url1", "url2");

		Mockito.when(board.getId()).thenReturn(boardId);
		Mockito.when(boardImageRepository.findAllByBoardId(boardId)).thenReturn(java.util.Collections.emptyList());
		Mockito.when(imageService.createImage(imageList)).thenReturn(imageIds);
		Mockito.when(boardImageService.getBoardImageUrls(boardId)).thenReturn(imageUrls);

		java.lang.reflect.Method method = BoardService.class.getDeclaredMethod("processImages", Board.class,
			List.class);
		method.setAccessible(true);

		// when
		@SuppressWarnings("unchecked")
		List<String> result = (List<String>)method.invoke(boardService, board, imageList);

		// then
		Mockito.verify(boardImageRepository).findAllByBoardId(boardId);
		Mockito.verify(imageService).createImage(imageList);
		Mockito.verify(boardImageService).saveBoardImages(boardId, imageIds);
		Mockito.verify(boardImageService).getBoardImageUrls(boardId);
		Assertions.assertEquals(imageUrls, result);
	}

	@Nested
	@DisplayName("작성한 게시글 목록 조회")
	class GetUserBoards {

		@Test
		@DisplayName("툴 게시글은 toolId와 scrapCount를 채워서 반환한다")
		void getUserBoards_toolBoard_fillsToolIdAndScrapCount() {
			// given
			Long userId = 1L;
			Pageable pageable = PageRequest.of(0, 5);
			User user = mock(User.class);
			Tool tool = mock(Tool.class);
			Board toolBoard = mock(Board.class);

			given(toolBoard.getId()).willReturn(10L);
			given(toolBoard.getTool()).willReturn(tool);
			given(toolBoard.getUser()).willReturn(user);
			given(user.getNickname()).willReturn("작성자");
			given(tool.getToolId()).willReturn(100L);
			given(tool.getToolMainName()).willReturn("Cursor");
			given(tool.getToolLogo()).willReturn("https://logo.png");

			given(boardRepository.findAllByUserIdAndDelYnFalse(userId, pageable))
				.willReturn(new PageImpl<>(List.of(toolBoard), pageable, 1));
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(boardScrapRepository.countMapByBoardIds(List.of(10L))).willReturn(Map.of(10L, 3L));
			given(commentRepository.findCommentsByBoardId(10L)).willReturn(List.of());
			given(boardImageService.getBoardImageUrls(10L)).willReturn(List.of());
			given(boardScrapService.isScraped(user, toolBoard)).willReturn(false);

			// when
			BoardListResponse result = boardService.getUserBoards(userId, pageable);

			// then
			assertThat(result.boardList()).hasSize(1);
			assertThat(result.boardList().get(0).getToolId()).isEqualTo(100L);
			assertThat(result.boardList().get(0).getScrapCount()).isEqualTo(3L);
		}

		@Test
		@DisplayName("자유 게시글은 toolId가 null, scrapCount는 0으로 반환한다")
		void getUserBoards_freeBoard_toolIdNullAndScrapCountZero() {
			// given
			Long userId = 1L;
			Pageable pageable = PageRequest.of(0, 5);
			User user = mock(User.class);
			Board freeBoard = mock(Board.class);

			given(freeBoard.getId()).willReturn(20L);
			given(freeBoard.getTool()).willReturn(null);
			given(freeBoard.getUser()).willReturn(user);
			given(user.getNickname()).willReturn("작성자");

			given(boardRepository.findAllByUserIdAndDelYnFalse(userId, pageable))
				.willReturn(new PageImpl<>(List.of(freeBoard), pageable, 1));
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(boardScrapRepository.countMapByBoardIds(List.of(20L))).willReturn(Map.of());
			given(commentRepository.findCommentsByBoardId(20L)).willReturn(List.of());
			given(boardImageService.getBoardImageUrls(20L)).willReturn(List.of());
			given(boardScrapService.isScraped(user, freeBoard)).willReturn(false);

			// when
			BoardListResponse result = boardService.getUserBoards(userId, pageable);

			// then
			assertThat(result.boardList()).hasSize(1);
			assertThat(result.boardList().get(0).getToolId()).isNull();
			assertThat(result.boardList().get(0).getScrapCount()).isEqualTo(0L);
		}
	}
}
