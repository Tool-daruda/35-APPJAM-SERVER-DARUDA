package com.daruda.darudaserver.domain.report.controller;

import com.daruda.darudaserver.domain.report.dto.req.CreateReportRequest;
import com.daruda.darudaserver.domain.report.dto.res.CreateReportResponse;
import com.daruda.darudaserver.domain.report.service.ReportService;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@Tag(name = "report 컨트롤러", description = "신고와 관련된 API를 처리합니다.")
public class ReportController {

	private final ReportService reportService;

	@PostMapping
	@Operation(summary = "신고 생성", description = "게시글이나 댓글을 신고합니다.")
	public ResponseEntity<ApiResponse<CreateReportResponse>> createReport(
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody CreateReportRequest request
	) {
		CreateReportResponse response = reportService.createReport(userId, request);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(response, SuccessCode.SUCCESS_CREATE));
	}
}
