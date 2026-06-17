package com.daruda.darudaserver.domain.community.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
import com.daruda.darudaserver.domain.community.dto.res.GetBoardResponse;
import com.daruda.darudaserver.domain.community.entity.BoardSortType;
import com.daruda.darudaserver.domain.community.service.BoardScrapService;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.security.JwtAuthenticationFilter;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.SuccessCode;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest {

	@Mock
	private BoardService boardService;

	@Mock
	private BoardScrapService boardScrapService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private BoardController boardController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(boardController)
			.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
			.addFilters(new JwtAuthenticationFilter(jwtTokenProvider))
			.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("게시글 리스트 조회: sortBy 미지정 시 LATEST가 서비스에 전달된다")
	void getBoardList_defaultSort_passesLatest() throws Exception {
		// given
		GetBoardResponse mockResponse = GetBoardResponse.of(List.of(), ScrollPaginationDto.of(0L, -1L));
		when(boardService.getBoardList(any(), any(), any(), anyInt(), any(), any(BoardSortType.class), any()))
			.thenReturn(mockResponse);

		// when
		mockMvc.perform(get("/api/v1/board")
				.param("size", "10"))
			.andExpect(status().isOk());

		// then
		ArgumentCaptor<BoardSortType> sortCaptor = ArgumentCaptor.forClass(BoardSortType.class);
		verify(boardService).getBoardList(any(), any(), any(), anyInt(), any(), sortCaptor.capture(), any());
		org.assertj.core.api.Assertions.assertThat(sortCaptor.getValue()).isEqualTo(BoardSortType.LATEST);
	}

	@Test
	@DisplayName("게시글 리스트 조회: sortBy=SCRAP 지정 시 SCRAP과 lastScrapCount가 서비스에 전달된다")
	void getBoardList_scrapSort_passesScrapAndLastScrapCount() throws Exception {
		// given
		GetBoardResponse mockResponse = GetBoardResponse.of(List.of(), ScrollPaginationDto.of(0L, -1L), 3L);
		when(boardService.getBoardList(any(), any(), any(), anyInt(), any(), any(BoardSortType.class), any()))
			.thenReturn(mockResponse);

		// when
		mockMvc.perform(get("/api/v1/board")
				.param("size", "10")
				.param("sortBy", "SCRAP")
				.param("lastBoardId", "100")
				.param("lastScrapCount", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nextScrapCount").value(3));

		// then
		ArgumentCaptor<BoardSortType> sortCaptor = ArgumentCaptor.forClass(BoardSortType.class);
		ArgumentCaptor<Long> scrapCountCaptor = ArgumentCaptor.forClass(Long.class);
		verify(boardService).getBoardList(any(), any(), any(), anyInt(), any(), sortCaptor.capture(),
			scrapCountCaptor.capture());
		org.assertj.core.api.Assertions.assertThat(sortCaptor.getValue()).isEqualTo(BoardSortType.SCRAP);
		org.assertj.core.api.Assertions.assertThat(scrapCountCaptor.getValue()).isEqualTo(5L);
	}

	@Test
	@DisplayName("게시글 스크랩 토글 성공")
	void scrapBoard_success() throws Exception {
		// given
		Long userId = 1L;
		Long boardId = 1L;
		String positionEngName = Positions.STUDENT.getEngName();
		Authentication authentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		BoardScrapRes mockRes = new BoardScrapRes(boardId, true);

		when(boardScrapService.toggleScrap(userId, boardId)).thenReturn(mockRes);

		// then
		String token = "accessToken";

		mockMvc.perform(post("/api/v1/board/{board-id}/scrap", boardId)
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.boardId").value(boardId))
			.andExpect(jsonPath("$.data.scrap").value(true))
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_SCRAP.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_SCRAP.getMessage()));
	}
}
