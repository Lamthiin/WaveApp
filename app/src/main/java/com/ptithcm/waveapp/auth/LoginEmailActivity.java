package com.ptithcm.waveapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.ptithcm.waveapp.MainActivity;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.service.AuthService;
import com.ptithcm.waveapp.util.TokenManager;

public class LoginEmailActivity extends AppCompatActivity {

    private EditText       etAccount, etPassword;
    private MaterialButton btnSubmit;
    // 🔥 ĐÃ THÊM: Khai báo TextView cho sự kiện Quên mật khẩu
    private TextView       tvForgotPassword;

    private AuthService  authService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        UserRepository userRepo = new UserRepository(dbHelper);
        tokenManager = new TokenManager(this);
        authService  = new AuthService(userRepo, tokenManager);

        etAccount  = findViewById(R.id.et_login_account);
        etPassword = findViewById(R.id.et_login_password);
        btnSubmit  = findViewById(R.id.btn_login_submit);

        // 🔥 ĐÃ THÊM: Ánh xạ View từ XML thông qua ID đã thiết lập ở các bước trước
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 🔥 ĐÃ THÊM: Xử lý sự kiện click để chuyển hướng sang màn hình nhập Email khôi phục
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        btnSubmit.setOnClickListener(v -> {
            String account  = etAccount.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (account.isEmpty())  { etAccount.setError("Vui lòng nhập email hoặc tên người dùng"); return; }
            if (password.isEmpty()) { etPassword.setError("Vui lòng nhập mật khẩu"); return; }

            login(account, password);
        });
    }

    private void login(String account, String password) {
        btnSubmit.setEnabled(false);

        new Thread(() -> {
            try {
                User user = authService.loginWithEmail(account, password);

                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    tokenManager.saveLogin(
                            user.getId(), user.getUsername(), user.getName(),
                            user.getEmail(), user.getAvatar(), user.getRole()
                    );
                    Toast.makeText(this, "Chào mừng " + user.getName(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                });

            } catch (RuntimeException e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}