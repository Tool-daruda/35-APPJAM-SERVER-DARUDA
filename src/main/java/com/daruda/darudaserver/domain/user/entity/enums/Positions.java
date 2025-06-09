package com.daruda.darudaserver.domain.user.entity.enums;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Positions {
	STUDENT("학생"),
	WORKER("직장인"),
	NORMAL("일반인"),
	ADMIN("관리자");

	private final String name;

	public static Positions fromString(String name) {
		for (Positions position : Positions.values()) {
			if (position.getName().equals(name)) {
				return position;
			}
		}
		throw new BusinessException(ErrorCode.INVALID_FIELD_ERROR);
	}
}
