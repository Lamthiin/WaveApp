package com.ptithcm.waveapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ptithcm.waveapp.MainActivity;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.config.ServiceLocator;
import com.ptithcm.waveapp.controller.AuthController;
import com.ptithcm.waveapp.dto.request.LoginRequest;
import com.ptithcm.waveapp.dto.response.ApiResponse;
import com.ptithcm.waveapp.dto.response.AuthResponse;
import com.google.android.material.button.MaterialButton;

public class LoginEmailActivity extends AppCompatActivity {

    private EditText etAccount, etPassword;
    private AuthController authController;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginEmailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        authController = ServiceLocator.getInstance().getAuthController();
        mAuth = FirebaseAuth.getInstance();

        ImageButton btnBack = findViewById(R.id.btn_back);
        etAccount = findViewById(R.id.et_login_account);
        etPassword = findViewById(R.id.et_login_password);
        MaterialButton btnSubmit = findViewById(R.id.btn_login_submit);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng nhập với Firebase
            mAuth.signInWithEmailAndPassword(account, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Nếu mail đã xác thực, tiến hành đăng nhập vào hệ thống app logic
                                proceedLogin(account, password);
                            } else if (user != null) {
                                Toast.makeText(this, "Vui lòng xác thực email trước khi đăng nhập", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        } else {
                            // Thử đăng nhập với Admin (Mock) nếu Firebase thất bại (để test tài khoản admin@wave.com)
                            proceedLogin(account, password);
                        }
                    });
        });
    }

    private void proceedLogin(String account, String password) {
        LoginRequest request = new LoginRequest(account, password);
        ApiResponse<AuthResponse> response = authController.loginEmail(request);

        if (response.isSuccess()) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
