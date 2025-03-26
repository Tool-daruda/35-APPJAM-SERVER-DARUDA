package com.daruda.darudaserver.domain.comment.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentRetrieveResponse;
import com.daruda.darudaserver.domain.comment.service.CommentService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {
	private final CommentService commentService;

	@PostMapping
	public ResponseEntity<ApiResponse<CreateCommentResponse>> postComment(@UserId Long userId,
		@RequestParam("board-id") Long boardId,
		@Valid @ModelAttribute CreateCommentRequest createCommentRequest,
		@Nullable @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
		CreateCommentResponse createCommentResponse = commentService.postComment(userId, boardId, createCommentRequest,
			image);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(createCommentResponse, SuccessCode.SUCCESS_CREATE));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<?>> deleteComment(@UserId Long userId,
		@RequestParam("comment-id") Long commentId) {
		commentService.deleteComment(userId, commentId);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_DELETE));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<GetCommentRetrieveResponse>> getComment(@RequestParam("board-id") Long boardId,
		@RequestParam(value = "size", defaultValue = "10") int size,
		@RequestParam(value = "lastCommentId", required = false) Long commentId) {
		GetCommentRetrieveResponse getCommentRetrieveResponse = commentService.getComments(boardId, size, commentId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(getCommentRetrieveResponse, SuccessCode.SUCCESS_FETCH));
	}
}
