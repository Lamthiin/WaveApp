package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.repository.PlaylistRepository;
import java.util.*;
import java.util.stream.Collectors;

public class MockPlaylistRepository implements PlaylistRepository {
    private static final Map<String, Playlist> playlists = new HashMap<>();

    @Override
    public List<Playlist> findByUserIdOrderByCreatedAtDesc(String userId) {
        return playlists.values().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Playlist> findById(String id) {
        return Optional.ofNullable(playlists.get(id));
    }

    @Override
    public void save(Playlist playlist) {
        if (playlist.getId() == null) playlist.setId(UUID.randomUUID().toString());
        playlists.put(playlist.getId(), playlist);
    }

    @Override
    public void deleteById(String id) {
        playlists.remove(id);
    }
}
