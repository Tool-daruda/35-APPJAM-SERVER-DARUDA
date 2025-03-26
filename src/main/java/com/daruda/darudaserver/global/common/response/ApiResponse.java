package com.daruda.darudaserver.global.common.response;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
	int statusCode,
	String message,
	T data
) {
	public static <T> ApiResponse<T> ofSuccessWithData(T data, SuccessCode successCode) {
		return new ApiResponse<>(successCode.getHttpStatus().value(), successCode.getMessage(), data);
	}

	public static <T> ApiResponse<T> ofSuccess(SuccessCode successCode) {
		return new ApiResponse<>(successCode.getHttpStatus().value(), successCode.getMessage(), null);
	}

	public static <T> ApiResponse<String> ofFailure(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getHttpStatus().value(), errorCode.getCode(), errorCode.getMessage());
	}

}
