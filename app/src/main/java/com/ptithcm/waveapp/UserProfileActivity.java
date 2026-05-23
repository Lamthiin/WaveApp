package com.ptithcm.waveapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ptithcm.waveapp.auth.LoginActivity;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.util.PasswordValidator;
import com.ptithcm.waveapp.util.TokenManager;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserProfileActivity extends BaseMiniPlayerActivity {

    private TokenManager tokenManager;
    private UserRepository userRepo;
    private GoogleSignInClient mGoogleSignInClient;

    private ShapeableImageView imgAvatar;
    private FloatingActionButton fabChangeAvatar;
    private TextView tvUsername, tvEmail, tvLastUpdated;
    private EditText etDisplayName, etOldPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnSaveInfo, btnChangePassword;

    private ActivityResultLauncher<String> avatarPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        tokenManager = new TokenManager(this);
        userRepo = new UserRepository(DatabaseHelper.getInstance(this));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) uploadAvatarToFirebase(uri);
                }
        );

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        imgAvatar = findViewById(R.id.img_avatar);
        fabChangeAvatar = findViewById(R.id.fab_change_avatar);

        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvLastUpdated = findViewById(R.id.tv_last_updated);
        etDisplayName = findViewById(R.id.et_display_name);

        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnSaveInfo = findViewById(R.id.btn_save_info);
        btnChangePassword = findViewById(R.id.btn_change_password);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadUserInfo();

        fabChangeAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        btnSaveInfo.setOnClickListener(v -> updateProfileInfo());
        btnChangePassword.setOnClickListener(v -> changePasswordFromProfile());

        View btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Toast.makeText(this, "Đang đăng xuất...", Toast.LENGTH_SHORT).show();
                performLogout();
            });
        }
    }

    private void showLastUpdated(LocalDateTime updatedAt) {
        if (tvLastUpdated == null) return;

        if (updatedAt == null) {
            tvLastUpdated.setText("Chưa cập nhật");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        tvLastUpdated.setText("Cập nhật lần cuối: " + updatedAt.format(formatter));
    }

    private void loadUserInfo() {
        String userId = tokenManager.getUserId();

        if (userId == null) {
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        Optional<User> userOpt = userRepo.findById(userId);
        if (!userOpt.isPresent()) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = userOpt.get();

        tvUsername.setText(user.getName());
        tvEmail.setText(user.getEmail());
        etDisplayName.setText(user.getName());
        showLastUpdated(user.getUpdatedAt());

        loadAvatar(user.getAvatar());
    }

    private void loadAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_logo);
        }
    }

    private void uploadAvatarToFirebase(Uri selectedUri) {
        String userId = tokenManager.getUserId();

        if (userId == null) {
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Optional<User> userOpt = userRepo.findById(userId);
        if (!userOpt.isPresent()) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        fabChangeAvatar.setEnabled(false);
        Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show();

        StorageReference avatarRef = FirebaseStorage.getInstance()
                .getReference()
                .child("user_avatars/" + userId + ".jpg");

        avatarRef.putFile(selectedUri)
                .addOnSuccessListener(taskSnapshot ->
                        avatarRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String avatarUrl = downloadUri.toString();

                                    User user = userOpt.get();
                                    user.setAvatar(avatarUrl);
                                    userRepo.save(user);
                                    showLastUpdated(LocalDateTime.now());

                                    tokenManager.saveLogin(
                                            user.getId(),
                                            user.getUsername(),
                                            user.getName(),
                                            user.getEmail(),
                                            avatarUrl,
                                            user.getRole()
                                    );

                                    loadAvatar(avatarUrl);

                                    fabChangeAvatar.setEnabled(true);
                                    Toast.makeText(this, "Cập nhật avatar thành công", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    fabChangeAvatar.setEnabled(true);
                                    Toast.makeText(this, "Không lấy được URL avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    fabChangeAvatar.setEnabled(true);
                    Toast.makeText(this, "Upload avatar thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileInfo() {
        String userId = tokenManager.getUserId();
        String newName = etDisplayName.getText().toString().trim();

        if (newName.isEmpty()) {
            etDisplayName.setError("Vui lòng nhập tên hiển thị");
            return;
        }

        Optional<User> userOpt = userRepo.findById(userId);
        if (!userOpt.isPresent()) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = userOpt.get();
        user.setName(newName);
        userRepo.save(user);
        showLastUpdated(LocalDateTime.now());

        tokenManager.saveLogin(
                user.getId(),
                user.getUsername(),
                newName,
                user.getEmail(),
                user.getAvatar(),
                user.getRole()
        );

        tvUsername.setText(newName);
        etDisplayName.setText(newName);
        etDisplayName.clearFocus();

        Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
    }

    private void changePasswordFromProfile() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (oldPassword.isEmpty()) {
            etOldPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            return;
        }

        if (!PasswordValidator.isValid(newPassword)) {
            etNewPassword.setError(PasswordValidator.getErrorMessage());
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        String email = tokenManager.getEmail();

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (!userOpt.isPresent()) {
            Toast.makeText(this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = userOpt.get();

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            Toast.makeText(this, "Tài khoản Google không dùng mật khẩu nội bộ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            etOldPassword.setError("Mật khẩu hiện tại không đúng");
            return;
        }

        userRepo.updatePassword(email, newPassword);

        etOldPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");

        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
    }

    private void performLogout() {
        if (tokenManager != null) tokenManager.logout();

        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> navigateToLogin());
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        if (isFinishing()) return;

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}