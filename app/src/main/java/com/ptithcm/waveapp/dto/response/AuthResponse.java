package com.ptithcm.waveapp.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String accessToken, refreshToken;
    private String tokenType;
    private UserResponse user;
}
