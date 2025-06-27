package com.daruda.darudaserver.domain.search.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daruda.darudaserver.domain.search.dto.response.BoardSearchResponse;
import com.daruda.darudaserver.domain.search.dto.response.SearchAllResponse;
import com.daruda.darudaserver.domain.search.dto.response.ToolSearchResponse;
import com.daruda.darudaserver.domain.search.service.BoardSearchService;
import com.daruda.darudaserver.domain.search.service.ToolSearchService;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.dto.SuccessResponse;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {
	private final BoardSearchService boardSearchService;
	private final ToolSearchService toolSearchService;

	@GetMapping()
	public ResponseEntity<SuccessResponse<SearchAllResponse>> searchAll(
		@RequestParam(name = "keyword") @NotBlank(message = "검색어는 필수 입력값입니다.") String keyword,
		@AuthenticationPrincipal Long userId) {
		List<BoardSearchResponse> boardSearchResponses = boardSearchService.searchByTitleAndContentAndTool(keyword);
		List<ToolSearchResponse> toolSearchResponses = toolSearchService.searchByName(keyword, userId);

		SearchAllResponse searchAllResponse = SearchAllResponse.of(boardSearchResponses, toolSearchResponses);

		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, searchAllResponse));
	}
}
