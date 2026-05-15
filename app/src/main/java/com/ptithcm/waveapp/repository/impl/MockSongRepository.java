package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.SongRepository;
import java.util.*;
import java.util.stream.Collectors;

public class MockSongRepository implements SongRepository {
    private static final Map<String, Song> songs = new HashMap<>();

    static {
        Artist sontung = Artist.builder().id("artist-1").name("Son Tung M-TP").build();
        Artist denvau = Artist.builder().id("artist-2").name("Den Vau").build();

        Album album1 = Album.builder().id("album-1").name("Chung Ta Cua Hien Tai").artist(sontung).build();

        songs.put("song-1", Song.builder()
                .id("song-1").name("Chung Ta Cua Tuong Lai")
                .artist(sontung).album(album1).duration(240).url("https://example.com/song1.mp3")
                .image("https://example.com/song1.jpg").playCount(1000000).likeCount(50000)
                .active(true).build());
        
        songs.put("song-2", Song.builder()
                .id("song-2").name("Nau An Cho Em")
                .artist(denvau).duration(300).url("https://example.com/song2.mp3")
                .image("https://example.com/song2.jpg").playCount(2000000).likeCount(150000)
                .active(true).build());
    }

    @Override
    public List<Song> findAll() {
        return new ArrayList<>(songs.values());
    }

    @Override
    public Optional<Song> findById(String id) {
        return Optional.ofNullable(songs.get(id));
    }

    @Override
    public void save(Song song) {
        if (song.getId() == null) song.setId(UUID.randomUUID().toString());
        songs.put(song.getId(), song);
    }

    @Override
    public void deleteById(String id) {
        songs.remove(id);
    }

    @Override
    public List<Song> findByActiveTrue() {
        return songs.values().stream().filter(Song::isActive).collect(Collectors.toList());
    }

    @Override
    public List<Song> findByAlbumIdAndActiveTrue(String albumId) {
        return songs.values().stream()
                .filter(s -> s.getAlbum() != null && s.getAlbum().getId().equals(albumId) && s.isActive())
                .collect(Collectors.toList());
    }

    @Override
    public List<Song> searchByName(String kw) {
        return songs.values().stream()
                .filter(s -> s.getName().toLowerCase().contains(kw.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public void incrementPlayCount(String id) {
        Song s = songs.get(id);
        if (s != null) s.setPlayCount(s.getPlayCount() + 1);
    }
}
