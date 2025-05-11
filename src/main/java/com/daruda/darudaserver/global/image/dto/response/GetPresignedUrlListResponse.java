package com.daruda.darudaserver.global.image.dto.response;

import java.util.List;

public record GetPresignedUrlListResponse(
	List<String> presignedUrlList
) {
	public static GetPresignedUrlListResponse of(List<String> presignedUrlList) {
		return new GetPresignedUrlListResponse(presignedUrlList);
	}
}
