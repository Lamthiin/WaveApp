package com.ptithcm.waveapp.controller;

import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.service.HomeService;
import lombok.RequiredArgsConstructor;
import java.util.List;

/** 
 * Controller này đã được chuyển đổi sang logic Android thuần.
 * Dùng cho HomeFragment: Album nổi tiếng, Bảng xếp hạng, Nghệ sĩ phổ biến...
 */
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    public ApiResponse<HomeResponse> getHome() {
        try {
            return ApiResponse.success(homeService.getHomeData());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<AlbumResponse>> albums(int limit) {
        try {
            return ApiResponse.success(homeService.getFeaturedAlbums(limit));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<ArtistResponse>> artists(int limit) {
        try {
            return ApiResponse.success(homeService.getFeaturedArtists(limit));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<GenreResponse>> categories() {
        try {
            return ApiResponse.success(homeService.getCategories());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<ChartResponse>> charts() {
        try {
            return ApiResponse.success(homeService.getCharts());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
