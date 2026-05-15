package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.PlaylistSong;
import com.ptithcm.waveapp.repository.PlaylistSongRepository;
import java.util.*;
import java.util.stream.Collectors;

public class MockPlaylistSongRepository implements PlaylistSongRepository {
    private static final List<PlaylistSong> playlistSongs = new ArrayList<>();

    @Override
    public List<PlaylistSong> findByPlaylistIdOrderByPosition(String playlistId) {
        return playlistSongs.stream()
                .filter(ps -> ps.getPlaylist().getId().equals(playlistId))
                .sorted(Comparator.comparingInt(PlaylistSong::getPosition))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PlaylistSong> findByPlaylistIdAndSongId(String playlistId, String songId) {
        return playlistSongs.stream()
                .filter(ps -> ps.getPlaylist().getId().equals(playlistId) && ps.getSong().getId().equals(songId))
                .findFirst();
    }

    @Override
    public boolean existsByPlaylistIdAndSongId(String playlistId, String songId) {
        return playlistSongs.stream()
                .anyMatch(ps -> ps.getPlaylist().getId().equals(playlistId) && ps.getSong().getId().equals(songId));
    }

    @Override
    public void deleteByPlaylistIdAndSongId(String playlistId, String songId) {
        playlistSongs.removeIf(ps -> ps.getPlaylist().getId().equals(playlistId) && ps.getSong().getId().equals(songId));
    }

    @Override
    public int countByPlaylistId(String playlistId) {
        return (int) playlistSongs.stream().filter(ps -> ps.getPlaylist().getId().equals(playlistId)).count();
    }

    @Override
    public void save(PlaylistSong playlistSong) {
        playlistSongs.add(playlistSong);
    }
}
