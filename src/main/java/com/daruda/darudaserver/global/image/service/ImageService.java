package com.daruda.darudaserver.global.image.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.image.entity.Image;
import com.daruda.darudaserver.global.image.repository.ImageRepository;
import com.daruda.darudaserver.global.s3.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
	private final S3Service s3Service;
	private final ImageRepository imageRepository;
	private final S3Presigner s3Presigner;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

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

	public List<Long> getImageIdList(final List<String> imageUrls) {
		return imageRepository.findAllByImageUrlIn(imageUrls).stream()
			.map(Image::getImageId)
			.toList();
	}

	// 이미지 조회 메서드
	private Image getImageById(long imageId) {
		return imageRepository.findById(imageId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FILE_NOT_FOUND));
	}

	public String getImageUrlById(final Long imageId) {
		return getImageById(imageId).getImageUrl();
	}

	@Transactional
	public String createUploadPresignedUrl(String key) {
		PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
			req -> req.signatureDuration(Duration.ofMinutes(15)) // 유효시간 15분
				.putObjectRequest(
					PutObjectRequest.builder()
						.bucket(bucketName)
						.key(key)
						.build()
				)
		);
		return presignedRequest.url().toString();
	}

	@Transactional
	public List<Long> createImage(List<String> imageUrlList) {
		List<Long> imageIdList = imageUrlList.stream()
			.map(
				imageUrl -> {
					try {
						Image image = Image.builder()
							.imageUrl(imageUrl)
							.build();
						Image savedImage = imageRepository.save(image);
						log.info("Image saved to database: imageId={}", savedImage.getImageId());
						return savedImage.getImageId();
					} catch (Exception e) {
						log.error("Image creation failed: error={}", e.getMessage(), e);
						throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
					}
				}
			)
			.toList();
		return imageIdList;
	}

	public boolean existsImageUrl(final String imageUrl) {
		return imageRepository.existsByImageUrl(imageUrl);
	}
}
