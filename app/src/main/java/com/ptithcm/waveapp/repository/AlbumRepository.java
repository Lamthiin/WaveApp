package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.Album;
import java.util.List;
import java.util.Optional;

public interface AlbumRepository {
    List<Album> findByActiveTrue();
    List<Album> findByArtistIdAndActiveTrue(String artistId);
    List<Album> searchByName(String kw);
    List<Album> findFeaturedAlbums();
    Optional<Album> findById(String id);
    void save(Album album);
}
