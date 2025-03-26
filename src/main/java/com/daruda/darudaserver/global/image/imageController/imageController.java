package com.daruda.darudaserver.global.image.imageController;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.global.image.service.ImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class imageController {
	private final ImageService imageService;

	@PostMapping
	public ResponseEntity<List<Long>> uploadImages(@RequestPart("files") List<MultipartFile> files) {
		List<Long> imageUrls = imageService.uploadImages(files);
		return ResponseEntity.ok(imageUrls);
	}

	@DeleteMapping
	public ResponseEntity<List<Long>> deleteImages(@RequestBody List<Long> imageIds) {
		imageService.deleteImages(imageIds);
		return ResponseEntity.ok(imageIds);
	}
}
