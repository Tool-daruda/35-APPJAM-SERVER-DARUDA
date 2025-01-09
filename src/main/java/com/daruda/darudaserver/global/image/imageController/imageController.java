package com.daruda.darudaserver.global.image.imageController;

import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class imageController {
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<List<Long>> uploadImages(@RequestParam("files")List<MultipartFile> files){
        List<Long> imageUrls = imageService.uploadImages(files);
        return ResponseEntity.ok(imageUrls);
    }

    @DeleteMapping
    public ResponseEntity<List<Long>> deleteImages(@RequestBody List<Long> imageIds) {
        imageService.deleteImages(imageIds);
        return ResponseEntity.ok(imageIds);
    }
}
