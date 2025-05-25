package com.daruda.darudaserver.domain.notification.dto.request;

public record CommunityBlockNoticeRequest(
	Long userId,
	int blockDurationInDays
) {
}
