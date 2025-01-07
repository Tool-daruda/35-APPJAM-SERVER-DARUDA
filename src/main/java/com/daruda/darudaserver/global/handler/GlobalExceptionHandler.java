package com.daruda.darudaserver.global.handler;

import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.dto.ErrorResponse;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e){
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        return buildErrorResponse(ErrorCode.INVALID_FIELD_ERROR, e.getBindingResult());
    }

    @ExceptionHandler(ConstraintDeclarationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolation e){
        return buildErrorResponse(ErrorCode.INVALID_FIELD_ERROR, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e){
        return buildErrorResponse(ErrorCode.MISSING_PARAMETER, e.getParameterName());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e){
        return buildErrorResponse(ErrorCode.MISSING_HEADER, e.getHeaderName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException e){
        String detail = e.getRequiredType() != null
                ? String.format("'%s'은(는) %s 타입이어야 합니다.", e.getName(), e.getRequiredType().getSimpleName())
                : "타입 변환 오류입니다.";
        return buildErrorResponse(ErrorCode.TYPE_MISMATCH, detail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return buildErrorResponse(ErrorCode.DATA_INTEGRITY_VIOLATION, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }


    private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, Object detail){
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, detail));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        return ResponseEntity.status(500).body("파일 처리 중 오류 발생: " + ex.getMessage());
    }
}
