package com.daruda.darudaserver.global.error.code;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorCode {
    /* 400 Bad Request */

    BAD_REQUEST_DATA(HttpStatus.BAD_REQUEST,"E400001","잘못된 요청입니다"),
    INVALID_FIELD_ERROR(HttpStatus.BAD_REQUEST, "E400002","요청 필드 값이 유효하지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "E400003","필수 요청 파라미터가 누락되었습니다"),
    MISSING_HEADER(HttpStatus.BAD_REQUEST, "E400004","필수 요청 헤더가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "E400005","요청 값 타입이 올바르지 않습니다"),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "E400006","요청 본문이 올바르지 않습니다"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "E400007","데이터 무결성 제약 조건을 위반했습니다"),
    BUSINESS_LOGIC_ERROR(HttpStatus.BAD_REQUEST, "E400008","비즈니스 로직 처리 중 오류가 발생했습니다"),

    /* 404 NOT FOUND */

    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "E404001","데이터가 존재하지 않습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E404002", "유저가 존재하지 않습니다"),

    /* 500 INTERNAL SERVER ERROR */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500001","서버 내부에서 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public HttpStatus getHttpStatus(){
        return httpStatus;
    }
    public String getCode() {return code;}
    public String getMessage(){
        return message;
    }
}




