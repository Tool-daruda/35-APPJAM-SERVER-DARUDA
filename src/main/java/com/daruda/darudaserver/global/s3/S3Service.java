package com.daruda.darudaserver.global.s3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class S3Service {

	private final String bucketName;
	private final S3Config s3Config;
	private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("image/jpeg", "image/png", "image/jpg",
		"image/webp", "image/heic", "image/heif");
	private static final Long MAX_FILE_SIZE = 7 * 1024 * 1024L;

	public S3Service(@Value("${cloud.aws.s3.bucket}") String bucketName, S3Config s3Config) {
		this.bucketName = bucketName;
		this.s3Config = s3Config;
	}

	public String uploadImage(MultipartFile image) {
		final String imageName = generateImageFileName(image);
		final S3Client s3Client = s3Config.getS3Client();
		validateExtension(image);
		validateFileSize(image);
		
		try {
			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(imageName) //이미지 이름만 키로 사용
				.contentType(image.getContentType())
				.contentDisposition("inline")
				.build();
			RequestBody requestBody = RequestBody.fromBytes(image.getBytes()); //파일 바이트로 변환
			s3Client.putObject(request, requestBody);
		} catch (S3Exception | IOException e) {
			throw new InvalidValueException(ErrorCode.FILE_UPLOAD_FAIL);
		}
		return imageName; //S3 에 저장된 경로 이미지 반환
	}

	public void deleteImage(String s3Key) {
		final S3Client s3Client = s3Config.getS3Client();
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.build()
			);
		} catch (S3Exception e) {
			throw new InvalidValueException(ErrorCode.FILE_DELETE_FAIL);
		}
	}

	private String generateImageFileName(MultipartFile image) {
		String extension = getExtension(Objects.requireNonNull(image.getContentType()));
		if (extension == null) {
			throw new InvalidValueException(ErrorCode.INVALID_IMAGE_TYPE);
		}
		return UUID.randomUUID() + extension;
	}

	private String getExtension(String contentType) {
		return switch (contentType) {
			case "image/png" -> ".png";
			case "image/webp" -> ".webp";
			case "image/heic" -> ".heic";
			case "image/heif" -> ".heif";
			default -> ".jpg";
		};
	}

	private void validateExtension(MultipartFile image) {
		String contentType = image.getContentType();
		if (!IMAGE_EXTENSIONS.contains(contentType)) {
			throw new InvalidValueException(ErrorCode.INVALID_IMAGE_TYPE);
		}
	}

	private void validateFileSize(MultipartFile image) {
		if (image.getSize() > MAX_FILE_SIZE) {
			throw new InvalidValueException(ErrorCode.INVALID_IMAGE_TYPE);
		}
	}

	public String getImageUrl(String imageName) {
		return String.format("https://%s.s3.amazonaws.com/%s", bucketName, imageName); // S3의 퍼블릭 URL 생성
	}
}
