package com.daruda.darudaserver.global.image.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.dto.SuccessResponse;
import com.daruda.darudaserver.global.image.dto.request.GetPresignedUrlRequest;
import com.daruda.darudaserver.global.image.dto.response.GetPresignedUrlListResponse;
import com.daruda.darudaserver.global.image.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/image")
@Tag(name = "image 컨트롤러", description = "이미지와 관련된 API를 처리합니다.")
public class ImageController {
	private final ImageService imageService;

	@PostMapping
	@Operation(summary = "이미지 업로드", description = "이미지를 S3 버킷에 업로드 합니다.")
	public ResponseEntity<List<Long>> uploadImages(@RequestPart("files") List<MultipartFile> files) {
		List<Long> imageUrls = imageService.uploadImages(files);
		return ResponseEntity.ok(imageUrls);
	}

	@DeleteMapping
	@Operation(summary = "이미지 삭제", description = "S3 버킷에 업로드된 이미지를 삭제 합니다.")
	public ResponseEntity<List<Long>> deleteImages(@RequestBody List<Long> imageIds) {
		imageService.deleteImages(imageIds);
		return ResponseEntity.ok(imageIds);
	}

	@GetMapping("/presigned-url")
	@Operation(summary = "이미지 업로드 용 presigned-url발급", description = "이미지 업로드를 위한 presignedUrl을 발급합니다")
	public ResponseEntity<SuccessResponse<?>> getPresignedUrl(
		@RequestBody GetPresignedUrlRequest getPresignedUrlRequest) {
		GetPresignedUrlListResponse getPresignedUrlResponseList = imageService.getUploadPresignedURL(
			getPresignedUrlRequest);

		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, getPresignedUrlResponseList));
	}

}
