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
import com.daruda.darudaserver.global.annotation.DisableSwaggerSecurity;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tool")
@Tag(name = "tool 컨트롤러", description = "tool과 관련된 API를 처리합니다.")
public class ToolController {

	private final ToolService toolService;

	@GetMapping("/{tool-id}")
	@Operation(summary = "tool 세부정보 조회", description = "tool의 세부정보를 조회합니다.")
	public ResponseEntity<ApiResponse<ToolDetailGetRes>> getToolDetail(@AuthenticationPrincipal Long userId,
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolDetailGetRes toolDetail = toolService.getToolDetail(userId, toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolDetail, SuccessCode.SUCCESS_FETCH));
	}

	@DisableSwaggerSecurity
	@GetMapping("/{tool-id}/core-features")
	@Operation(summary = "tool 핵심 기능 조회", description = "tool의 핵심 기능을 조회합니다.")
	public ResponseEntity<ApiResponse<ToolCoreListRes>> getToolCoreFeature(
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolCoreListRes toolCore = toolService.getToolCore(toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolCore, SuccessCode.SUCCESS_FETCH));
	}

	@DisableSwaggerSecurity
	@Operation(summary = "tool 플랜 조회", description = "tool의 가격 플랜을 조회합니다.")
	@GetMapping("/{tool-id}/plans")
	public ResponseEntity<ApiResponse<PlanListRes>> getToolPlans(
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		PlanListRes plan = toolService.getPlan(toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(plan, SuccessCode.SUCCESS_FETCH));
	}

	@DisableSwaggerSecurity
	@GetMapping("/{tool-id}/alternatives")
	@Operation(summary = "대안 tool 조회", description = "해당 tool의 대안이 되는 tool을 조회합니다.")
	public ResponseEntity<ApiResponse<RelatedToolListRes>> getRelatedTool(
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		RelatedToolListRes relatedTool = toolService.getRelatedTool(toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(relatedTool, SuccessCode.SUCCESS_FETCH));
	}

	@GetMapping
	@Operation(summary = "tool 목록 조회", description = "tool의 목록을 조회합니다.")
	public ResponseEntity<ApiResponse<ToolListRes>> getToolList(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "정렬 기준", example = "popular")
		@RequestParam(defaultValue = "popular", value = "criteria") String criteria,
		@Parameter(description = "카테고리(영어)", example = "ALL")
		@RequestParam(defaultValue = "ALL", value = "category") String category,
		@Parameter(description = "조회할 tool의 개수", example = "18")
		@RequestParam(value = "size", defaultValue = "18") int size,
		@Parameter(description = "조회했을 때 마지막 tool Id", example = "18")
		@RequestParam(value = "lastToolId", required = false) Long lastToolId,
		@Parameter(description = "무료 tool 여부", example = "true")
		@RequestParam(value = "isFree") Boolean isFree
	) {
		ToolListRes toolListRes = toolService
			.getToolList(userId, criteria, category, size, lastToolId, isFree);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolListRes, SuccessCode.SUCCESS_FETCH));
	}

	@DisableSwaggerSecurity
	@GetMapping("/category")
	@Operation(summary = "tool 카테고리 조회", description = "tool의 카테고리를 조회합니다.")
	public ResponseEntity<ApiResponse<List<CategoryRes>>> getAllCategories() {
		List<CategoryRes> categoryRes = Arrays.stream(Category.values())
			.map(CategoryRes::from)
			.collect(Collectors.toList());
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(categoryRes, SuccessCode.SUCCESS_FETCH));
	}

	@PostMapping("/{tool-id}/scrap")
	@Operation(summary = "tool 찜하기", description = "tool을 찜하거나, 찜을 해제합니다.")
	public ResponseEntity<ApiResponse<ToolScrapRes>> postToolScrap(@AuthenticationPrincipal Long userId,
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolScrapRes toolScrapRes = toolService.postToolScrap(userId, toolId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolScrapRes, SuccessCode.SUCCESS_CREATE));
	}
}
