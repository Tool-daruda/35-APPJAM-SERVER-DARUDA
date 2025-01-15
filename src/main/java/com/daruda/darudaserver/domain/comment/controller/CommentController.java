package com.daruda.darudaserver.domain.comment.controller;
import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.service.CommentService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> postComment(@UserId Long userId,
                                         @PathVariable("board-id")Long boardId,
                                         @Valid @RequestBody CreateCommentRequest createCommentRequest){
        CreateCommentResponse createCommentResponse = commentService.postComment(userId, boardId, createCommentRequest);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(createCommentResponse, SuccessCode.SUCCESS_CREATE));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteComment(@UserId Long userId,
                                           @PathVariable("comment-id")Long commentId){
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_DELETE));
    }

}
