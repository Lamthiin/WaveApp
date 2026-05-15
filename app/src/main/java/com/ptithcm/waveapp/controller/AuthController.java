package com.ptithcm.waveapp.controller;

import com.ptithcm.waveapp.dto.request.*;
import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.service.AuthService;
import lombok.RequiredArgsConstructor;

/**
 * Controller này đã được chuyển đổi sang logic Java thuần để chạy trên Android.
 * Các Activity có thể gọi trực tiếp các phương thức ở đây để xử lý logic đăng nhập/đăng ký.
 */
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /** activity_login_email: btn_login_submit */
    public ApiResponse<AuthResponse> loginEmail(LoginRequest req) {
        try {
            return ApiResponse.success("Đăng nhập thành công",
                    authService.loginWithEmail(req.getIdentifier(), req.getPassword()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /** activity_login + activity_register_google: btn_continue_google */
    public ApiResponse<AuthResponse> loginGoogle(GoogleAuthRequest req) {
        try {
            return ApiResponse.success("Đăng nhập Google thành công",
                    authService.loginWithGoogle(req.getIdToken()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /** activity_register_email: btn_next (gửi OTP) */
    public ApiResponse<Void> registerEmail(RegisterRequest req) {
        try {
            authService.registerWithEmail(req);
            return ApiResponse.success("Mã OTP đã gửi đến " + req.getEmail(), null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /** activity_otp_verification: tv_resend */
    public ApiResponse<Void> resendOtp(String identifier) {
        try {
            authService.resendOtp(identifier);
            return ApiResponse.success("Đã gửi lại mã OTP", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /** activity_otp_verification: btn_next */
    public ApiResponse<AuthResponse> verifyOtp(OtpVerifyRequest req) {
        try {
            return ApiResponse.success("Xác thực thành công",
                    authService.verifyOtp(req));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<AuthResponse> refresh(String refreshToken) {
        try {
            return ApiResponse.success(authService.refresh(refreshToken));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
