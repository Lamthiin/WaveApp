package com.ptithcm.waveapp.dto.request;
import lombok.Data;
@Data
public class UpdateProfileRequest {
    private String name;
    private String avatar;
    private String phone;
}
