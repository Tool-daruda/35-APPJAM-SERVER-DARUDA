package com.daruda.darudaserver.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
	HATE_SPEECH("욕설/비하"),
	ILLEGAL("불법촬영물 유통"),
	SPAM("유출/사칭/사기"),
	ADULT_CONTENT("음란물/불건전한 만남 및 대화"),
	POLITICAL("정당/정치인 비하 및 선거운동"),
	COMMERCIAL("상업적 광고 및 판매"),
	DEFAMATION("낚시/도배");

	private final String description;
}
