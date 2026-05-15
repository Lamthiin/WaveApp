package com.ptithcm.waveapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.ptithcm.waveapp.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialButton btnContinueEmail = findViewById(R.id.btn_continue_email);
        MaterialButton btnContinueGoogle = findViewById(R.id.btn_continue_google);
        TextView tvRegisterLink = findViewById(R.id.tv_register_link);

        btnContinueEmail.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, LoginEmailActivity.class));
        });

        btnContinueGoogle.setOnClickListener(v -> {
            // Logic Google Login (Mock hoặc Firebase)
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
