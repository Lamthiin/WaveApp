package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.model.*;
import com.ptithcm.waveapp.repository.*;
import java.util.List;

/**
 * HomeFragment: Album nổi tiếng, Bảng xếp hạng, Nghệ sĩ phổ biến, Thể loại
 * PlaylistDetailActivity: getAlbumById()
 */
public class HomeService {

    private final AlbumRepository  albumRepo;
    private final ArtistRepository artistRepo;
    private final GenreRepository  genreRepo;
    private final SongRepository   songRepo;

    public HomeService(AlbumRepository albumRepo, ArtistRepository artistRepo,
                       GenreRepository genreRepo, SongRepository songRepo) {
        this.albumRepo  = albumRepo;
        this.artistRepo = artistRepo;
        this.genreRepo  = genreRepo;
        this.songRepo   = songRepo;
    }

    /** "Album nổi tiếng" */
    public List<Album> getFeaturedAlbums() {
        return albumRepo.findFeaturedAlbums();
    }

    /** PlaylistDetailActivity: chi tiết 1 album */
    public Album getAlbumById(String albumId) {
        return albumRepo.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album không tồn tại"));
    }

    /** "Nghệ sĩ phổ biến" */
    public List<Artist> getFeaturedArtists() {
        return artistRepo.findTopArtists();
    }

    /** "Khám phá các thể loại" */
    public List<Genre> getCategories() {
        return genreRepo.findAll();
    }

    /** "Bảng xếp hạng" — Top 50 theo lượt nghe */
    public List<Song> getTop50() {
        return songRepo.findTopByPlayCount(50);
    }

    /** Hot theo lượt like */
    public List<Song> getHotHits() {
        return songRepo.findTopByLikeCount(20);
    }

    /** Lấy tất cả bài hát cho tab Nhạc */
    public List<Song> getAllSongs() {
        return songRepo.findAll();
    }
}