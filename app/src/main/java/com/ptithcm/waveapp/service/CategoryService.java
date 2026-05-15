package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.exception.ResourceNotFoundException;
import com.ptithcm.waveapp.model.*;
import com.ptithcm.waveapp.repository.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Xử lý:
 *   - AllCategoriesActivity.java    → getAllCategories()
 *   - SongsByCategoryActivity.java  → getSongsByCategory()
 */
@RequiredArgsConstructor
public class CategoryService {

    private final GenreRepository genreRepo;
    private final SongRepository  songRepo;

    /** AllCategoriesActivity – hiển thị lưới tất cả thể loại */
    public List<GenreResponse> getAllCategories() {
        return genreRepo.findAll().stream()
                .map(g -> GenreResponse.builder()
                        .id(g.getId()).name(g.getName())
                        .description(g.getDescription()).imageUrl(g.getImageUrl())
                        .songCount(0)
                        .build())
                .collect(Collectors.toList());
    }

    /** SongsByCategoryActivity – danh sách bài hát theo thể loại */
    public List<SongResponse> getSongsByCategory(String genreId) {
        Genre genre = genreRepo.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Thể loại không tồn tại"));

        return songRepo.findByActiveTrue().stream()
                .map(s -> SongResponse.builder()
                        .id(s.getId()).name(s.getName())
                        .artistId(s.getArtist().getId()).artistName(s.getArtist().getName())
                        .albumId(s.getAlbum() != null ? s.getAlbum().getId() : null)
                        .albumName(s.getAlbum() != null ? s.getAlbum().getName() : null)
                        .genreId(genre.getId()).genreName(genre.getName())
                        .duration(s.getDuration()).url(s.getUrl()).image(s.getImage())
                        .playCount(s.getPlayCount()).likeCount(s.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }
}
