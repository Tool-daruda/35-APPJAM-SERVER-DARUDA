package com.daruda.darudaserver.global.error.exception;

import com.daruda.darudaserver.global.error.code.ErrorCode;

public class InvalidValueException extends BusinessException {
	public InvalidValueException(ErrorCode errorCode) {
		super(errorCode);
	}
}
