package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.model.*;
import com.ptithcm.waveapp.repository.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Xử lý: HomeFragment.java
 *   - "Album nổi tiếng"     → getFeaturedAlbums()
 *   - "Bảng xếp hạng"       → getCharts()
 *   - "Nghệ sĩ phổ biến"    → getFeaturedArtists()
 *   - "Khám phá các thể loại" → getCategories()
 */
@RequiredArgsConstructor
public class HomeService {

    private final AlbumRepository  albumRepo;
    private final ArtistRepository artistRepo;
    private final GenreRepository  genreRepo;
    private final SongRepository   songRepo;

    public HomeResponse getHomeData() {
        return HomeResponse.builder()
                .featuredAlbums(getFeaturedAlbums(8))
                .featuredArtists(getFeaturedArtists(8))
                .categories(getCategories())
                .charts(getCharts())
                .build();
    }

    /** "Album nổi tiếng" – section trong fragment_home.xml */
    public List<AlbumResponse> getFeaturedAlbums(int limit) {
        return albumRepo.findFeaturedAlbums()
                .stream().map(this::toAlbumResponse).collect(Collectors.toList());
    }

    /** "Nghệ sĩ phổ biến" – section trong fragment_home.xml */
    public List<ArtistResponse> getFeaturedArtists(int limit) {
        return artistRepo.findTopArtists()
                .stream().map(this::toArtistResponse).collect(Collectors.toList());
    }

    /** "Khám phá các thể loại" – section trong fragment_home.xml */
    public List<GenreResponse> getCategories() {
        return genreRepo.findAll().stream()
                .map(g -> GenreResponse.builder()
                        .id(g.getId()).name(g.getName())
                        .description(g.getDescription()).imageUrl(g.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    /** "Bảng xếp hạng" – section trong fragment_home.xml */
    public List<ChartResponse> getCharts() {
        List<Song> top50   = songRepo.findByActiveTrue(); // Giả lập top
        List<Song> hotHits = songRepo.findByActiveTrue(); // Giả lập hot
        
        List<ChartResponse> charts = new ArrayList<>();
        charts.add(ChartResponse.builder().id("top50-vn").name("Top 50\n\nVIETNAM")
                .description("Cập nhật hằng ngày những bản nhạc được nghe nhiều nhất")
                .image("/images/charts/top50.jpg")
                .songs(top50.stream().map(this::toSongResponse).collect(Collectors.toList()))
                .build());
        charts.add(ChartResponse.builder().id("hot-hits-vn").name("HOT HITS\n\nVIETNAM")
                .description("BTS, HIEUTHUHAI, Low G, Bray, Phùng Khánh Linh...")
                .image("/images/charts/hothits.jpg")
                .songs(hotHits.stream().map(this::toSongResponse).collect(Collectors.toList()))
                .build());
        return charts;
    }

    // ── Mappers ──
    AlbumResponse toAlbumResponse(Album a) {
        return AlbumResponse.builder()
                .id(a.getId()).name(a.getName())
                .artistId(a.getArtist().getId()).artistName(a.getArtist().getName())
                .image(a.getImage()).releaseDate(a.getReleaseDate())
                .songCount(0) // Cần logic lấy count nếu cần
                .build();
    }

    ArtistResponse toArtistResponse(Artist a) {
        return ArtistResponse.builder()
                .id(a.getId()).name(a.getName())
                .image(a.getImage()).bio(a.getBio())
                .followersCount(a.getFollowersCount())
                .build();
    }

    SongResponse toSongResponse(Song s) {
        return SongResponse.builder()
                .id(s.getId()).name(s.getName())
                .artistId(s.getArtist().getId()).artistName(s.getArtist().getName())
                .albumId(s.getAlbum() != null ? s.getAlbum().getId() : null)
                .albumName(s.getAlbum() != null ? s.getAlbum().getName() : null)
                .duration(s.getDuration()).url(s.getUrl()).image(s.getImage())
                .playCount(s.getPlayCount()).likeCount(s.getLikeCount())
                .build();
    }
}
