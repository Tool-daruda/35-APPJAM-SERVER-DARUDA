package com.daruda.darudaserver.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String message,
        int statusCode,
        T data,
        HttpStatus status
        ) {
    public static <T> ApiResponse<T> ofSuccess(T data){
        return new ApiResponse<>("SUCCESS", HttpStatus.OK.value(), data, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> ofFailure(String message, HttpStatus status){
        return new ApiResponse<>(message, status.value(), null, status);
    }

    public static <T> ApiResponse<T> ofSuccess(T data, String message){
        return new ApiResponse<>(message, HttpStatus.OK.value(), data, HttpStatus.OK);
    }
}
