package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.dto.request.*;
import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.exception.BadRequestException;
import com.ptithcm.waveapp.exception.ResourceNotFoundException;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Service xử lý logic xác thực cho Android.
 * Lưu ý: Logic Redis và Mail đã được vô hiệu hóa để có thể chạy trên Android.
 */
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository      userRepository;
    private final JwtTokenProvider    jwt;

    // Giả lập lưu trữ User đang chờ xác thực (Thay thế Redis)
    private static final java.util.Map<String, RegisterRequest> pendingUsers = new java.util.HashMap<>();
    private static final java.util.Map<String, String> otpStorage = new java.util.HashMap<>();

    // Giả lập config
    private String googleClientId = "your-google-client-id";
    private long otpTtl = 300;

    public AuthResponse loginWithEmail(String identifier, String rawPassword) {
        // Kiểm tra xem user có trong pending không (Dành cho Firebase sync)
        if (!userRepository.existsByEmail(identifier) && pendingUsers.containsKey(identifier)) {
            RegisterRequest reg = pendingUsers.get(identifier);
            if (reg.getPassword().equals(rawPassword)) {
                // Tự động xác thực và lưu vào repo chính
                User newUser = User.builder()
                        .id(UUID.randomUUID().toString())
                        .email(reg.getEmail())
                        .username(reg.getUsername())
                        .name(reg.getName())
                        .password(reg.getPassword())
                        .authProvider(User.AuthProvider.LOCAL)
                        .role(User.Role.USER)
                        .verified(true)
                        .active(true)
                        .build();
                userRepository.save(newUser);
                pendingUsers.remove(identifier);
            }
        }

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại trong hệ thống Mock"));
        
        if (!user.isActive())    throw new BadRequestException("Tài khoản đã bị khóa");
        if (!user.isVerified())  throw new BadRequestException("Tài khoản chưa xác thực email");
        
        // Android-side password check (Cần JBCrypt nếu muốn check mật khẩu hash)
        if (!rawPassword.equals(user.getPassword()))
            throw new BadRequestException("Mật khẩu không đúng");
            
        return buildAuth(user);
    }

    public AuthResponse loginWithGoogle(String idToken) {
        // Logic verify Google Token trên Android thường dùng Firebase hoặc GoogleSignIn SDK
        // Ở đây giữ lại khung logic cũ
        throw new UnsupportedOperationException("Vui lòng sử dụng Firebase Auth cho Google Login trên Android");
    }

    public void registerWithEmail(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword()))
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new BadRequestException("Email đã được sử dụng");

        // Lưu thông tin đăng ký vào bộ nhớ tạm
        pendingUsers.put(req.getEmail(), req);
        sendOtp(req.getEmail());
    }

    public void resendOtp(String identifier) { 
        sendOtp(identifier); 
    }

    public AuthResponse verifyOtp(OtpVerifyRequest req) {
        // Kiểm tra OTP giả lập (Ở đây chấp nhận bất kỳ mã nào hoặc mã 123456)
        String storedOtp = otpStorage.get(req.getIdentifier());
        if (storedOtp == null || !storedOtp.equals(req.getOtp())) {
            // Cho phép mã 123456 để test dễ hơn
            if (!"123456".equals(req.getOtp())) {
                throw new BadRequestException("Mã OTP không đúng hoặc đã hết hạn");
            }
        }

        // Lấy thông tin đăng ký từ bộ nhớ tạm
        RegisterRequest registerData = pendingUsers.get(req.getIdentifier());
        
        User user = userRepository.findByIdentifier(req.getIdentifier()).orElse(null);
        
        if (user == null && registerData != null) {
            // Tạo user mới từ dữ liệu đăng ký
            user = User.builder()
                    .id(UUID.randomUUID().toString())
                    .email(registerData.getEmail())
                    .username(registerData.getUsername())
                    .name(registerData.getName())
                    .password(registerData.getPassword()) // Trong thực tế nên mã hóa
                    .authProvider(User.AuthProvider.LOCAL)
                    .role(User.Role.USER)
                    .verified(true)
                    .active(true)
                    .build();
            userRepository.save(user);
            pendingUsers.remove(req.getIdentifier());
        } else if (user != null) {
            user.setVerified(true);
            userRepository.save(user);
        } else {
            throw new BadRequestException("Không tìm thấy thông tin đăng ký");
        }
        
        otpStorage.remove(req.getIdentifier());
        return buildAuth(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwt.validate(refreshToken)) throw new BadRequestException("Refresh token không hợp lệ");
        String userId = jwt.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        return buildAuth(user);
    }

    public void sendOtp(String identifier) {
        String otp = "123456"; // Cố định mã 123456 cho môi trường mock để dễ test
        otpStorage.put(identifier, otp);
        System.out.println("OTP for " + identifier + " is: " + otp);
    }

    private AuthResponse buildAuth(User user) {
        String at = jwt.generateAccessToken(user.getId(), user.getRole().name());
        String rt = jwt.generateRefreshToken(user.getId());
        return AuthResponse.builder()
                .accessToken(at).refreshToken(rt).tokenType("Bearer")
                .user(UserResponse.builder()
                        .id(user.getId()).username(user.getUsername())
                        .email(user.getEmail()).phone(user.getPhone())
                        .name(user.getName()).avatar(user.getAvatar())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
