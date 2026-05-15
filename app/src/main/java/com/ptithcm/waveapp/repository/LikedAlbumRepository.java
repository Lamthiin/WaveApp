package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.LikedAlbum;
import java.util.List;

public interface LikedAlbumRepository {
    boolean existsByUserIdAndAlbumId(String userId, String albumId);
    void deleteByUserIdAndAlbumId(String userId, String albumId);
    List<LikedAlbum> findByUserIdOrderByAddedAtDesc(String userId);
    void save(LikedAlbum likedAlbum);
}
