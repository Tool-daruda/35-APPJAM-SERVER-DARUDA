package com.daruda.darudaserver.domain.search.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daruda.darudaserver.domain.search.dto.response.GetBoardDocumentResponse;
import com.daruda.darudaserver.domain.search.dto.response.ToolSearchResponse;
import com.daruda.darudaserver.domain.search.service.BoardSearchService;
import com.daruda.darudaserver.domain.search.service.ToolSearchService;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.dto.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {
	private final BoardSearchService boardSearchService;
	private final ToolSearchService toolSearchService;

	@GetMapping("/board")
	@Operation(summary = "게시글 검색", description = "게시글 검색을 합니다")
	public ResponseEntity<SuccessResponse<?>> searchBoard(
		@RequestParam(name = "keyword") @NotBlank(message = "검색어는 필수 입력값입니다.") String keyword,
		@RequestParam(name = "nextCursor", required = false) String nextCursor,
		@RequestParam(name = "size", defaultValue = "10") int size
	) {
		GetBoardDocumentResponse getBoardDocumentResponse = boardSearchService.searchByTitleAndContentAndTool(keyword,
			nextCursor, size);

		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, getBoardDocumentResponse));
	}

	@GetMapping("/tool")
	@Operation(summary = "툴을 검색합니다", description = "툴 검색을 합니다")
	public ResponseEntity<SuccessResponse<?>> searchTool(
		@RequestParam(name = "keyword") @NotBlank(message = "검색어는 필수 입력값입니다.") String keyword,
		@AuthenticationPrincipal Long userId) {
		List<ToolSearchResponse> toolSearchResponses = toolSearchService.searchByName(keyword, userId);

		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, toolSearchResponses));
	}
}
