package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.model.Genre;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.GenreRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import java.util.List;
import java.util.Optional;

/**
 * AllCategoriesActivity → getAllCategories()
 * SongsByCategoryActivity → getSongsByCategory()
 */
public class CategoryService {

    private final GenreRepository genreRepo;
    private final SongRepository  songRepo;

    public CategoryService(GenreRepository genreRepo, SongRepository songRepo) {
        this.genreRepo = genreRepo;
        this.songRepo  = songRepo;
    }

    /** AllCategoriesActivity: lưới tất cả thể loại */
    public List<Genre> getAllCategories() {
        return genreRepo.findAll();
    }

    /** SongsByCategoryActivity: bài hát theo thể loại */
    public List<Song> getSongsByCategory(String genreId) {
        // Kiểm tra thể loại có tồn tại không
        genreRepo.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Thể loại không tồn tại"));

        // FIX 3: findByActiveTrue() lấy TẤT CẢ bài → phải dùng findByGenreIdAndActiveTrue()
        return songRepo.findByGenreIdAndActiveTrue(genreId);
    }
}
