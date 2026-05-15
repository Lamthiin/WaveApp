package com.ptithcm.waveapp.exception;

import com.ptithcm.waveapp.dto.response.ApiResponse;

/**
 * Class này được giữ lại để tương thích logic, nhưng @RestControllerAdvice đã được gỡ bỏ.
 */
public class GlobalExceptionHandler {

    public ApiResponse<Void> handleNotFound(ResourceNotFoundException e) {
        return ApiResponse.error(e.getMessage());
    }

    public ApiResponse<Void> handleBadRequest(BadRequestException e) {
        return ApiResponse.error(e.getMessage());
    }

    public ApiResponse<Void> handleGeneral(Exception e) {
        return ApiResponse.error("Đã xảy ra lỗi: " + e.getMessage());
    }
}
