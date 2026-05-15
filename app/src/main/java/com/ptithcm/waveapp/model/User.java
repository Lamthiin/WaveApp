package com.ptithcm.waveapp.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String name;
    private String avatar;          // path file anh: "images/avatars/u001.jpg"
    @Builder.Default private String role = "USER"; // "USER" hoac "ADMIN"
    @Builder.Default private boolean active = true;
    @Builder.Default private boolean verified = false;
    private LocalDateTime createdAt;
}
