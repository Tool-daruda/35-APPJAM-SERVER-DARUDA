package com.daruda.darudaserver.domain.admin.controller;

import org.springframework.http.ResponseEntity;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
		@Parameter(description = "정렬 기준", example = "createdAt")
		@RequestParam(defaultValue = "createdAt", value = "criteria") String criteria,
		@Parameter(description = "정렬 순서 (ASC/DESC)", example = "DESC")
		@RequestParam(defaultValue = "DESC", value = "direction") String direction,
		@Min(0)
		@Parameter(description = "페이지 인덱스", example = "0")
		@RequestParam(value = "page", defaultValue = "0") int page,
		@Min(1) @Max(100)
		@Parameter(description = "페이지 크기", example = "10")
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AdminToolPageRes res = adminService.fetchAllTool(criteria, direction, page, size);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, res));
	}

	@PostMapping
	@Operation(
		summary = "관리자용 툴 추가",
		description = """
			관리자용 툴 추가 API입니다.

			**플랜(plans) 필드 사용 가이드:**
			- 플랜은 여러 개를 추가할 수 있습니다 (배열 형태)
			- 각 플랜은 planName(플랜명), planPrice(가격), planDescription(설명)을 포함합니다
			- planType과 plans는 함께 사용됩니다:
			  * FREE: 무료 플랜인 경우 plans는 빈 배열이거나 무료 플랜 1개만 포함
			  * MONTHLY: 월간 구독 플랜들 (예: Basic, Pro, Enterprise 등)
			  * PURCHASE: 일회성 구매 플랜들 (예: Starter, Professional 등)
			  * MONTHLY_ANNUAL: 월간 & 연간 플랜 혼합 (예: 월간 Basic, 연간 Basic 등)

			**플랜 케이스 예시:**
			1. 무료 툴: planType="무료", plans=[]
			   또는 plans=[{planName:"무료", planPrice:0, planDescription:"기본 기능 무료 제공"}]
			2. 월간 구독: planType="월간",
			   plans=[{planName:"Basic", planPrice:10000, ...}, {planName:"Pro", planPrice:30000, ...}]
			3. 일회성 구매: planType="구매",
			   plans=[{planName:"Starter", planPrice:50000, ...},
			   {planName:"Professional", planPrice:150000, ...}]
			4. 혼합: planType="월간 & 연간",
			   plans=[{planName:"Basic 월간", planPrice:10000, ...},
			   {planName:"Basic 연간", planPrice:100000, ...}]

			**주의사항:**
			- planPrice는 원화 단위로 입력합니다 (예: 10000 = 1만원)
			- planName, planPrice, planDescription은 모두 필수입니다
			- planDescription은 최대 500자까지 입력 가능합니다
			- plans가 null이거나 빈 배열일 수 있지만, planType이 설정된 경우 적절한 플랜 정보를 제공하는 것을 권장합니다
			"""
	)
	public ResponseEntity<SuccessResponse<Void>> createTool(
		@Parameter(description = "추가할 툴 정보 (플랜 포함)")
		@RequestBody @Valid CreateToolRequest createToolRequest) {

		adminService.createTool(createToolRequest);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_CREATE));
	}

	@PatchMapping("/tools/{toolId}")
	@Operation(
		summary = "관리자용 툴 수정",
		description = """
			관리자용 툴 수정 API입니다.

			**플랜(plans) 필드 수정 가이드:**
			- plans 필드를 전송하면 기존 플랜들이 모두 삭제되고 새로운 플랜들로 교체됩니다
			- plans를 null로 보내면 플랜 정보는 변경되지 않습니다
			- plans를 빈 배열([])로 보내면 모든 플랜이 삭제됩니다

			**플랜 수정 케이스:**
			1. 플랜 추가/변경: plans=[{planName:"New Plan", planPrice:20000, planDescription:"새로운 플랜"}]
			2. 플랜 삭제: plans=[] (모든 플랜 삭제)
			3. 플랜 유지: plans=null (기존 플랜 그대로 유지)
			4. 플랜 수정: plans=[수정된 플랜 목록] (기존 플랜 삭제 후 새로 추가)

			**주의사항:**
			- 플랜 수정 시 planName, planPrice, planDescription은 모두 필수입니다 (null이면 해당 플랜은 저장되지 않습니다)
			- planDescription은 최대 500자까지 입력 가능합니다
			- planType도 함께 수정하려면 planType 필드도 함께 전송하세요
			- 기존 플랜을 유지하면서 일부만 수정하려면, 유지할 플랜도 포함하여 전체 플랜 목록을 다시 전송해야 합니다
			"""
	)
	public ResponseEntity<SuccessResponse<Void>> updateTool(
		@Parameter(description = "수정할 툴 ID") @PathVariable Long toolId,
		@Parameter(description = "수정 요청 DTO (플랜 정보 포함)")
		@RequestBody @Valid UpdateToolRequest request
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
