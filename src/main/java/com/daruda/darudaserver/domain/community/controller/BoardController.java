package com.daruda.darudaserver.domain.community.controller;

import com.daruda.darudaserver.domain.community.dto.req.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.res.BoardRes;
import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
import com.daruda.darudaserver.domain.community.dto.res.GetBoardResponse;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 작성
     */
    @PostMapping("/boards")
    public ResponseEntity<ApiResponse<?>> createBoard(
            @UserId Long userId,
            @ModelAttribute @Valid BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            @RequestPart(value = "images", required = false)  @Size(max=5) List<MultipartFile> images){

        BoardRes boardRes = boardService.createBoard(userId, boardCreateAndUpdateReq, images);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes,SuccessCode.SUCCESS_CREATE));
    }

    /**
     * 게시글 수정
     */
    @PatchMapping("/boards/{board-id}")
    public ResponseEntity<ApiResponse<?>> updateBoard(
            @UserId Long userId,
            @PathVariable(name="board-id") final Long boardId,
            @ModelAttribute @Valid final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            @RequestPart(value = "images", required = false)  @Size(max=5) List<MultipartFile> images){
        BoardRes boardRes = boardService.updateBoard(userId , boardId , boardCreateAndUpdateReq,images);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes,SuccessCode.SUCCESS_UPDATE));
    }

    /**
     * 게시글 조회
     */
    @GetMapping("/boards/board/{board-id}")
    public ResponseEntity<ApiResponse<?>> getBoard(
            @AuthenticationPrincipal Long userIdOrNull,
            @PathVariable(name="board-id") final Long boardId){
        BoardRes boardRes = boardService.getBoard(userIdOrNull, boardId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardRes,SuccessCode.SUCCESS_FETCH));
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/boards/{board-id}")
    public ResponseEntity<ApiResponse<?>> deleteBoard(
            @UserId Long userId,
            @PathVariable(name="board-id") final Long boardId){
        boardService.deleteBoard(userId,boardId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_DELETE));
    }

    /**
     * 게시글 스크랩
     */
    @PostMapping("/users/boards/{board-id}/scrap")
    public ResponseEntity<ApiResponse<?>> scrapBoard(
            @UserId Long userId,
            @PathVariable(name="board-id") final Long boardId){
        BoardScrapRes boardScrapRes = boardService.postScrap(userId,boardId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardScrapRes , SuccessCode.SUCCESS_SCRAP));
    }

    /**
     * 게시글 리스트 조회
     */
    @GetMapping("boards/board/list")
    public ResponseEntity <ApiResponse<?>> getBoardList(
            @AuthenticationPrincipal Long userIdOrNull,
            @RequestParam(name="isFree") Boolean isFree,
            @RequestParam(name = "toolId",required = false) Long toolId,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "lastBoardId", required = false) Long lastBoardId )
    {
        GetBoardResponse boardResponse =  boardService.getBoardList(userIdOrNull, isFree, toolId, size, lastBoardId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardResponse, SuccessCode.SUCCESS_FETCH));
    }
}
