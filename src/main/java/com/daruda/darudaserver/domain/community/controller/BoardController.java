package com.daruda.darudaserver.domain.community.controller;

import com.daruda.darudaserver.domain.community.dto.request.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.response.BoardRes;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class BoardController {

    private final BoardService boardService;

    @PostMapping()
    public ResponseEntity<ApiResponse<?>> createBoard(
            @ModelAttribute @Valid final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            @RequestPart(value = "images", required = false)  @Size(max=5) List<MultipartFile> images){

        BoardRes boardRes = boardService.createBoard(boardCreateAndUpdateReq,images);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes,SuccessCode.SUCCESS_CREATE));
    }

    @PatchMapping("{board-id}")
    public ResponseEntity<ApiResponse<?>> updateBoard(
            @PathVariable(name="board-id") final Long boardId,
            @ModelAttribute @Valid final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            @RequestPart(value = "images", required = false)  @Size(max=5) List<MultipartFile> images){
        BoardRes boardRes = boardService.updateBoard(boardId , boardCreateAndUpdateReq,images);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes,SuccessCode.SUCCESS_CREATE));
    }

    @GetMapping("{board-id}")
    public ResponseEntity<ApiResponse<?>> getBoard(@PathVariable(name="board-id") final Long boardId){
        BoardRes boardRes = boardService.getBoard(boardId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes,SuccessCode.SUCCESS_FETCH));
    }

    @DeleteMapping("{board-id}")
    public ResponseEntity<ApiResponse<?>> deleteBoard(
            @PathVariable(name="board-id") final Long boardId){
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_DELETE));
    }
}
