package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.repository.ArtistRepository;
import java.util.*;
import java.util.stream.Collectors;

public class MockArtistRepository implements ArtistRepository {
    private static final Map<String, Artist> artists = new HashMap<>();

    static {
        artists.put("artist-1", Artist.builder()
                .id("artist-1").name("Son Tung M-TP")
                .image("https://example.com/sontung.jpg")
                .bio("Vietnamese singer-songwriter and actor.")
                .followersCount(1000000).active(true).build());
        artists.put("artist-2", Artist.builder()
                .id("artist-2").name("Den Vau")
                .image("https://example.com/denvau.jpg")
                .bio("Vietnamese rapper and songwriter.")
                .followersCount(800000).active(true).build());
    }

    @Override
    public List<Artist> findByActiveTrue() {
        return artists.values().stream().filter(Artist::isActive).collect(Collectors.toList());
    }

    @Override
    public List<Artist> searchByName(String kw) {
        return artists.values().stream()
                .filter(a -> a.getName().toLowerCase().contains(kw.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Artist> findTopArtists() {
        return artists.values().stream()
                .sorted((a1, a2) -> Integer.compare(a2.getFollowersCount(), a1.getFollowersCount()))
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public void incrementFollowers(String id) {
        Artist a = artists.get(id);
        if (a != null) a.setFollowersCount(a.getFollowersCount() + 1);
    }

    @Override
    public void decrementFollowers(String id) {
        Artist a = artists.get(id);
        if (a != null && a.getFollowersCount() > 0) a.setFollowersCount(a.getFollowersCount() - 1);
    }

    @Override
    public Optional<Artist> findById(String id) {
        return Optional.ofNullable(artists.get(id));
    }

    @Override
    public void save(Artist artist) {
        if (artist.getId() == null) artist.setId(UUID.randomUUID().toString());
        artists.put(artist.getId(), artist);
    }
}
