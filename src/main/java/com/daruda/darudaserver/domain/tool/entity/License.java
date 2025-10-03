package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import lombok.Getter;

@Getter
public enum License {
	FREE("무료"),
	PARTIALLY_FREE("부분 무료"),
	PAID("유료");

	private final String koreanName;

	// 생성자
	License(String koreanName) {
		this.koreanName = koreanName;
	}

	// Getter 메서드
	public String getKoreanName() {
		return koreanName;
	}

	public static License from(String koreanName) {
		if (koreanName == null) {
			throw new NotFoundException(ErrorCode.DATA_NOT_FOUND);
		}
		for (License license : values()) {
			if (license.koreanName.equals(koreanName)) {
				return license;
			}
		}
		throw new NotFoundException(ErrorCode.DATA_NOT_FOUND);
	}
}
