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

        MaterialButton btnContinueEmail = findViewById(R.id.btn_continue_email);
        MaterialButton btnContinueGoogle = findViewById(R.id.btn_continue_google);
        TextView tvLoginLink = findViewById(R.id.tv_register_link); // In XML it's labeled tv_register_link but used for "Đăng nhập" here

        btnContinueEmail.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, RegisterEmailActivity.class));
        });

        btnContinueGoogle.setOnClickListener(v -> {
            // Logic Google Register
        });

        tvLoginLink.setOnClickListener(v -> {
            finish();
        });
    }
}
