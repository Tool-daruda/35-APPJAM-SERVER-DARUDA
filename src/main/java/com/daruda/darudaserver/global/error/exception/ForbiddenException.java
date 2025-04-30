package com.daruda.darudaserver.global.error.exception;

import com.daruda.darudaserver.global.error.code.ErrorCode;

public class ForbiddenException extends BusinessException {
	public ForbiddenException(ErrorCode errorCode) {
		super(errorCode);
	}
}

