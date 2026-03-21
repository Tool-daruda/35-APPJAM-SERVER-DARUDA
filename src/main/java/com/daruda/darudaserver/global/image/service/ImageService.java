package com.daruda.darudaserver.global.image.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.image.entity.Image;
import com.daruda.darudaserver.global.image.repository.ImageRepository;
import com.daruda.darudaserver.global.oci.OCIService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

	private final OCIService ociService;
	private final ImageRepository imageRepository;

	// 1. 이미지 업로드
	public List<Long> uploadImages(final List<MultipartFile> images) {
		List<String> imageUrls = images.stream()
			.map(ociService::uploadImage)
			.toList();

		try {
			List<Image> imageEntities = imageUrls.stream()
				.map(url -> Image.builder().imageUrl(url).build())
				.toList();

			return imageRepository.saveAll(imageEntities).stream()
				.map(Image::getImageId)
				.toList();
		} catch (Exception e) {
			log.error("DB Save failed after OCI upload. Reverting uploads...", e);
			imageUrls.forEach(url -> {
				try {
					ociService.deleteImage(url);
				} catch (Exception ex) {
					log.error("Failed to revert image upload: {}", url, ex);
				}
			});
			throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
		}
	}

	// 2. 이미지 삭제
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

	public String createUploadPresignedUrl(String key) {
		return ociService.createUploadPresignedUrl(key);
	}

	@Transactional
	public List<Long> createImage(List<String> imageUrlList) {
		List<Image> images = imageUrlList.stream()
			.map(url -> Image.builder().imageUrl(url).build())
			.toList();

		return imageRepository.saveAll(images).stream()
			.map(Image::getImageId)
			.toList();
	}
}
