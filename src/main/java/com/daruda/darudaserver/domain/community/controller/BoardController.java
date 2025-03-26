package com.daruda.darudaserver.domain.community.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.domain.community.dto.req.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.res.BoardRes;
import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
import com.daruda.darudaserver.domain.community.dto.res.GetBoardResponse;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteBoardsRetrieveResponse;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BoardController {

	private final BoardService boardService;

	/**
	 * 게시글 작성
	 */
	@PostMapping("/boards")
	public ResponseEntity<ApiResponse<?>> createBoard(
		@UserId Long userId,
		@ModelAttribute @Valid BoardCreateAndUpdateReq boardCreateAndUpdateReq,
		@RequestPart(value = "images", required = false) @Size(max = 5) List<MultipartFile> images) {

		BoardRes boardRes = boardService.createBoard(userId, boardCreateAndUpdateReq, images);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes, SuccessCode.SUCCESS_CREATE));
	}

	/**
	 * 게시글 수정
	 */
	@PatchMapping("/boards/{board-id}")
	public ResponseEntity<ApiResponse<?>> updateBoard(
		@UserId Long userId,
		@PathVariable(name = "board-id") final Long boardId,
		@ModelAttribute @Valid final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
		@RequestPart(value = "images", required = false) @Size(max = 5) List<MultipartFile> images) {
		BoardRes boardRes = boardService.updateBoard(userId, boardId, boardCreateAndUpdateReq, images);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes, SuccessCode.SUCCESS_UPDATE));
	}

	/**
	 * 게시글 조회
	 */
	@GetMapping("/boards/board/{board-id}")
	public ResponseEntity<ApiResponse<?>> getBoard(
		@AuthenticationPrincipal Long userIdOrNull,
		@PathVariable(name = "board-id") final Long boardId) {
		BoardRes boardRes = boardService.getBoard(userIdOrNull, boardId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes, SuccessCode.SUCCESS_FETCH));
	}

	/**
	 * 게시글 삭제
	 */
	@DeleteMapping("/boards/{board-id}")
	public ResponseEntity<ApiResponse<?>> deleteBoard(
		@UserId Long userId,
		@PathVariable(name = "board-id") final Long boardId) {
		boardService.deleteBoard(userId, boardId);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_DELETE));
	}

	/**
	 * 게시글 스크랩
	 */
	@PostMapping("/users/boards/{board-id}/scrap")
	public ResponseEntity<ApiResponse<?>> scrapBoard(
		@UserId Long userId,
		@PathVariable(name = "board-id") final Long boardId) {
		BoardScrapRes boardScrapRes = boardService.postScrap(userId, boardId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardScrapRes, SuccessCode.SUCCESS_SCRAP));
	}

	/**
	 * 게시글 리스트 조회
	 */
	@GetMapping("/boards/board/list")
	public ResponseEntity<ApiResponse<?>> getBoardList(
		@AuthenticationPrincipal Long userIdOrNull,
		@RequestParam(name = "noTopic", required = false) Boolean noTopic,
		@RequestParam(name = "toolId", required = false) Long toolId,
		@RequestParam(value = "size", defaultValue = "10") int size,
		@RequestParam(value = "lastBoardId", required = false) Long lastBoardId) {
		GetBoardResponse boardResponse = boardService.getBoardList(userIdOrNull, noTopic, toolId, size, lastBoardId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardResponse, SuccessCode.SUCCESS_FETCH));
	}

	// 관심있는 글 조회
	@GetMapping("/users/profile/boards/scrap")
	public ResponseEntity<?> getFavoriteBoards(@AuthenticationPrincipal Long userIdOrNull,
		@RequestParam(defaultValue = "1", value = "page") int pageNo,
		@RequestParam(defaultValue = "5", value = "size") int size,
		@RequestParam(defaultValue = "createdAt", value = "criteria") String criteria) {
		Pageable pageable = PageRequest.of(pageNo - 1, size, Sort.by(Sort.Direction.DESC, criteria));
		FavoriteBoardsRetrieveResponse favoriteBoardsRetrieveResponse = boardService.getFavoriteBoards(userIdOrNull,
			pageable);

		return ResponseEntity.ok(
			ApiResponse.ofSuccessWithData(favoriteBoardsRetrieveResponse, SuccessCode.SUCCESS_FETCH));
	}
}
