package com.ptithcm.waveapp.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().success(true).message("Success").data(data).build();
    }
    public static <T> ApiResponse<T> success(String msg, T data) {
        return ApiResponse.<T>builder().success(true).message(msg).data(data).build();
    }
    public static <T> ApiResponse<T> error(String msg) {
        return ApiResponse.<T>builder().success(false).message(msg).build();
    }
}
