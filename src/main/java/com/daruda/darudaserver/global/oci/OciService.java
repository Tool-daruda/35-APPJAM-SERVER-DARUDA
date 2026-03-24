package com.daruda.darudaserver.global.oci;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.image.dto.response.PresignedUrlResponse;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OciService {

	private final ObjectStorage objectStorage;

	@Value("${oci.bucket.name}")
	private String bucketName;

	@Value("${oci.bucket.namespace}")
	private String namespaceName;

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

	public PresignedUrlResponse createUploadPresignedUrl(String prefix, String extension) {
		String objectName = prefix + "/" + UUID.randomUUID() + extension;

		try {
			CreatePreauthenticatedRequestDetails details = CreatePreauthenticatedRequestDetails.builder()
				.name("PAR_UPLOAD_" + UUID.randomUUID().toString().substring(0, 8))
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

			String presignedUrl = String.format("%s%s", objectStorage.getEndpoint(), accessUri);
			String publicUrl = getPublicUrl(objectName);

			return PresignedUrlResponse.of(presignedUrl, publicUrl);
		} catch (Exception e) {
			log.error("Failed to create presigned URL", e);
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private String getPublicUrl(String objectName) {
		return String.format("%s/n/%s/b/%s/o/%s", objectStorage.getEndpoint(), namespaceName, bucketName, objectName);
	}

	private String extractObjectName(String imageUrl) {
		if (imageUrl.contains("/o/")) {
			return imageUrl.substring(imageUrl.lastIndexOf("/o/") + 3);
		}
		return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
	}
}
