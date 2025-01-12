package com.daruda.darudaserver.global.error.exception;

import com.daruda.darudaserver.global.error.code.ErrorCode;

public class UnauhtorizedException extends BusinessException {
    public UnauhtorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
