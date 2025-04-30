package com.daruda.darudaserver.domain.comment.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentRetrieveResponse;
import com.daruda.darudaserver.domain.comment.service.CommentService;
import com.daruda.darudaserver.global.annotation.DisableSwaggerSecurity;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
@Tag(name = "comment 컨트롤러", description = "댓글과 관련된 API를 처리합니다.")
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	@Operation(summary = "댓글 작성", description = "댓글을 작성합니다.")
	public ResponseEntity<ApiResponse<CreateCommentResponse>> postComment(
		@AuthenticationPrincipal Long userId,

		@Parameter(description = "board Id", example = "1")
		@RequestParam("board-id") Long boardId,

		@Parameter(description = "작성할 댓글")
		@RequestBody @Valid CreateCommentRequest request
	) throws IOException {
		CreateCommentResponse response = commentService.postComment(userId, boardId, request);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(response, SuccessCode.SUCCESS_CREATE));
	}

	@DisableSwaggerSecurity
	@GetMapping
	@Operation(summary = "댓글 조회", description = "댓글을 조회합니다.")
	public ResponseEntity<ApiResponse<GetCommentRetrieveResponse>> getComment(
		@Parameter(description = "board Id", example = "1")
		@RequestParam("board-id") Long boardId,

		@Parameter(description = "조회할 댓글 개수", example = "10")
		@RequestParam(value = "size", defaultValue = "10") int size,

		@Parameter(description = "조회했을 때 마지막 comment Id", example = "10")
		@RequestParam(value = "lastCommentId", required = false) Long commentId
	) {
		GetCommentRetrieveResponse response = commentService.getComments(boardId, size, commentId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(response, SuccessCode.SUCCESS_FETCH));
	}

	@DeleteMapping("/{comment-id}")
	@Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
	public ResponseEntity<ApiResponse<?>> deleteComment(
		@AuthenticationPrincipal Long userId,

		@Parameter(description = "comment Id", example = "1")
		@PathVariable("comment-id") Long commentId)
	{
		commentService.deleteComment(userId, commentId);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_DELETE));
	}
}
