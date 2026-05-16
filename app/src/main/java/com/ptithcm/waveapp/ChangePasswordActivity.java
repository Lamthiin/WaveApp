package com.ptithcm.waveapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.util.PasswordValidator;
import com.ptithcm.waveapp.util.TokenManager;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnSave;
    private UserRepository userRepo;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userRepo = new UserRepository(dbHelper);
        tokenManager = new TokenManager(this);

        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword     = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_new_password);
        btnSave           = findViewById(R.id.btn_save);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String currentPass = etCurrentPassword.getText().toString().trim();
            String newPass     = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (currentPass.isEmpty()) { etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại"); return; }
            if (!PasswordValidator.isValid(newPass)) {
                etNewPassword.setError(PasswordValidator.getErrorMessage());
                return;
            }
            if (!newPass.equals(confirmPass)) { etConfirmPassword.setError("Mật khẩu xác nhận không khớp"); return; }

            processChangePassword(currentPass, newPass);
        });
    }

    private void processChangePassword(String currentPassword, String newPassword) {
        btnSave.setEnabled(false);

        // Lấy Email của người dùng đang đăng nhập từ TokenManager
        String loggedInEmail = tokenManager.getEmail();

        new Thread(() -> {
            try {
                // 1. Tìm thông tin user trong DB để chéc mật khẩu cũ
                Optional<User> userOpt = userRepo.findByEmail(loggedInEmail);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // 2. Kiểm tra xem mật khẩu cũ người dùng gõ có khớp với DB không (Sử dụng BCrypt)
                    if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                        runOnUiThread(() -> {
                            btnSave.setEnabled(true);
                            etCurrentPassword.setError("Mật khẩu hiện tại không chính xác!");
                        });
                        return;
                    }

                    // 3. Nếu đúng mật khẩu cũ -> Gọi repo cập nhật (Repo sẽ tự mã hóa mật khẩu mới)
                    userRepo.updatePassword(loggedInEmail, newPassword);

                    // ✅ ĐÃ SỬA: Đảm bảo Toast và finish() chạy đồng bộ trên Main Thread
                    runOnUiThread(() -> {
                        Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });

                } else {
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(ChangePasswordActivity.this, "Không tìm thấy thông tin phiên đăng nhập!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                // ✅ ĐÃ SỬA: Đưa toàn bộ việc bật lại nút và Toast báo lỗi vào trong runOnUiThread chống văng app
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(ChangePasswordActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}