package com.daruda.darudaserver.global.oci;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OCIService {

	private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("image/jpeg", "image/png", "image/jpg",
		"image/webp", "image/heic", "image/heif");
	private static final Long MAX_FILE_SIZE = 7 * 1024 * 1024L;
	private final ObjectStorage objectStorage;
	@Value("${oci.bucket.name}")
	private String bucketName;

	@Value("${oci.bucket.namespace}")
	private String namespaceName;

	@Value("${oci.region}")
	private String region;

	public String uploadImage(MultipartFile file) {
		validateExtension(file);
		validateFileSize(file);
		String objectName = generateImageFileName(file);

		try {
			PutObjectRequest request = PutObjectRequest.builder()
				.bucketName(bucketName)
				.namespaceName(namespaceName)
				.objectName(objectName)
				.contentLength(file.getSize())
				.contentType(file.getContentType())
				.contentDisposition("inline")
				.putObjectBody(file.getInputStream())
				.build();

			objectStorage.putObject(request);

			return getPublicUrl(objectName);
		} catch (IOException e) {
			log.error("Failed to upload image to OCI", e);
			throw new InvalidValueException(ErrorCode.FILE_UPLOAD_FAIL);
		} catch (Exception e) {
			log.error("OCI upload error", e);
			throw new InvalidValueException(ErrorCode.FILE_UPLOAD_FAIL);
		}
	}

	public void deleteImage(String imageUrl) {
		String objectName = extractObjectName(imageUrl);

		try {
			DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucketName(bucketName)
				.namespaceName(namespaceName)
				.objectName(objectName)
				.build();

			objectStorage.deleteObject(request);
		} catch (Exception e) {
			log.error("Failed to delete image from OCI", e);
			throw new InvalidValueException(ErrorCode.FILE_DELETE_FAIL);
		}
	}

	public String createUploadPresignedUrl(String objectName) {
		try {
			CreatePreauthenticatedRequestDetails details = CreatePreauthenticatedRequestDetails.builder()
				.name("PAR_UPLOAD_" + objectName)
				.objectName(objectName)
				.accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectWrite)
				.timeExpires(Date.from(Instant.now().plus(Duration.ofMinutes(15))))
				.build();

			CreatePreauthenticatedRequestRequest request = CreatePreauthenticatedRequestRequest.builder()
				.namespaceName(namespaceName)
				.bucketName(bucketName)
				.createPreauthenticatedRequestDetails(details)
				.build();

			CreatePreauthenticatedRequestResponse response = objectStorage.createPreauthenticatedRequest(request);
			String accessUri = response.getPreauthenticatedRequest().getAccessUri();

			// Construct full URL
			return String.format("https://objectstorage.%s.oraclecloud.com%s", region, accessUri);
		} catch (Exception e) {
			log.error("Failed to create presigned URL", e);
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private String getPublicUrl(String objectName) {
		return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
			region, namespaceName, bucketName, objectName);
	}

	private String extractObjectName(String imageUrl) {
		if (imageUrl.contains("/o/")) {
			return imageUrl.substring(imageUrl.lastIndexOf("/o/") + 3);
		}
		return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
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
}
