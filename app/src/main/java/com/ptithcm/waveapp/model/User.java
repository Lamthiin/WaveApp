package com.ptithcm.waveapp.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private String id;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String name;
    private String avatar;

    @Builder.Default
    private String role = "USER";

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean verified = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}