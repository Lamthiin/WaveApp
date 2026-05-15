package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.LikedSong;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import java.util.*;
import java.util.stream.Collectors;

public class MockLikedSongRepository implements LikedSongRepository {
    private static final List<LikedSong> likedSongs = new ArrayList<>();

    @Override
    public boolean existsByUserIdAndSongId(String userId, String songId) {
        return likedSongs.stream().anyMatch(ls -> ls.getUser().getId().equals(userId) && ls.getSong().getId().equals(songId));
    }

    @Override
    public void deleteByUserIdAndSongId(String userId, String songId) {
        likedSongs.removeIf(ls -> ls.getUser().getId().equals(userId) && ls.getSong().getId().equals(songId));
    }

    @Override
    public List<LikedSong> findByUserIdOrderByLikedAtDesc(String userId) {
        return likedSongs.stream()
                .filter(ls -> ls.getUser().getId().equals(userId))
                .sorted((ls1, ls2) -> ls2.getLikedAt().compareTo(ls1.getLikedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void save(LikedSong likedSong) {
        likedSongs.add(likedSong);
    }
}
