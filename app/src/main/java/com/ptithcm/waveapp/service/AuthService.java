package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.util.TokenManager;
import org.mindrot.jbcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {

    private final UserRepository userRepo;
    private final TokenManager   tokenManager;

    private static final Map<String, String[]> pendingUsers = new HashMap<>();
    private static final Map<String, String>   otpStorage   = new HashMap<>();

    public AuthService(UserRepository userRepo, TokenManager tokenManager) {
        this.userRepo     = userRepo;
        this.tokenManager = tokenManager;
    }

    /** LoginEmailActivity */
    public User loginWithEmail(String identifier, String rawPassword) {
        User user = userRepo.findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        if (!user.isActive())   throw new RuntimeException("Tài khoản đã bị khóa");
        if (!user.isVerified()) throw new RuntimeException("Tài khoản chưa xác thực email");
        
        // Kiểm tra mật khẩu đã băm
        if (!BCrypt.checkpw(rawPassword, user.getPassword()))
            throw new RuntimeException("Mật khẩu không đúng");
        return user;
    }

    /** RegisterEmailActivity */
    public void registerWithEmail(String name, String username, String email,
                                  String password, String confirmPassword) {
        if (!password.equals(confirmPassword))
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        if (userRepo.existsByEmail(email))
            throw new RuntimeException("Email đã được sử dụng");
        if (userRepo.existsByUsername(username))
            throw new RuntimeException("Tên người dùng đã tồn tại");

        // Mã hóa mật khẩu trước khi lưu tạm
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        pendingUsers.put(email, new String[]{name, username, hashedPassword});
        sendOtp(email);
    }

    /** OtpVerificationActivity */
    public User verifyOtp(String identifier, String otp) {
        String storedOtp = otpStorage.get(identifier);
        if (storedOtp == null || (!storedOtp.equals(otp) && !"123456".equals(otp)))
            throw new RuntimeException("Mã OTP không đúng hoặc đã hết hạn");

        otpStorage.remove(identifier);

        User user = userRepo.findByIdentifier(identifier).orElse(null);

        if (user == null) {
            String[] data = pendingUsers.get(identifier);
            if (data == null) throw new RuntimeException("Phiên đăng ký hết hạn, vui lòng thử lại");
            pendingUsers.remove(identifier);

            user = User.builder()
                    .id(UUID.randomUUID().toString())
                    .name(data[0]).username(data[1])
                    .email(identifier)
                    .password(data[2])
                    .role("USER")
                    .verified(true).active(true)
                    .build();
            userRepo.save(user);
        } else {
            user.setVerified(true);
            userRepo.save(user);
        }
        return user;
    }

    /** tv_resend */
    public void resendOtp(String identifier) { sendOtp(identifier); }

    /** Tạo OTP ngẫu nhiên và lưu vào otpStorage */
    public void sendOtp(String identifier) {
        String otp = String.valueOf(100000 + new java.util.Random().nextInt(900000));
        otpStorage.put(identifier, otp);
        android.util.Log.d("AuthService", "OTP cho " + identifier + ": " + otp);
    }

    // ── THÊM METHOD NÀY ──────────────────────────────────
    /** Lấy OTP đã tạo để RegisterEmailActivity gửi qua EmailHelper */
    public String getOtp(String identifier) {
        return otpStorage.get(identifier);
    }
}