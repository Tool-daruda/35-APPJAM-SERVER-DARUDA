package com.daruda.darudaserver.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
	PENDING("처리 대기중"),
	PROCESSING("처리중"),
	COMPLETED("처리 완료"),
	REJECTED("신고 거절");

	private final String description;
}
