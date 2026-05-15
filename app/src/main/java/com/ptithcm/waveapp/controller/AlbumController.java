package com.ptithcm.waveapp.controller;

import com.ptithcm.waveapp.dto.response.AlbumResponse;
import com.ptithcm.waveapp.dto.response.ApiResponse;
import com.ptithcm.waveapp.dto.response.SongResponse;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AlbumController {
    private final AlbumRepository albumRepo;
    private final SongRepository songRepo;
    private final LikedAlbumRepository likedAlbumRepo;
    private final LikedSongRepository likedSongRepo;

    public ApiResponse<AlbumResponse> getAlbumDetail(String albumId, String userId) {
        Album album = albumRepo.findById(albumId).orElse(null);
        if (album == null) return ApiResponse.error("Album không tồn tại");

        List<Song> songs = songRepo.findByAlbumIdAndActiveTrue(albumId);
        boolean isLiked = userId != null && likedAlbumRepo.existsByUserIdAndAlbumId(userId, albumId);

        AlbumResponse res = AlbumResponse.builder()
                .id(album.getId())
                .name(album.getName())
                .artistId(album.getArtist().getId())
                .artistName(album.getArtist().getName())
                .image(album.getImage())
                .releaseDate(album.getReleaseDate())
                .liked(isLiked)
                .songCount(songs.size())
                .songs(songs.stream().map(s -> toSongResponse(s, userId)).collect(Collectors.toList()))
                .build();

        return ApiResponse.success(res);
    }

    private SongResponse toSongResponse(Song s, String userId) {
        return SongResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .artistId(s.getArtist().getId())
                .artistName(s.getArtist().getName())
                .albumId(s.getAlbum() != null ? s.getAlbum().getId() : null)
                .albumName(s.getAlbum() != null ? s.getAlbum().getName() : null)
                .duration(s.getDuration())
                .url(s.getUrl())
                .image(s.getImage())
                .liked(userId != null && likedSongRepo.existsByUserIdAndSongId(userId, s.getId()))
                .build();
    }
}
