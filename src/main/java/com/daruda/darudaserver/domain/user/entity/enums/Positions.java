package com.daruda.darudaserver.domain.user.entity.enums;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Positions {
	STUDENT("학생", "STUDENT"),
	WORKER("직장인", "WORKER"),
	NORMAL("일반인", "NORMAL"),
	ADMIN("관리자", "ADMIN");

	private final String name;
	private final String engName;

	public static Positions fromString(String name) {
		for (Positions position : Positions.values()) {
			if (position.getName().equals(name)) {
				return position;
			}
		}
		throw new BusinessException(ErrorCode.INVALID_FIELD_ERROR);
	}

	public static Positions fromEngName(String engName) {
		for (Positions position : Positions.values()) {
			if (position.getEngName().equalsIgnoreCase(engName)) {
				return position;
			}
		}
		throw new BusinessException(ErrorCode.INVALID_FIELD_ERROR);
	}
}
