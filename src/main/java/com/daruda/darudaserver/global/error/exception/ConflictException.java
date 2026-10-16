package com.daruda.darudaserver.global.error.exception;

import com.daruda.darudaserver.global.error.code.ErrorCode;

public class ConflictException extends BusinessException {
	public ConflictException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
