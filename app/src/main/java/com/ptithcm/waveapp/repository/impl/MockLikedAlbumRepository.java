package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.LikedAlbum;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;
import java.util.*;
import java.util.stream.Collectors;

public class MockLikedAlbumRepository implements LikedAlbumRepository {
    private static final List<LikedAlbum> likedAlbums = new ArrayList<>();

    @Override
    public boolean existsByUserIdAndAlbumId(String userId, String albumId) {
        return likedAlbums.stream().anyMatch(la -> la.getUser().getId().equals(userId) && la.getAlbum().getId().equals(albumId));
    }

    @Override
    public void deleteByUserIdAndAlbumId(String userId, String albumId) {
        likedAlbums.removeIf(la -> la.getUser().getId().equals(userId) && la.getAlbum().getId().equals(albumId));
    }

    @Override
    public List<LikedAlbum> findByUserIdOrderByAddedAtDesc(String userId) {
        return likedAlbums.stream()
                .filter(la -> la.getUser().getId().equals(userId))
                .sorted((la1, la2) -> la2.getAddedAt().compareTo(la1.getAddedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void save(LikedAlbum likedAlbum) {
        likedAlbums.add(likedAlbum);
    }
}
