package com.ptithcm.waveapp.controller;

import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.model.*;
import com.ptithcm.waveapp.repository.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

/** fragment_search: tabSongs / tabArtists / tabAlbums / tabGenres */
@RequiredArgsConstructor
public class SearchController {
    private final SongRepository   songRepo;
    private final ArtistRepository artistRepo;
    private final AlbumRepository  albumRepo;
    private final GenreRepository  genreRepo;

    public ApiResponse<SearchResponse> searchAll(String q) {
        SearchResponse result = SearchResponse.builder()
                .keyword(q)
                .songs(songRepo.searchByName(q).stream().map(this::toSong).collect(Collectors.toList()))
                .artists(artistRepo.searchByName(q).stream().map(this::toArtist).collect(Collectors.toList()))
                .albums(albumRepo.searchByName(q).stream().map(this::toAlbum).collect(Collectors.toList()))
                .build();
        return ApiResponse.success(result);
    }

    public ApiResponse<List<SongResponse>> searchSongs(String q) {
        List<SongResponse> content = songRepo.searchByName(q).stream().map(this::toSong).collect(Collectors.toList());
        return ApiResponse.success(content);
    }

    public ApiResponse<List<ArtistResponse>> searchArtists(String q) {
        List<ArtistResponse> content = artistRepo.searchByName(q).stream().map(this::toArtist).collect(Collectors.toList());
        return ApiResponse.success(content);
    }

    public ApiResponse<List<AlbumResponse>> searchAlbums(String q) {
        List<AlbumResponse> content = albumRepo.searchByName(q).stream().map(this::toAlbum).collect(Collectors.toList());
        return ApiResponse.success(content);
    }

    public ApiResponse<List<GenreResponse>> genres() {
        List<GenreResponse> list = genreRepo.findAll().stream()
                .map(g -> GenreResponse.builder().id(g.getId()).name(g.getName()).description(g.getDescription()).imageUrl(g.getImageUrl()).build())
                .collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    // mappers
    private SongResponse toSong(Song s) {
        return SongResponse.builder().id(s.getId()).name(s.getName())
                .artistId(s.getArtist().getId()).artistName(s.getArtist().getName())
                .albumId(s.getAlbum()!=null?s.getAlbum().getId():null)
                .albumName(s.getAlbum()!=null?s.getAlbum().getName():null)
                .duration(s.getDuration()).url(s.getUrl()).image(s.getImage())
                .playCount(s.getPlayCount()).likeCount(s.getLikeCount()).build();
    }
    private ArtistResponse toArtist(Artist a) {
        return ArtistResponse.builder().id(a.getId()).name(a.getName())
                .image(a.getImage()).bio(a.getBio()).followersCount(a.getFollowersCount()).build();
    }
    private AlbumResponse toAlbum(Album a) {
        return AlbumResponse.builder().id(a.getId()).name(a.getName())
                .artistId(a.getArtist().getId()).artistName(a.getArtist().getName())
                .image(a.getImage()).releaseDate(a.getReleaseDate())
                .songCount(0).build();
    }
}
