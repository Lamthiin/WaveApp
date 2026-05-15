package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.UserFollowArtist;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;
import java.util.*;
import java.util.stream.Collectors;

public class MockUserFollowArtistRepository implements UserFollowArtistRepository {
    private static final List<UserFollowArtist> follows = new ArrayList<>();

    @Override
    public boolean existsByUserIdAndArtistId(String userId, String artistId) {
        return follows.stream().anyMatch(f -> f.getUser().getId().equals(userId) && f.getArtist().getId().equals(artistId));
    }

    @Override
    public void deleteByUserIdAndArtistId(String userId, String artistId) {
        follows.removeIf(f -> f.getUser().getId().equals(userId) && f.getArtist().getId().equals(artistId));
    }

    @Override
    public List<UserFollowArtist> findByUserIdOrderByFollowedAtDesc(String userId) {
        return follows.stream()
                .filter(f -> f.getUser().getId().equals(userId))
                .sorted((f1, f2) -> f2.getFollowedAt().compareTo(f1.getFollowedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void save(UserFollowArtist userFollowArtist) {
        follows.add(userFollowArtist);
    }
}
