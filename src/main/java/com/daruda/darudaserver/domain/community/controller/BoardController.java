package com.daruda.darudaserver.domain.community.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daruda.darudaserver.domain.community.dto.request.BoardCreateAndUpdateRequest;
import com.daruda.darudaserver.domain.community.dto.response.BoardResponse;
import com.daruda.darudaserver.domain.community.dto.response.BoardScrapResponse;
import com.daruda.darudaserver.domain.community.dto.response.GetBoardResponse;
import com.daruda.darudaserver.domain.community.entity.BoardSortType;
import com.daruda.darudaserver.domain.community.service.BoardScrapService;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.global.annotation.DisableSwaggerSecurity;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.dto.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
@Tag(name = "board 컨트롤러", description = "게시판과 관련된 API를 처리합니다.")
public class BoardController {
	private final BoardService boardService;
	private final BoardScrapService boardScrapService;

	@PostMapping
	@Operation(summary = "게시글 작성", description = "게시글을 작성합니다.")
	public ResponseEntity<SuccessResponse<BoardResponse>> createBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "작성할 게시글")
		@RequestBody @Valid BoardCreateAndUpdateRequest boardCreateAndUpdateReq) {

		BoardResponse boardRes = boardService.createBoard(userId, boardCreateAndUpdateReq);
		return ResponseEntity.status(SuccessCode.SUCCESS_CREATE.getHttpStatus())
			.body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, boardRes));
	}

	@PatchMapping("/{board-id}")
	@Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
	public ResponseEntity<SuccessResponse<BoardResponse>> updateBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "board Id", example = "1")
		@PathVariable(name = "board-id") final Long boardId,
		@Parameter(description = "수정할 게시글")
		@RequestBody @Valid final BoardCreateAndUpdateRequest boardCreateAndUpdateReq) {
		BoardResponse boardRes = boardService.updateBoard(userId, boardId, boardCreateAndUpdateReq);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_UPDATE, boardRes));
	}

	@DisableSwaggerSecurity
	@GetMapping("/{board-id}")
	@Operation(summary = "게시글 조회", description = "게시글을 조회합니다.")
	public ResponseEntity<SuccessResponse<BoardResponse>> getBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "board Id", example = "1")
		@PathVariable(name = "board-id") final Long boardId) {
		BoardResponse boardRes = boardService.getBoard(userId, boardId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, boardRes));
	}

	@DeleteMapping("/{board-id}")
	@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
	public ResponseEntity<SuccessResponse<Void>> deleteBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "board Id", example = "1")
		@PathVariable(name = "board-id") final Long boardId) {
		boardService.deleteBoard(userId, boardId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_DELETE));
	}

	@PostMapping("/{board-id}/scrap")
	@Operation(summary = "게시글 스크랩", description = "게시글을 스크랩합니다.")
	public ResponseEntity<SuccessResponse<BoardScrapResponse>> scrapBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "board Id", example = "1")
		@PathVariable(name = "board-id") final Long boardId) {
		BoardScrapResponse boardScrapRes = boardScrapService.toggleScrap(userId, boardId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_SCRAP, boardScrapRes));
	}

	@DisableSwaggerSecurity
	@GetMapping
	@Operation(summary = "게시글 리스트 조회", description = "게시글 리스트를 조회합니다.")
	public ResponseEntity<SuccessResponse<GetBoardResponse>> getBoardList(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "자유 게시판 게시글 여부", example = "true")
		@RequestParam(name = "noTopic", required = false) Boolean noTopic,
		@Parameter(description = "tool Id", example = "1")
		@RequestParam(name = "toolId", required = false) Long toolId,
		@Parameter(description = "조회할 게시글 개수", example = "10")
		@RequestParam(value = "size", defaultValue = "10") int size,
		@Parameter(description = "조회했을 때 마지막 board Id", example = "10")
		@RequestParam(value = "lastBoardId", required = false) Long lastBoardId,
		@Parameter(description = "정렬 기준 (LATEST: 최신순, SCRAP: 스크랩 많은 순)", example = "LATEST")
		@RequestParam(value = "sortBy", required = false) String sortBy,
		@Parameter(description = "스크랩 정렬 시 마지막 게시글의 스크랩 수 (sortBy=SCRAP일 때 필요)", example = "5")
		@RequestParam(value = "lastScrapCount", required = false) Long lastScrapCount) {
		BoardSortType sortType = BoardSortType.from(sortBy);
		GetBoardResponse boardResponse = boardService.getBoardList(userId, noTopic, toolId, size, lastBoardId, sortType,
			lastScrapCount);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, boardResponse));
	}
}
