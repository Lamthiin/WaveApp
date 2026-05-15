package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.Genre;
import java.util.List;
import java.util.Optional;

public interface GenreRepository {
    Optional<Genre> findByName(String name);
    List<Genre> findAll();
    Optional<Genre> findById(String id);
    void save(Genre genre);
}
