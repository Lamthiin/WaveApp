package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.ptithcm.waveapp.auth.LoginActivity;
import com.ptithcm.waveapp.util.TokenManager;

public class UserProfileActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        // 1. Khởi tạo các Manager
        tokenManager = new TokenManager(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 2. Xử lý khoảng trống hệ thống (Status bar, Navigation bar)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 3. Xử lý nút Back
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 4. Xử lý nút Đăng xuất (Gán sự kiện trực tiếp)
        View btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Toast.makeText(UserProfileActivity.this, "Đang đăng xuất...", Toast.LENGTH_SHORT).show();
                performLogout();
            });
        }
    }

    private void performLogout() {
        // Xóa session local ngay lập tức
        if (tokenManager != null) {
            tokenManager.logout();
        }

        // Đăng xuất Google
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                navigateToLogin();
            });
        } else {
            navigateToLogin();
        }

        // Cơ chế dự phòng: Nếu sau 1.5 giây Google chưa phản hồi, vẫn sẽ chuyển màn hình
        View btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.postDelayed(() -> {
                if (!isFinishing()) {
                    navigateToLogin();
                }
            }, 1500);
        }
    }

    private void navigateToLogin() {
        if (isFinishing()) return;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}