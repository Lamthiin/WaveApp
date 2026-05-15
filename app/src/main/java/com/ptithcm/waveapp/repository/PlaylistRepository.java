package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.Playlist;
import java.util.List;
import java.util.Optional;

public interface PlaylistRepository {
    List<Playlist> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Playlist> findById(String id);
    void save(Playlist playlist);
    void deleteById(String id);
}
