package com.daruda.darudaserver.global.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedUrlResponse(
	@Schema(description = "이미지를 업로드할 수 있는 Presigned URL")
	String presignedUrl,
	@Schema(description = "업로드 완료 후 접근 가능한 Public URL")
	String publicUrl
) {
	public static PresignedUrlResponse of(String presignedUrl, String publicUrl) {
		return new PresignedUrlResponse(presignedUrl, publicUrl);
	}
}
