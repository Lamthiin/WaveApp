package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.Genre;
import com.ptithcm.waveapp.repository.GenreRepository;
import java.util.*;
import java.util.stream.Collectors;

public class MockGenreRepository implements GenreRepository {
    private static final Map<String, Genre> genres = new HashMap<>();

    static {
        genres.put("genre-1", Genre.builder().id("genre-1").name("Pop").description("Pop music").imageUrl("https://example.com/pop.jpg").build());
        genres.put("genre-2", Genre.builder().id("genre-2").name("K-Pop").description("Korean Pop").imageUrl("https://example.com/kpop.jpg").build());
        genres.put("genre-3", Genre.builder().id("genre-3").name("R&B").description("Rhythm and Blues").imageUrl("https://example.com/rnb.jpg").build());
    }

    @Override
    public Optional<Genre> findByName(String name) {
        return genres.values().stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public List<Genre> findAll() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public Optional<Genre> findById(String id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public void save(Genre genre) {
        if (genre.getId() == null) genre.setId(UUID.randomUUID().toString());
        genres.put(genre.getId(), genre);
    }
}
