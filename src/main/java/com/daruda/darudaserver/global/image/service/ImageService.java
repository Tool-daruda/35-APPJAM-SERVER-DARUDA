package com.daruda.darudaserver.global.image.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.image.dto.response.PresignedUrlResponse;
import com.daruda.darudaserver.global.image.entity.Image;
import com.daruda.darudaserver.global.image.repository.ImageRepository;
import com.daruda.darudaserver.global.oci.OciService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

	private static final List<String> ALLOWED_EXTENSIONS = List.of(
		".jpg", ".jpeg", ".png", ".webp", ".gif", ".svg", ".heic", ".heif"
	);
	private final OciService ociService;
	private final ImageRepository imageRepository;

	// 2. 이미지 삭제
	@Transactional
	public void deleteImages(final List<Long> imageIds) {
		List<Image> images = imageRepository.findAllById(imageIds);

		if (images.size() != imageIds.size()) {
			throw new NotFoundException(ErrorCode.FILE_NOT_FOUND);
		}

		imageRepository.deleteAllInBatch(images);

		images.parallelStream().forEach(image -> {
			try {
				ociService.deleteImage(image.getImageUrl());
			} catch (Exception e) {
				log.error("Failed to delete image from OCI: {}", image.getImageUrl(), e);
			}
		});
	}

	// 이미지 조회 메서드
	private Image getImageById(long imageId) {
		return imageRepository.findById(imageId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FILE_NOT_FOUND));
	}

	public String getImageUrlById(final Long imageId) {
		return getImageById(imageId).getImageUrl();
	}

	public PresignedUrlResponse createUploadPresignedUrl(String prefix, String extension) {
		validateExtensionString(extension);
		return ociService.createUploadPresignedUrl(prefix, extension);
	}

	@Transactional
	public List<Long> createImage(List<String> imageUrlList) {
		List<Image> images = imageUrlList.stream()
			.map(url -> {
				validateUrlExtension(url);
				return Image.builder().imageUrl(url).build();
			})
			.toList();

		return imageRepository.saveAll(images).stream()
			.map(Image::getImageId)
			.toList();
	}

	private void validateExtensionString(String extension) {
		if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
		}
	}

	private void validateUrlExtension(String url) {
		if (url == null) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
		}
		String lowerUrl = url.toLowerCase();
		boolean isValid = ALLOWED_EXTENSIONS.stream().anyMatch(lowerUrl::endsWith);
		if (!isValid) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
		}
	}
}
