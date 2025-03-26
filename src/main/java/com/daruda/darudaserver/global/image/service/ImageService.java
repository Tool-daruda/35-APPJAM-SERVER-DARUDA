package com.daruda.darudaserver.global.image.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.image.entity.Image;
import com.daruda.darudaserver.global.image.repository.ImageRepository;
import com.daruda.darudaserver.global.infra.S3.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
	private final S3Service s3Service;
	private final ImageRepository imageRepository;

	// 1. 이미지 업로드
	@Transactional
	public List<Long> uploadImages(final List<MultipartFile> images) {

		return images.stream()
			.map(image -> {
				try {
					String imageUrl = s3Service.uploadImage(image);
					log.info("Uploaded image successfully: imageUrl={}", imageUrl);
					Image newImage = Image.builder()
						.imageUrl(imageUrl)
						.build();
					Image savedImage = imageRepository.save(newImage);
					log.info("Image saved to database: imageId={}", savedImage.getImageId());
					return savedImage.getImageId();
				} catch (Exception e) {
					log.error("Image upload failed: error={}", e.getMessage(), e);
					throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
				}
			})
			.toList();
	}

	// 2. 이미지 삭제
	@Transactional
	public void deleteImages(final List<Long> imageIds) {

		for (Long imageId : imageIds) {
			Image image = getImageById(imageId); // 이미지 조회

			try {
				// S3에서 삭제
				log.info("Delete image from S3: imageId={}, imageUrl={}", imageId, image.getImageUrl());
				s3Service.deleteImage(image.getImageUrl());
				// DB에서 삭제
				imageRepository.delete(image);
				log.info("Image deleted successfully: imageId={}", imageId);
			} catch (InvalidValueException e) {
				log.error("Failed to delete image: imageId={}, error={}", imageId, e.getMessage(), e);
				throw new InvalidValueException(ErrorCode.FILE_DELETE_FAIL);
			}
		}
	}

	// 이미지 조회 메서드
	private Image getImageById(long imageId) {
		return imageRepository.findById(imageId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FILE_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public String getImageUrlById(final Long imageId) {
		return getImageById(imageId).getImageUrl();
	}
}
