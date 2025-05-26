package com.daruda.darudaserver.domain.report.entity;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuspensionDuration {
	ONE(1, "1일"),
	THREE(3, "3일"),
	SEVEN(7, "7일"),
	THIRTY(30, "30일"),
	NINETY(90, "90일"),
	ONE_YEAR(365, "1년");

	private final int days;
	private final String description;

	public static SuspensionDuration fromString(String description) {
		for (SuspensionDuration suspensionDuration : SuspensionDuration.values()) {
			if (suspensionDuration.getDescription().equals(description)) {
				return suspensionDuration;
			}
		}
		throw new BusinessException(ErrorCode.INVALID_FIELD_ERROR);
	}
} 