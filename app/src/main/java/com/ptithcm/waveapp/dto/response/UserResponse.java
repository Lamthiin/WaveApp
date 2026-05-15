package com.ptithcm.waveapp.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder
public class UserResponse {
    private String id, username, email, phone, name, avatar, role;
    private LocalDateTime createdAt;
}
