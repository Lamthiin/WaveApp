package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.repository.AlbumRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MockAlbumRepository implements AlbumRepository {
    private static final Map<String, Album> albums = new HashMap<>();

    static {
        Artist sontung = Artist.builder().id("artist-1").name("Son Tung M-TP").build();
        
        albums.put("album-1", Album.builder()
                .id("album-1").name("Chung Ta Cua Hien Tai")
                .artist(sontung).image("https://example.com/album1.jpg")
                .releaseDate(LocalDate.now()).active(true).build());
    }

    @Override
    public List<Album> findByActiveTrue() {
        return albums.values().stream().filter(Album::isActive).collect(Collectors.toList());
    }

    @Override
    public List<Album> findByArtistIdAndActiveTrue(String artistId) {
        return albums.values().stream()
                .filter(a -> a.getArtist().getId().equals(artistId) && a.isActive())
                .collect(Collectors.toList());
    }

    @Override
    public List<Album> searchByName(String kw) {
        return albums.values().stream()
                .filter(a -> a.getName().toLowerCase().contains(kw.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Album> findFeaturedAlbums() {
        return new ArrayList<>(albums.values());
    }

    @Override
    public Optional<Album> findById(String id) {
        return Optional.ofNullable(albums.get(id));
    }

    @Override
    public void save(Album album) {
        if (album.getId() == null) album.setId(UUID.randomUUID().toString());
        albums.put(album.getId(), album);
    }
}
