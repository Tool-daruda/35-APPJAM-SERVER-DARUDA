package com.daruda.darudaserver.domain.report.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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

	private static final Map<String, SuspensionDuration> DESCRIPTION_MAP =
		Arrays.stream(values())
			.collect(Collectors.toMap(SuspensionDuration::getDescription, duration -> duration));
	private final int days;
	private final String description;

	public static SuspensionDuration fromString(String description) {
		SuspensionDuration duration = DESCRIPTION_MAP.get(description);
		if (duration == null) {
			throw new BusinessException(ErrorCode.INVALID_FIELD_ERROR);
		}

		return duration;
	}
}
