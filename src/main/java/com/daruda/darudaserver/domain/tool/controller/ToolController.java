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

import com.daruda.darudaserver.domain.tool.dto.response.CategoryResponse;
import com.daruda.darudaserver.domain.tool.dto.response.PlanListResponse;
import com.daruda.darudaserver.domain.tool.dto.response.RelatedToolListResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolBlogListResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolCoreListResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolDetailGetResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolLikeResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolListResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolScrapResponse;
import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.global.annotation.DisableSwaggerSecurity;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.dto.SuccessResponse;

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

	@DisableSwaggerSecurity
	@GetMapping("/{tool-id}")
	@Operation(summary = "tool 세부정보 조회", description = "tool의 세부정보를 조회합니다.")
	public ResponseEntity<SuccessResponse<ToolDetailGetResponse>> getToolDetail(@AuthenticationPrincipal Long userId,
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolDetailGetResponse toolDetail = toolService.getToolDetail(userId, toolId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, toolDetail));
	}

	@DisableSwaggerSecurity
	@GetMapping("/{tool-id}/core-features")
	@Operation(summary = "tool 핵심 기능 조회", description = "tool의 핵심 기능을 조회합니다.")
	public ResponseEntity<SuccessResponse<ToolCoreListResponse>> getToolCoreFeature(
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolCoreListResponse toolCore = toolService.getToolCore(toolId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, toolCore));
	}

	@DisableSwaggerSecurity
	@Operation(summary = "tool 플랜 조회", description = "tool의 가격 플랜을 조회합니다.")
	@GetMapping("/{tool-id}/plans")
	public ResponseEntity<SuccessResponse<PlanListResponse>> getToolPlans(
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		PlanListResponse plan = toolService.getPlan(toolId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, plan));
	}

	@DisableSwaggerSecurity
	@GetMapping("/{tool-id}/alternatives")
	@Operation(summary = "대안 tool 조회", description = "해당 tool의 대안이 되는 tool을 조회합니다.")
	public ResponseEntity<SuccessResponse<RelatedToolListResponse>> getRelatedTool(
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		RelatedToolListResponse relatedTool = toolService.getRelatedTool(toolId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, relatedTool));
	}

	@DisableSwaggerSecurity
	@GetMapping("/{tool-id}/blogs")
	@Operation(summary = "tool 블로그 조회", description = "tool 관련 블로그를 조회합니다.")
	public ResponseEntity<SuccessResponse<ToolBlogListResponse>> getToolBlogs(
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId
	) {
		ToolBlogListResponse toolBlogRes = toolService.getBlog(toolId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, toolBlogRes));
	}

	@GetMapping
	@Operation(summary = "tool 목록 조회", description = "tool의 목록을 조회합니다.")
	public ResponseEntity<SuccessResponse<ToolListResponse>> getToolList(
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
		ToolListResponse toolListRes = toolService
			.getToolList(userId, criteria, category, size, lastToolId, isFree);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, toolListRes));
	}

	@DisableSwaggerSecurity
	@GetMapping("/category")
	@Operation(summary = "tool 카테고리 조회", description = "tool의 카테고리를 조회합니다.")
	public ResponseEntity<SuccessResponse<List<CategoryResponse>>> getAllCategories() {
		List<CategoryResponse> categoryRes = Arrays.stream(Category.values())
			.map(CategoryResponse::from)
			.collect(Collectors.toList());
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, categoryRes));
	}

	@PostMapping("/{tool-id}/scrap")
	@Operation(summary = "tool 찜하기", description = "tool을 찜하거나, 찜을 해제합니다.")
	public ResponseEntity<SuccessResponse<ToolScrapResponse>> postToolScrap(@AuthenticationPrincipal Long userId,
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolScrapResponse toolScrapRes = toolService.postToolScrap(userId, toolId);
		return ResponseEntity.status(SuccessCode.SUCCESS_CREATE.getHttpStatus())
			.body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, toolScrapRes));
	}

	@PostMapping("/{tool-id}/like")
	@Operation(summary = "tool 좋아요", description = "tool에 좋아요를 누르거나, 좋아요를 취소합니다.")
	public ResponseEntity<SuccessResponse<ToolLikeResponse>> postToolLike(@AuthenticationPrincipal Long userId,
		@Parameter(description = "tool Id", example = "1")
		@PathVariable(name = "tool-id") final Long toolId) {
		ToolLikeResponse toolLikeRes = toolService.postToolLike(userId, toolId);
		return ResponseEntity.status(SuccessCode.SUCCESS_CREATE.getHttpStatus())
			.body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, toolLikeRes));
	}
}
