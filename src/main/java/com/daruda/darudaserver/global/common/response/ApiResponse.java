package com.daruda.darudaserver.global.common.response;

import com.daruda.darudaserver.global.error.BusinessException;
import com.daruda.darudaserver.global.error.dto.ErrorResponse;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        HttpStatus httpStatus,
        boolean success,
        @Nullable T data,
        @Nullable ErrorResponse errorResponse
        ) {
    public static <T> ApiResponse<T> ok(@Nullable final T data){
        return new ApiResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> ApiResponse<T> created(@Nullable final T data){
        return new ApiResponse<>(HttpStatus.CREATED, true, data, null);
    }

    public static <T> ApiResponse<T> fail(final BusinessException e){
        return new ApiResponse<>(e.getErrorCode().getHttpStatus(), false, null, ErrorResponse.of(e.getErrorCode()));
    }
}
