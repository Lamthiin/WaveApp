package com.ptithcm.waveapp.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/** Dùng cho UserProfileActivity.java */
@Data @Builder
public class UserProfileResponse {
    private String id, username, email, phone, name, avatar, role;
    private LocalDateTime createdAt;
    // Thống kê hiển thị trên profile
    private long likedSongsCount;
    private long likedAlbumsCount;
    private long followingArtistsCount;
    private long playlistsCount;
}
