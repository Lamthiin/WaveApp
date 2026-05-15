package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.LikedSong;
import java.util.List;

public interface LikedSongRepository {
    boolean existsByUserIdAndSongId(String userId, String songId);
    void deleteByUserIdAndSongId(String userId, String songId);
    List<LikedSong> findByUserIdOrderByLikedAtDesc(String userId);
    void save(LikedSong likedSong);
}
