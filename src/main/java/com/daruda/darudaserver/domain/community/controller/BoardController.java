package com.daruda.darudaserver.domain.community.controller;

import com.daruda.darudaserver.domain.community.dto.request.BoardCreateAndUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class BoardController {

    public ResponseEntity<?> createBoard(
            @RequestPart("title") @Valid final BoardCreateAndUpdateRequest boardCreateAndUpdateRequest,
            @RequestPart(value = "images", required = false) @Validated @Size(max=5) List<MultipartFile> images){

    }
}
