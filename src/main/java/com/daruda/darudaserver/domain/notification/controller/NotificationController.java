package com.daruda.darudaserver.domain.notification.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.daruda.darudaserver.domain.notification.dto.request.CommunityBlockNoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.request.NoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.response.NotificationResponse;
import com.daruda.darudaserver.domain.notification.service.NotificationService;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "SSE 연결", description = "실시간 알림을 위한 SSE(Server-Sent Events)에 연결합니다.")
	public ResponseEntity<SseEmitter> subscribe(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "EventStream이 끊어 젔을 경우, Web에서 수신한 마지막 ID 값", example = "2_1747732045603")
		@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
		return ResponseEntity.ok(notificationService.subscribe(userId, lastEventId));
	}

	@PatchMapping("/read/{notification-id}")
	@Operation(summary = "알림 읽음", description = "알림을 읽음 처리 합니다.")
	public ResponseEntity<ApiResponse<Void>> readNotification(
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "notification Id", example = "1")
		@PathVariable(name = "notification-id") Long notificationId) {
		notificationService.readNotification(userId, notificationId);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_UPDATE));
	}

	@GetMapping
	@Operation(summary = "전체 알림 목록 조회", description = "전체 알림을 목록으로 조회합니다.")
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
		@AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(
			ApiResponse.ofSuccessWithData(notificationService.getNotifications(userId), SuccessCode.SUCCESS_FETCH));
	}

	@GetMapping("/recent")
	@Operation(summary = "최근 알림 목록 조회", description = "최근 3개의 알림을 목록으로 조회합니다.")
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> getRecentNotifications(
		@AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(
			ApiResponse.ofSuccessWithData(notificationService.getRecentNotifications(userId),
				SuccessCode.SUCCESS_FETCH));
	}

	@PostMapping("/notice")
	@Operation(summary = "공지 발송(관리자)", description = "모든 사용자에게 공지를 발송합니다.")
	public ResponseEntity<ApiResponse<Void>> notice(@RequestBody NoticeRequest noticeRequest) {
		notificationService.sendNotice(noticeRequest);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_SEND_NOTICE));
	}

	@PostMapping("/block-notice")
	@Operation(summary = "커뮤니티 제한 공지 발송(관리자)", description = "커뮤니티가 제한된 사용자에게 공지를 발송합니다.")
	public ResponseEntity<ApiResponse<Void>> blockNotice(
		@RequestBody CommunityBlockNoticeRequest communityBlockNoticeRequest) {
		notificationService.sendBlockNotice(communityBlockNoticeRequest);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_SEND_COMMUNITY_BLOCK_NOTICE));
	}
}
