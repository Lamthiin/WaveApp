package com.ptithcm.waveapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.service.AuthService;
import com.ptithcm.waveapp.util.EmailHelper;
import com.ptithcm.waveapp.util.PasswordValidator;
import com.ptithcm.waveapp.util.TokenManager;

public class RegisterEmailActivity extends AppCompatActivity {

    private EditText       etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnNext;
    private ProgressBar    progressBar;
    private AuthService    authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);

        try {
            DatabaseHelper dbHelper   = DatabaseHelper.getInstance(this);
            UserRepository userRepo   = new UserRepository(dbHelper);
            TokenManager tokenManager = new TokenManager(this);
            authService = new AuthService(userRepo, tokenManager);

            etEmail           = findViewById(R.id.et_email);
            etPassword        = findViewById(R.id.et_new_password);
            etConfirmPassword = findViewById(R.id.et_confirm_new_password);
            btnNext           = findViewById(R.id.btn_next);
            progressBar       = findViewById(R.id.progressBar);

            ImageButton btnBack = findViewById(R.id.btn_back);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            btnNext.setOnClickListener(v -> {
                String email    = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirm  = etConfirmPassword.getText().toString().trim();

                if (email.isEmpty())           { etEmail.setError("Vui lòng nhập email"); return; }
                if (!email.contains("@"))      { etEmail.setError("Email không hợp lệ"); return; }
                if (!PasswordValidator.isValid(password)) {
                    etPassword.setError(PasswordValidator.getErrorMessage());
                    return;
                }
                if (!password.equals(confirm)) { etConfirmPassword.setError("Mật khẩu không khớp"); return; }

                register(email, password, confirm);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void register(String email, String password, String confirm) {
        setLoading(true);

        new Thread(() -> {
            try {
                String username = email.split("@")[0];

                // 1. Lưu pending + tạo OTP vào Database local
                authService.registerWithEmail(username, username, email, password, confirm);

                // 2. Lấy OTP vừa tạo từ Database
                String otp = authService.getOtp(email);

                // 3. Gửi OTP về email thật (Đã bọc kiểm tra an toàn)
                boolean sent = false;
                try {
                    sent = EmailHelper.sendOtp(email, otp);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final boolean isSent = sent;
                runOnUiThread(() -> {
                    setLoading(false);
                    if (isSent) {
                        Toast.makeText(this, "Mã OTP đã gửi tới " + email, Toast.LENGTH_LONG).show();

                        // Chỉ chuyển màn hình khi đã gửi mail thành công hoàn toàn
                        Intent intent = new Intent(this, OtpVerificationActivity.class);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("SEND_TIME", System.currentTimeMillis());
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Không gửi được email. Vui lòng kiểm tra cấu hình SMTP hoặc App Password!", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void setLoading(boolean loading) {
        if (btnNext != null) btnNext.setEnabled(!loading);
        if (progressBar != null)
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}