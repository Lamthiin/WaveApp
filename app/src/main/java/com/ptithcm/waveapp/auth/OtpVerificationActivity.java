package com.ptithcm.waveapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.ptithcm.waveapp.MainActivity;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.config.ServiceLocator;
import com.ptithcm.waveapp.controller.AuthController;
import com.ptithcm.waveapp.dto.request.OtpVerifyRequest;
import com.ptithcm.waveapp.dto.response.ApiResponse;
import com.ptithcm.waveapp.dto.response.AuthResponse;

public class OtpVerificationActivity extends AppCompatActivity {

    private AuthController authController;
    private EditText[] otpInputs = new EditText[6];
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        authController = ServiceLocator.getInstance().getAuthController();
        email = getIntent().getStringExtra("EMAIL");

        initViews();
        setupOtpInputs();
    }

    private void initViews() {
        otpInputs[0] = findViewById(R.id.otp_1);
        otpInputs[1] = findViewById(R.id.otp_2);
        otpInputs[2] = findViewById(R.id.otp_3);
        otpInputs[3] = findViewById(R.id.otp_4);
        otpInputs[4] = findViewById(R.id.otp_5);
        otpInputs[5] = findViewById(R.id.otp_6);

        MaterialButton btnNext = findViewById(R.id.btn_next);
        TextView tvResend = findViewById(R.id.tv_resend);
        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> verifyOtp());

        tvResend.setOnClickListener(v -> {
            ApiResponse<Void> response = authController.resendOtp(email);
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupOtpInputs() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) {
                        otpInputs[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void verifyOtp() {
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText input : otpInputs) {
            otpBuilder.append(input.getText().toString());
        }
        String otp = otpBuilder.toString();

        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setIdentifier(email);
        request.setOtp(otp);

        ApiResponse<AuthResponse> response = authController.verifyOtp(request);
        if (response.isSuccess()) {
            Toast.makeText(this, "Xác thực thành công! Đang đăng nhập...", Toast.LENGTH_SHORT).show();
            
            // Chuyển hướng vào màn hình chính (MainActivity)
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
