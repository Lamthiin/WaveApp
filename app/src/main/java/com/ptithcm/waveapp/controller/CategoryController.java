package com.ptithcm.waveapp.controller;

import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * AllCategoriesActivity.java  → GET /api/categories
 * SongsByCategoryActivity.java → GET /api/categories/{id}/songs
 */
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /** AllCategoriesActivity: lưới tất cả thể loại */
    public ApiResponse<List<GenreResponse>> allCategories() {
        return ApiResponse.success(categoryService.getAllCategories());
    }

    /** SongsByCategoryActivity: bài hát theo thể loại */
    public ApiResponse<List<SongResponse>> songsByCategory(String genreId) {
        return ApiResponse.success(categoryService.getSongsByCategory(genreId));
    }
}
