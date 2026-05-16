package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.ptithcm.waveapp.auth.LoginActivity;
import com.ptithcm.waveapp.util.TokenManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // LUÔN LUÔN chuyển đến LoginActivity để ép buộc người dùng xem màn hình Đăng nhập/Đăng ký
        // Chúng ta tạm thời bỏ qua kiểm tra tokenManager.isLoggedIn() để bạn test giao diện
        Intent intent = new Intent(this, com.ptithcm.waveapp.auth.LoginActivity.class);
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}