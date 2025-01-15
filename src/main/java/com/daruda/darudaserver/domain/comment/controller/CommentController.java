package com.daruda.darudaserver.domain.comment.controller;
import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.service.CommentService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> postComment(@UserId Long userId,
                                         @RequestParam("board-id")Long boardId,
                                         @Valid @ModelAttribute CreateCommentRequest createCommentRequest,
                                         @Nullable @RequestPart(value = "image", required = false)MultipartFile image) throws IOException {
        CreateCommentResponse createCommentResponse = commentService.postComment(userId, boardId, createCommentRequest, image);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(createCommentResponse, SuccessCode.SUCCESS_CREATE));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteComment(@UserId Long userId,
                                           @PathVariable("comment-id")Long commentId){
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_DELETE));
    }

}
