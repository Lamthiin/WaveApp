package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.UserFollowArtist;
import java.util.List;

public interface UserFollowArtistRepository {
    boolean existsByUserIdAndArtistId(String userId, String artistId);
    void deleteByUserIdAndArtistId(String userId, String artistId);
    List<UserFollowArtist> findByUserIdOrderByFollowedAtDesc(String userId);
    void save(UserFollowArtist userFollowArtist);
}
