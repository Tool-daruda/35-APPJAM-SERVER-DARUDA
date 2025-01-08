package com.daruda.darudaserver.domain.community.controller;

import com.daruda.darudaserver.domain.community.dto.request.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.exception.code.SuccessCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
            @RequestPart @Valid final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            @RequestPart(value = "images", required = false) @Validated @Size(max=5) List<MultipartFile> images){

        boardService.createBoard(boardCreateAndUpdateReq,images);
        return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_CREATE));
    }
}
