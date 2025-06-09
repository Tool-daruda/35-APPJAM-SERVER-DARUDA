package com.daruda.darudaserver.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
	PENDING("처리 대기"),
	APPROVED("승인"),
	REJECTED("거절");

	private final String description;
}
