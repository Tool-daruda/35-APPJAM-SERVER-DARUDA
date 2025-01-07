package com.daruda.darudaserver.global.exception;

import com.daruda.darudaserver.global.exception.code.ErrorCode;

public class InvalidValueException extends BusinessException{
    public InvalidValueException(){
        super(ErrorCode.BAD_REQUEST_DATA);
    }

    public InvalidValueException(ErrorCode errorCode){
        super(errorCode);
    }
}
