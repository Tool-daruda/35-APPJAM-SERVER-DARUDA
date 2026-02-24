package com.daruda.darudaserver.global.handler;

import java.io.IOException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.dto.ErrorResponse;
import com.daruda.darudaserver.global.error.exception.BusinessException;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		log.debug("BusinessException 발생: {}", ex.getErrorCode().getCode(), ex);
		return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
			.body(ErrorResponse.of(ex.getErrorCode()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		log.debug("MethodArgumentNotValidException 발생", ex);
		return ResponseEntity.status(ErrorCode.INVALID_FIELD_ERROR.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INVALID_FIELD_ERROR, ex.getBindingResult()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
		log.debug("ConstraintViolationException 발생", ex);
		return ResponseEntity.status(ErrorCode.INVALID_FIELD_ERROR.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INVALID_FIELD_ERROR, ex.getConstraintViolations()));
	}

	@ExceptionHandler(ConstraintDeclarationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintDeclarationException(ConstraintDeclarationException ex) {
		log.error("ConstraintDeclarationException 발생", ex);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		log.warn("IllegalArgumentException 발생: {}", ex.getMessage(), ex);
		return ResponseEntity.status(ErrorCode.BAD_REQUEST_DATA.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.BAD_REQUEST_DATA));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
		log.warn("IllegalStateException 발생: {}", ex.getMessage(), ex);
		return ResponseEntity.status(ErrorCode.BAD_REQUEST_DATA.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.BAD_REQUEST_DATA));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException ex) {
		log.debug("MissingServletRequestParameterException 발생: {}", ex.getParameterName(), ex);
		return buildErrorResponse(ErrorCode.MISSING_PARAMETER, ex.getParameterName());
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
		log.debug("MissingRequestHeaderException 발생: {}", ex.getHeaderName(), ex);
		return buildErrorResponse(ErrorCode.MISSING_HEADER, ex.getHeaderName());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
		log.debug("MethodArgumentTypeMismatchException 발생", ex);
		String detail = ex.getRequiredType() != null
			? String.format("'%s'은(는) %s 타입이어야 합니다.", ex.getName(), ex.getRequiredType().getSimpleName())
			: "타입 변환 오류입니다.";
		return buildErrorResponse(ErrorCode.TYPE_MISMATCH, detail);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
		log.warn("DataIntegrityViolationException 발생", ex);
		return ResponseEntity.status(ErrorCode.DATA_INTEGRITY_VIOLATION.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.DATA_INTEGRITY_VIOLATION));
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ErrorResponse> handleIoException(IOException ex) {
		log.error("IOException 발생", ex);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	@ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
	public ResponseEntity<ErrorResponse> handleMultipartException(Exception ex) {
		log.error("파일 업로드 예외 발생", ex);
		return ResponseEntity.status(ErrorCode.FILE_UPLOAD_FAIL.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.FILE_UPLOAD_FAIL));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(HandlerMethodValidationException ex) {
		log.debug("HandlerMethodValidationException 발생", ex);
		return buildErrorResponse(ErrorCode.INVALID_FIELD_ERROR, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		log.error("처리되지 않은 예외 발생", ex);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, String detail) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ErrorResponse.of(errorCode, detail));
	}
}
