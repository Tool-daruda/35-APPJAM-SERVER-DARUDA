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

import com.daruda.darudaserver.domain.community.dto.req.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.res.BoardRes;
import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
import com.daruda.darudaserver.domain.community.dto.res.GetBoardResponse;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.global.annotation.DisableSwaggerSecurity;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

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

	@PostMapping
	@Operation(summary = "게시글 작성", description = "게시글을 작성합니다.")
	public ResponseEntity<ApiResponse<?>> createBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "작성할 게시글")
		@RequestBody @Valid BoardCreateAndUpdateReq boardCreateAndUpdateReq) {

		BoardRes boardRes = boardService.createBoard(userId, boardCreateAndUpdateReq);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes, SuccessCode.SUCCESS_CREATE));
	}

	@PatchMapping("/{board-id}")
	@Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
	public ResponseEntity<ApiResponse<?>> updateBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "board Id", example = "1")
		@PathVariable(name = "board-id") final Long boardId,
		@Parameter(description = "수정할 게시글")
		@RequestBody @Valid final BoardCreateAndUpdateReq boardCreateAndUpdateReq) {
		BoardRes boardRes = boardService.updateBoard(userId, boardId, boardCreateAndUpdateReq);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes, SuccessCode.SUCCESS_UPDATE));
	}

	@DisableSwaggerSecurity
	@GetMapping("/{board-id}")
	@Operation(summary = "게시글 조회", description = "게시글을 조회합니다.")
	public ResponseEntity<ApiResponse<?>> getBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "board Id", example = "1")
		@PathVariable(name = "board-id") final Long boardId) {
		BoardRes boardRes = boardService.getBoard(userId, boardId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes, SuccessCode.SUCCESS_FETCH));
	}

	@DeleteMapping("/{board-id}")
	@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
	public ResponseEntity<ApiResponse<?>> deleteBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "board Id", example = "1")
		@PathVariable(name = "board-id") final Long boardId) {
		boardService.deleteBoard(userId, boardId);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_DELETE));
	}

	@PostMapping("/{board-id}/scrap")
	@Operation(summary = "게시글 스크랩", description = "게시글을 스크랩합니다.")
	public ResponseEntity<ApiResponse<?>> scrapBoard(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "board Id", example = "1")
		@PathVariable(name = "board-id") final Long boardId) {
		BoardScrapRes boardScrapRes = boardService.postScrap(userId, boardId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardScrapRes, SuccessCode.SUCCESS_SCRAP));
	}

	@DisableSwaggerSecurity
	@GetMapping
	@Operation(summary = "게시글 리스트 조회", description = "게시글 리스트를 조회합니다.")
	public ResponseEntity<ApiResponse<?>> getBoardList(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "자유 게시판 게시글 여부", example = "true")
		@RequestParam(name = "noTopic", required = false) Boolean noTopic,
		@Parameter(description = "tool Id", example = "1")
		@RequestParam(name = "toolId", required = false) Long toolId,
		@Parameter(description = "조회할 게시글 개수", example = "10")
		@RequestParam(value = "size", defaultValue = "10") int size,
		@Parameter(description = "조회했을 때 마지막 board Id", example = "10")
		@RequestParam(value = "lastBoardId", required = false) Long lastBoardId) {
		GetBoardResponse boardResponse = boardService.getBoardList(userId, noTopic, toolId, size, lastBoardId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardResponse, SuccessCode.SUCCESS_FETCH));
	}
}
