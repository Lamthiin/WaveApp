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
import com.ptithcm.waveapp.AdminUserManagementActivity;
import com.ptithcm.waveapp.MainActivity;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.util.TokenManager;

public class LoginActivity extends AppCompatActivity {

    // Khai báo các thuộc tính để quản lý Google SDK
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private TokenManager tokenManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo TokenManager để quản lý phiên đăng nhập
        tokenManager = new TokenManager(this);

        // Khởi tạo Database và UserRepository
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userRepository = new UserRepository(dbHelper);

        if (tokenManager.isLoggedIn()) {
            String role = tokenManager.getRole();

            Intent intent;
            if (role != null && role.equalsIgnoreCase("ADMIN")) {
                intent = new Intent(this, AdminUserManagementActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // 1. Cấu hình Google Sign-In yêu cầu email và mã chứng thực ID Token
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                // 🔥 QUAN TRỌNG: Hãy dán chuỗi Web Client ID bạn vừa copy ở Bước 1 vào giữa dấu ngoặc kép này nhé!
                .requestIdToken("177509917673-jjcq8pa51835d9ohuih51u5tt287t5fp.apps.googleusercontent.com")
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 2. Đăng ký bộ lắng nghe kết quả (Launcher) xử lý dữ liệu sau khi người dùng chọn Gmail
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
        );

        MaterialButton btnEmail  = findViewById(R.id.btn_continue_email);
        MaterialButton btnGoogle = findViewById(R.id.btn_continue_google);
        TextView tvRegister      = findViewById(R.id.tv_register_link);

        // Chuyển sang màn hình Đăng nhập bằng Email/Mật khẩu (Giữ nguyên luồng của bạn)
        btnEmail.setOnClickListener(v ->
                startActivity(new Intent(this, LoginEmailActivity.class))
        );

        // Xử lý sự kiện khi bấm nút "Tiếp tục với Google"
        btnGoogle.setOnClickListener(v -> {
            // Đăng xuất phiên cũ ngầm để lúc nào bấm nút cũng hiện hộp thoại chọn tài khoản
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        // Chuyển sang màn hình Đăng ký
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    // Hàm xử lý kết quả bóc tách thông tin từ tài khoản Google được chọn
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                // Trích xuất các thông tin cơ bản từ tài khoản Google
                String googleId = account.getId();
                String name     = account.getDisplayName();
                String email    = account.getEmail();
                String avatar   = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
                String username = email != null ? email.split("@")[0] : "gg_user";

                // 1. Đồng bộ thông tin người dùng vào SQLite Database
                userRepository.saveOrUpdateGoogleUser(googleId, username, email, name, avatar);

                // 2. Lưu trạng thái phiên đăng nhập vào hệ thống bằng TokenManager giống hệt LoginEmail
                tokenManager.saveLogin(googleId, username, name, email, avatar, "USER");

                Toast.makeText(this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();

                // Dọn sạch bộ nhớ các màn hình cũ và phi thẳng vào Trang chủ (MainActivity)
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (ApiException e) {
            e.printStackTrace();
            // Code 12501 nghĩa là người dùng bấm ra ngoài để hủy chọn tài khoản
            if (e.getStatusCode() != 12501) {
                Toast.makeText(this, "Lỗi kết nối Google (Mã lỗi: " + e.getStatusCode() + ")", Toast.LENGTH_LONG).show();
            }
        }
    }
}