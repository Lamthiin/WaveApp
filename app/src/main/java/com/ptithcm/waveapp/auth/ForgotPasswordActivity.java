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

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText       etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSubmit;
    private ProgressBar    progressBar;
    private UserRepository userRepo;
    private AuthService    authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thay vì dùng chung layout đăng ký, ta gắn với layout chuyên dụng của quên mật khẩu
        setContentView(R.layout.activity_forgot_password);

        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            userRepo = new UserRepository(dbHelper);
            authService = new AuthService(userRepo, new TokenManager(this));

            // ✅ ĐÃ SỬA: Khớp hoàn toàn với bộ ID mới của activity_forgot_password.xml
            etEmail           = findViewById(R.id.et_forgot_email);
            etPassword        = findViewById(R.id.et_forgot_new_password);
            etConfirmPassword = findViewById(R.id.et_forgot_confirm_new_password);
            btnSubmit         = findViewById(R.id.btn_forgot_submit);
            progressBar       = findViewById(R.id.forgot_progressBar);

            ImageButton btnBack = findViewById(R.id.btn_forgot_back);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            btnSubmit.setOnClickListener(v -> {
                String email    = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirm  = etConfirmPassword.getText().toString().trim();

                // Validation tương tự như màn hình Register của bạn
                if (email.isEmpty())           { etEmail.setError("Vui lòng nhập email"); return; }
                if (!email.contains("@"))      { etEmail.setError("Email không hợp lệ"); return; }
                if (!PasswordValidator.isValid(password)) {
                    etPassword.setError(PasswordValidator.getErrorMessage());
                    return;
                }
                if (!password.equals(confirm)) { etConfirmPassword.setError("Mật khẩu không khớp"); return; }

                checkAndSendOtp(email, password);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkAndSendOtp(String email, String newPassword) {
        setLoading(true);

        new Thread(() -> {
            // 1. Kiểm tra xem tài khoản có tồn tại trong hệ thống không để cho khôi phục
            if (!userRepo.existsByEmail(email)) {
                runOnUiThread(() -> {
                    setLoading(false);
                    etEmail.setError("Email này chưa được đăng ký tài khoản!");
                });
                return;
            }

            try {
                // 2. Sử dụng AuthService để tạo mã OTP và lưu vào storage (thống nhất với Register)
                authService.sendOtp(email);
                String otp = authService.getOtp(email);

                // 3. Gửi OTP về hòm thư thật
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
                        Toast.makeText(this, "Mã OTP đã gửi tới " + email, Toast.LENGTH_SHORT).show();

                        // 4. Chuyển sang màn hình OTP, ôm theo đầy đủ thông tin để rẽ nhánh
                        Intent intent = new Intent(this, OtpVerificationActivity.class);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("TYPE", "FORGOT");          // Cờ rẽ nhánh
                        intent.putExtra("OTP_SECRET", otp);         // OTP hệ thống sinh ra ngầm
                        intent.putExtra("NEW_PASSWORD", newPassword); // Mật khẩu mới để tí update
                        intent.putExtra("SEND_TIME", System.currentTimeMillis());
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Không gửi được email. Vui lòng kiểm tra lại cấu hình SMTP hoặc App Password!", Toast.LENGTH_LONG).show();
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
        if (btnSubmit != null) btnSubmit.setEnabled(!loading);
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }
}