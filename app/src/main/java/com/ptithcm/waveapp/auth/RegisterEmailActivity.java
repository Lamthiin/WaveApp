package com.ptithcm.waveapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.config.ServiceLocator;
import com.ptithcm.waveapp.controller.AuthController;
import com.ptithcm.waveapp.dto.request.RegisterRequest;

public class RegisterEmailActivity extends AppCompatActivity {

    private AuthController authController;
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword, etConfirmPassword;
    private static final String TAG = "RegisterEmailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);

        authController = ServiceLocator.getInstance().getAuthController();
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        MaterialButton btnNext = findViewById(R.id.btn_next);
        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng ký với Firebase
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                sendVerificationEmail(user, password);
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterEmailActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void sendVerificationEmail(FirebaseUser user, String password) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterEmailActivity.this,
                                "Link xác thực đã được gửi tới " + user.getEmail(),
                                Toast.LENGTH_LONG).show();
                        
                        // Lưu thông tin vào local mock (để đồng bộ hệ thống hiện tại)
                        RegisterRequest request = new RegisterRequest();
                        request.setEmail(user.getEmail());
                        request.setPassword(password);
                        request.setConfirmPassword(password);
                        request.setName(user.getEmail().split("@")[0]);
                        request.setUsername(user.getEmail().split("@")[0]);
                        authController.registerEmail(request); // Chỉ để lưu vào pendingUsers

                        // Chuyển hướng về Login và nhắc người dùng check mail
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(RegisterEmailActivity.this,
                                "Không thể gửi mail xác thực.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
