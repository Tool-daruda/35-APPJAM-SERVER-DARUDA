package com.daruda.darudaserver.global.error.exception;

import com.daruda.darudaserver.global.error.code.ErrorCode;

public class UnauthorizedException  extends BusinessException{
    public UnauthorizedException(){
        super(ErrorCode.UNAUTHORIZED);
    }
    public UnauthorizedException(ErrorCode errorCode){
        super(errorCode);
    }

}
