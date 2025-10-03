package com.daruda.darudaserver.domain.admin.controller;

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolRequest;
import com.daruda.darudaserver.domain.admin.dto.request.UpdateToolRequest;
import com.daruda.darudaserver.domain.admin.service.AdminService;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "admin 컨트롤러", description = "어드민 관련 API를 처리합니다.")
public class AdminController {
	private final AdminService adminService;

	@PostMapping
	@Operation(summary = "관리자용 툴 추가", description = "관리자용 툴 추가 API입니다.")
	public ResponseEntity<SuccessResponse<?>> createTool(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "추가할 툴")
		@RequestBody @Valid CreateToolRequest createToolRequest) {

		adminService.createTool(createToolRequest);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_CREATE));
	}

	@PatchMapping("/tools/{toolId}")
	@Operation(summary = "관리자용 툴 수정", description = "관리자용 툴 수정 API입니다.")
	public ResponseEntity<SuccessResponse<?>> updateTool(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "수정할 툴 ID") @PathVariable Long toolId,
		@Parameter(description = "수정 요청 DTO") @RequestBody @Valid UpdateToolRequest request
	) {
		adminService.updateTool(toolId, request);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_UPDATE));
	}

}
