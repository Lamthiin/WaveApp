package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.dto.request.UpdateProfileRequest;
import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.exception.ResourceNotFoundException;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.*;
import lombok.RequiredArgsConstructor;

/**
 * Xử lý: UserProfileActivity.java
 *   - Xem thông tin cá nhân
 *   - Cập nhật name, avatar, phone
 *   - Thống kê: số bài đã like, số album đã lưu, số nghệ sĩ đang follow, số playlist
 */
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository             userRepo;
    private final LikedSongRepository        likedSongRepo;
    private final LikedAlbumRepository       likedAlbumRepo;
    private final UserFollowArtistRepository  followRepo;
    private final PlaylistRepository          playlistRepo;

    /** UserProfileActivity: load thông tin user */
    public UserProfileResponse getProfile(String userId) {
        User user = findUser(userId);
        return buildProfile(user);
    }

    /** UserProfileActivity: cập nhật thông tin */
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest req) {
        User user = findUser(userId);
        if (req.getName()   != null) user.setName(req.getName());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());
        if (req.getPhone()  != null) user.setPhone(req.getPhone());
        userRepo.save(user);
        return buildProfile(user);
    }

    private User findUser(String userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
    }

    private UserProfileResponse buildProfile(User user) {
        long likedSongs   = likedSongRepo.findByUserIdOrderByLikedAtDesc(user.getId()).size();
        long likedAlbums  = likedAlbumRepo.findByUserIdOrderByAddedAtDesc(user.getId()).size();
        long followArtists = followRepo.findByUserIdOrderByFollowedAtDesc(user.getId()).size();
        long playlists    = playlistRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).size();

        return UserProfileResponse.builder()
                .id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).phone(user.getPhone())
                .name(user.getName()).avatar(user.getAvatar())
                .role(user.getRole().name())
                .likedSongsCount(likedSongs)
                .likedAlbumsCount(likedAlbums)
                .followingArtistsCount(followArtists)
                .playlistsCount(playlists)
                .build();
    }
}
