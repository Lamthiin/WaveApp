package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.Artist;
import java.util.List;
import java.util.Optional;

public interface ArtistRepository {
    List<Artist> findByActiveTrue();
    List<Artist> searchByName(String kw);
    List<Artist> findTopArtists();
    void incrementFollowers(String id);
    void decrementFollowers(String id);
    Optional<Artist> findById(String id);
    void save(Artist artist);
}
