package com.daruda.darudaserver.global.error.exception;

import com.daruda.darudaserver.global.error.code.ErrorCode;

public class NotFoundException extends BusinessException {
	public NotFoundException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
