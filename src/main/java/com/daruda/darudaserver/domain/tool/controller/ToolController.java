package com.daruda.darudaserver.domain.tool.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daruda.darudaserver.domain.tool.dto.res.CategoryRes;
import com.daruda.darudaserver.domain.tool.dto.res.PlanListRes;
import com.daruda.darudaserver.domain.tool.dto.res.RelatedToolListRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolCoreListRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolDetailGetRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolListRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolScrapRes;
import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ToolController {

	private final ToolService toolService;

	/**
	 * 툴 세부정보 조회
	 */
	@GetMapping("/tools/{tool-id}")
	public ResponseEntity<ApiResponse<?>> getToolDetail(@AuthenticationPrincipal Long userIdOrNull,
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolDetailGetRes toolDetail = toolService.getToolDetail(userIdOrNull, toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolDetail, SuccessCode.SUCCESS_FETCH));
	}

	/**
	 * 툴 핵심 기능 조회
	 */
	@GetMapping("/tools/{tool-id}/core-features")
	public ResponseEntity<ApiResponse<?>> getToolCoreFeature(@PathVariable(name = "tool-id") final Long toolId) {
		ToolCoreListRes toolCore = toolService.getToolCore(toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolCore, SuccessCode.SUCCESS_FETCH));
	}

	/**
	 * 툴- 플랜 조회
	 */
	@GetMapping("/tools/{tool-id}/plans")
	public ResponseEntity<ApiResponse<?>> getToolPlans(@PathVariable(name = "tool-id") final Long toolId) {
		PlanListRes plan = toolService.getPlan(toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(plan, SuccessCode.SUCCESS_FETCH));
	}

	/**
	 * 대안툴 조회
	 */
	@GetMapping("/tools/{tool-id}/related-tool")
	public ResponseEntity<ApiResponse<?>> getRelatedTool(@PathVariable(name = "tool-id") final Long toolId) {
		RelatedToolListRes relatedTool = toolService.getRelatedTool(toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(relatedTool, SuccessCode.SUCCESS_FETCH));
	}

	/**
	 * 툴 리스트 조회
	 */
	@GetMapping("/tools")
	public ResponseEntity<ApiResponse<?>> getToolList(
		@AuthenticationPrincipal Long userIdOrNull,
		@RequestParam(defaultValue = "popular", value = "criteria") String criteria,
		@RequestParam(defaultValue = "ALL", value = "category") String category,
		@RequestParam(value = "size", defaultValue = "18") int size,
		@RequestParam(value = "lastToolId", required = false) Long lastToolId,
		@RequestParam(value = "isFree") Boolean isFree
	) {
		ToolListRes toolListRes = toolService
			.getToolList(userIdOrNull, criteria, category, size, lastToolId, isFree);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolListRes, SuccessCode.SUCCESS_FETCH));
	}

	/**
	 * 툴 찜하기
	 */
	@PostMapping("/users/tools/{tool-id}/scrap")
	public ResponseEntity<ApiResponse<?>> postToolScrap(@UserId final Long userId,
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolScrapRes toolScrapRes = toolService.postToolScrap(userId, toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolScrapRes, SuccessCode.SUCCESS_CREATE));
	}

	/**
	 * 카테고리 조회 API
	 */
	@GetMapping("/tools/category")
	public ResponseEntity<ApiResponse<List<CategoryRes>>> getAllCategories() {
		List<CategoryRes> categoryRes = Arrays.stream(Category.values())
			.map(CategoryRes::from)
			.collect(Collectors.toList());
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(categoryRes, SuccessCode.SUCCESS_FETCH));
	}
}
