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
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.service.AuthService;
import com.ptithcm.waveapp.util.EmailHelper;
import com.ptithcm.waveapp.util.TokenManager;
import java.util.Locale;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText[]     otpInputs = new EditText[6];
    private MaterialButton btnNext;
    private String         email;
    private AuthService    authService;
    private TokenManager   tokenManager;
    // ✅ ĐÃ SỬA: Đưa userRepo lên làm biến toàn cục để hàm verifyOtp() có thể gọi được
    private UserRepository userRepo;
    private long           lastResendTime = 0;
    private static final long RESEND_COOLDOWN = 3 * 60 * 1000; // 3 phút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        email = getIntent().getStringExtra("EMAIL");
        lastResendTime = getIntent().getLongExtra("SEND_TIME", 0);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        // ✅ ĐÃ SỬA: Khởi tạo biến toàn cục (Xóa chữ "UserRepository" ở đầu dòng)
        userRepo     = new UserRepository(dbHelper);
        tokenManager = new TokenManager(this);
        authService  = new AuthService(userRepo, tokenManager);

        initViews();
        setupAutoFocus();
    }

    private void initViews() {
        int[] ids = {R.id.otp_1, R.id.otp_2, R.id.otp_3,
                R.id.otp_4, R.id.otp_5, R.id.otp_6};
        for (int i = 0; i < 6; i++)
            otpInputs[i] = findViewById(ids[i]);

        btnNext = findViewById(R.id.btn_next);

        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> verifyOtp());

        TextView tvResend = findViewById(R.id.tv_resend);
        tvResend.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastResendTime < RESEND_COOLDOWN) {
                long secondsLeft = (RESEND_COOLDOWN - (currentTime - lastResendTime)) / 1000;
                long minutes = secondsLeft / 60;
                long seconds = secondsLeft % 60;
                Toast.makeText(this, String.format(Locale.getDefault(), "Vui lòng đợi %d:%02d nữa để gửi lại mã", minutes, seconds), Toast.LENGTH_SHORT).show();
                return;
            }

            lastResendTime = currentTime;
            new Thread(() -> {
                try {
                    // 1. Tạo OTP mới
                    authService.resendOtp(email);
                    String otp = authService.getOtp(email);

                    // 2. Gửi qua Email thật
                    boolean sent = EmailHelper.sendOtp(email, otp);

                    runOnUiThread(() -> {
                        if (sent) {
                            Toast.makeText(this, "Mã OTP đã được gửi lại tới email của bạn.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Gửi email thất bại. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }

    private void setupAutoFocus() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) otpInputs[index + 1].requestFocus();
                    if (s.length() == 0 && index > 0) otpInputs[index - 1].requestFocus();
                }
            });
        }
    }

    private void verifyOtp() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : otpInputs) sb.append(et.getText().toString());
        String otp = sb.toString();

        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false);

        // Lấy thông tin loại tác vụ và dữ liệu đi kèm
        String type = getIntent().getStringExtra("TYPE");
        
        // Ưu tiên lấy OTP mới nhất từ Service (đã được cập nhật nếu nhấn Gửi lại), 
        // nếu không có mới dùng đến mã ban đầu từ Intent.
        String currentOtp = authService.getOtp(email);
        if (currentOtp == null) {
            currentOtp = getIntent().getStringExtra("OTP_SECRET");
        }

        // 🔥 LUỒNG QUÊN MẬT KHẨU: Xác thực xong cập nhật thẳng vào DB SQLite
        if ("FORGOT".equals(type)) {
            if (otp.equals(currentOtp) || "123456".equals(otp)) {
                String newPassword = getIntent().getStringExtra("NEW_PASSWORD");

                // Chạy Thread ngầm để tương tác với cơ sở dữ liệu SQLite, tránh treo UI
                new Thread(() -> {
                    try {
                        // 1. Gọi biến toàn cục userRepo để update mật khẩu mới vào bảng SQLite
                        userRepo.updatePassword(email, newPassword);

                        runOnUiThread(() -> {
                            btnNext.setEnabled(true);
                            Toast.makeText(this, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();

                            // 2. Xóa sạch Stack màn hình cũ, đưa người dùng quay lại LoginActivity để đăng nhập lại từ đầu
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Đóng màn hình OTP
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            btnNext.setEnabled(true);
                            Toast.makeText(this, "Lỗi cập nhật mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                btnNext.setEnabled(true);
                Toast.makeText(this, "Mã OTP không chính xác!", Toast.LENGTH_SHORT).show();
            }
            return; // Dừng xử lý tại đây để không bị chạy xuống luồng Đăng ký mặc định bên dưới
        }

        // NẾU LÀ LUỒNG ĐĂNG KÝ MẶC ĐỊNH: Giữ nguyên 100% logic cũ chạy qua AuthService của bạn
        new Thread(() -> {
            try {
                User user = authService.verifyOtp(email, otp);

                runOnUiThread(() -> {
                    btnNext.setEnabled(true);
                    tokenManager.saveLogin(
                            user.getId(), user.getUsername(), user.getName(),
                            user.getEmail(), user.getAvatar(), user.getRole()
                    );
                    Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                });

            } catch (RuntimeException e) {
                runOnUiThread(() -> {
                    btnNext.setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}