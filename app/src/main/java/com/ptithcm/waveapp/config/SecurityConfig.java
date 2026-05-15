package com.ptithcm.waveapp.config;

/**
 * LƯU Ý: File này được giữ lại theo yêu cầu nhưng đã được vô hiệu hóa 
 * vì Spring Security không chạy trực tiếp trên Android.
 * Các logic bảo mật của Android sẽ được xử lý qua Firebase hoặc SharedPreferences.
 */
public class SecurityConfig {
    // PasswordEncoder có thể được thay thế bằng một thư viện như JBCrypt nếu cần
    public String encodePassword(String rawPassword) {
        return rawPassword; // Cần thay thế bằng logic mã hóa thực tế
    }
}
