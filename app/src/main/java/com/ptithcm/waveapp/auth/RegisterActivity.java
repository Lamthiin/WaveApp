package com.ptithcm.waveapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import com.ptithcm.waveapp.MainActivity;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.util.TokenManager;

public class RegisterActivity extends AppCompatActivity {

    // Khai báo các thuộc tính để quản lý Google SDK và Database
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private TokenManager tokenManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Khởi tạo TokenManager và Database/Repository
        tokenManager = new TokenManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userRepository = new UserRepository(dbHelper);

        // 2. Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("177509917673-jjcq8pa51835d9ohuih51u5tt287t5fp.apps.googleusercontent.com")
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 3. Đăng ký Launcher để nhận kết quả trả về từ Google
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
        );

        // 4. Ánh xạ View
        MaterialButton btnEmail  = findViewById(R.id.btn_continue_email);
        MaterialButton btnGoogle = findViewById(R.id.btn_continue_google);
        TextView tvLogin         = findViewById(R.id.tv_register_link);

        // Chuyển sang màn đăng ký bằng Email
        btnEmail.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterEmailActivity.class))
        );

        // Xử lý sự kiện khi bấm nút "Tiếp tục với Google"
        btnGoogle.setOnClickListener(v -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        // Quay lại màn đăng nhập
        tvLogin.setOnClickListener(v -> finish());
    }

    // Hàm xử lý kết quả sau khi người dùng chọn tài khoản Google
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                // Trích xuất các thông tin
                String googleId = account.getId();
                String name     = account.getDisplayName();
                String email    = account.getEmail();
                String avatar   = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
                String username = email != null ? email.split("@")[0] : "gg_user";

                // Lưu (hoặc cập nhật) người dùng vào SQLite Database
                userRepository.saveOrUpdateGoogleUser(googleId, username, email, name, avatar);

                // Lưu trạng thái phiên đăng nhập
                tokenManager.saveLogin(googleId, username, name, email, avatar, "USER");

                Toast.makeText(this, "Đăng ký bằng Google thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển thẳng tới trang chủ và xóa lịch sử Activity hiện tại
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (ApiException e) {
            e.printStackTrace();
            // Bỏ qua mã lỗi 12501 (khi người dùng chủ động tắt hộp thoại chọn tài khoản)
            if (e.getStatusCode() != 12501) {
                Toast.makeText(this, "Lỗi kết nối Google (Mã lỗi: " + e.getStatusCode() + ")", Toast.LENGTH_LONG).show();
            }
        }
    }
}