package com.ptithcm.waveapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.ptithcm.waveapp.R;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MaterialButton btnEmail  = findViewById(R.id.btn_continue_email);
        MaterialButton btnGoogle = findViewById(R.id.btn_continue_google);
        TextView tvLogin         = findViewById(R.id.tv_register_link);

        btnEmail.setOnClickListener(v ->
            startActivity(new Intent(this, RegisterEmailActivity.class))
        );

        btnGoogle.setOnClickListener(v -> {
            // TODO: tích hợp Google Sign-In SDK nếu cần
        });

        // Quay lại màn đăng nhập
        tvLogin.setOnClickListener(v -> finish());
    }
}
