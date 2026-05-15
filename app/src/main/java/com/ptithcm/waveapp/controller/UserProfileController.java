package com.ptithcm.waveapp.controller;

import com.ptithcm.waveapp.dto.request.UpdateProfileRequest;
import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.service.UserProfileService;
import lombok.RequiredArgsConstructor;

/** UserProfileActivity.java */
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService profileService;

    public ApiResponse<UserProfileResponse> getProfile(String userId) {
        return ApiResponse.success(profileService.getProfile(userId));
    }

    public ApiResponse<UserProfileResponse> updateProfile(UpdateProfileRequest req, String userId) {
        return ApiResponse.success("Cập nhật thành công",
                profileService.updateProfile(userId, req));
    }
}
