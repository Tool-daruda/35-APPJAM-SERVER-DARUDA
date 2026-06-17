package com.daruda.darudaserver.domain.community.entity;

import java.util.Arrays;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;

public enum BoardSortType {
	LATEST,
	SCRAP;

	public static BoardSortType from(final String value) {
		if (value == null || value.isBlank()) {
			return LATEST;
		}
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(value))
			.findFirst()
			.orElseThrow(() -> new InvalidValueException(ErrorCode.INVALID_FIELD_ERROR));
	}
}
