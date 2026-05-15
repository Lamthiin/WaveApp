package com.ptithcm.waveapp.dto.request;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String identifier;
    private String otp;
}
