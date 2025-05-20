package com.daruda.darudaserver.domain.notification.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
	COMMENT("댓글"),
	NOTICE("공지 사항");

	private final String name;
}
