package com.ptithcm.waveapp.dto.request;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String identifier; // email / phone / username
    private String password;
}
