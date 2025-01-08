package com.daruda.darudaserver.global.error.exception;

import com.daruda.darudaserver.global.error.code.ErrorCode;

public class BadRequestException extends BusinessException{
    public BadRequestException(){
        super(ErrorCode.BAD_REQUEST_DATA);
    }
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}

