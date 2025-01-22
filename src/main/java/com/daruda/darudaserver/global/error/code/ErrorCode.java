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
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "E400007","데이터 무결성 제약 조건을 위반했습니다"),
    REFRESH_TOKEN_USER_ID_MISMATCH_ERROR(HttpStatus.BAD_REQUEST,"E400009", "리프레쉬 토큰의 사용자 정보가 일치하지 않습니다"),
    INVALID_REFRESH_TOKEN_ERROR(HttpStatus.BAD_REQUEST,"E400010", "잘못된 리프레쉬 토큰입니다"),
    REFRESH_TOKEN_SIGNATURE_ERROR(HttpStatus.BAD_REQUEST,"E400011", "리프레쉬 토큰의 서명이 잘못되었습니다"),
    UNSUPPORTED_REFRESH_TOKEN_ERROR(HttpStatus.BAD_REQUEST,"E400012", "지원하지 않는 리프레쉬 토큰입니다"),
    REFRESH_TOKEN_EMPTY_ERROR(HttpStatus.BAD_REQUEST,"E400013", "리프레쉬 토큰이 비어있습니다"),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "E400009", "지원하지 않는 이미지 확장자 입니다."),
    INVALID_TOOL_CATEGORY(HttpStatus.BAD_REQUEST, "E400012", "존재하지 않는 카테고리 입니다."),
    INVALID_PAGE_MIN_SIZE(HttpStatus.BAD_REQUEST, "E400013", "페이지는 1 이상이어야 합니다."),
    INVALID_PAGE_MAX_SIZE(HttpStatus.BAD_REQUEST, "E400014", "한 번에 18개 이하만 조회할 수 있습니다."),
    REFREH_TOKEN_EMPTY_ERROR(HttpStatus.BAD_REQUEST,"E400015","리프레시 토큰이 비었습니다"),
    /* 401 */

    AUTHENTICATION_CODE_EXPIRED(HttpStatus.UNAUTHORIZED,"E401001", "인가코드가 만료되었습니다"),
    REFRESH_TOKEN_EXPIRED_ERROR(HttpStatus.UNAUTHORIZED,"E401002","리프레쉬 토큰이 만료되었습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401003", "리소스 접근 권한이 없습니다."),
    EMPTY_OR_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E401004", "토큰이 존재하지 않거나 유효하지 않습니다"),

    /* 403 */
    BOARD_FORBIDDEN(HttpStatus.FORBIDDEN, "E403001", "게시판 접근 권한이 없습니다."),

    /* 404 NOT FOUND */

    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "E404001","데이터가 존재하지 않습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E404002", "유저가 존재하지 않습니다"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND,"E404003", "리프레쉬 토큰이 존재하지 않습니다"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"E404004","댓글이 존재하지 않습니다"),
    TOOL_NOT_FOUND(HttpStatus.NOT_FOUND,"E404005","툴 존재하지 않습니다"),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND,"E404006","게시글이 존재하지 않습니다"),
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND,"E404006","스크랩이 존재하지 않습니다"),
    /* 409 CONFLICT */
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT,"E409001","닉네임 중복입니다"),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT,"E409002","이메일 중복입니다"),

    /* 500 INTERNAL SERVER ERROR */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500001","서버 내부에서 오류가 발생했습니다"),
    FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "E500002", "이미지 업로드에 실패했습니다"),
    FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "E500003", "이미지를 찾을 수 없습니다"),
    FILE_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "E500004", "이미지 삭제에 실패했습니다");

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




