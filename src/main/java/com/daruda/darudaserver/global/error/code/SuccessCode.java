package com.daruda.darudaserver.global.error.code;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SuccessCode {
    /* 201 CREATED */
    SUCCESS_CREATE(HttpStatus.CREATED, "생성이 완료되었습니다"),
    SUCCESS_REISSUE(HttpStatus.CREATED,"토큰이 재발급되었습니다"),

    /* 200 OK */
    SUCCESS_UPDATE(HttpStatus.OK, "업데이트가 완료되었습니다"),
    SUCCESS_DELETE(HttpStatus.OK, "삭제가 완료되었습니다"),
    SUCCESS_FETCH(HttpStatus.OK, "요청 데이터가 성공적으로 조회되었습니다"),
    SUCCESS_SCRAP(HttpStatus.OK,  "스크랩이 변경이 완료되었습니다"),
    SUCCESS_LOGIN(HttpStatus.OK, "로그인 성공했습니다"),
    SUCCESS_LOGUT(HttpStatus.OK,"로그아웃에 성공했습니다");

    private final HttpStatus httpStatus;
    private final String message;

    public HttpStatus getHttpStatus(){
        return httpStatus;
    }

    public String getMessage(){
        return message;
    }
}
