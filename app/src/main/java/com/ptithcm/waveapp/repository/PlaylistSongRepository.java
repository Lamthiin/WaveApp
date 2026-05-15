package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.PlaylistSong;
import java.util.List;
import java.util.Optional;

public interface PlaylistSongRepository {
    List<PlaylistSong> findByPlaylistIdOrderByPosition(String playlistId);
    Optional<PlaylistSong> findByPlaylistIdAndSongId(String playlistId, String songId);
    boolean existsByPlaylistIdAndSongId(String playlistId, String songId);
    void deleteByPlaylistIdAndSongId(String playlistId, String songId);
    int countByPlaylistId(String playlistId);
    void save(PlaylistSong playlistSong);
}
