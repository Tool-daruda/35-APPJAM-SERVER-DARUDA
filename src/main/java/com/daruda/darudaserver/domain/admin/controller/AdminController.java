package com.daruda.darudaserver.domain.admin.controller;

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

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolRequest;
import com.daruda.darudaserver.domain.admin.dto.request.UpdateToolRequest;
import com.daruda.darudaserver.domain.admin.dto.response.AdminToolPageRes;
import com.daruda.darudaserver.domain.admin.service.AdminService;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.dto.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "admin 컨트롤러", description = "어드민 관련 API를 처리합니다.")
public class AdminController {
	private final AdminService adminService;

	@GetMapping("/tool")
	@Operation(summary = "관리자용 툴 조회", description = "관리자용 전체 툴 조회 API입니다.")
	public ResponseEntity<SuccessResponse<AdminToolPageRes>> fetchAllTool(
		@Parameter(description = "페이지 인덱스 (0 기반)", example = "0")
		@RequestParam(value = "page", defaultValue = "0") int page,
		@Parameter(description = "페이지 크기", example = "20")
		@RequestParam(value = "size", defaultValue = "20") int size
	) {
		AdminToolPageRes res = adminService.fetchAllTool(page, size);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, res));
	}

	@PostMapping
	@Operation(summary = "관리자용 툴 추가", description = "관리자용 툴 추가 API입니다.")
	public ResponseEntity<SuccessResponse<Void>> createTool(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "추가할 툴")
		@RequestBody @Valid CreateToolRequest createToolRequest) {

		adminService.createTool(createToolRequest);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_CREATE));
	}

	@PatchMapping("/tools/{toolId}")
	@Operation(summary = "관리자용 툴 수정", description = "관리자용 툴 수정 API입니다.")
	public ResponseEntity<SuccessResponse<Void>> updateTool(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "수정할 툴 ID") @PathVariable Long toolId,
		@Parameter(description = "수정 요청 DTO") @RequestBody @Valid UpdateToolRequest request
	) {
		adminService.updateTool(toolId, request);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_UPDATE));
	}

	@DeleteMapping("/tools/{toolId}")
	@Operation(summary = "관리자용 툴 삭제", description = "관리자용 툴 삭제 API입니다.")
	public ResponseEntity<SuccessResponse<Void>> deleteTool(
		@Parameter(description = "삭제 할 툴 ID") @PathVariable Long toolId) {
		adminService.deleteTool(toolId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_DELETE));
	}
}
