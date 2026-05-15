package com.ptithcm.waveapp.controller;

import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.model.*;
import com.ptithcm.waveapp.repository.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * fragment_library: tabSongs / tabArtists / tabAlbums / tabCustomPlaylists
 * like/unlike song, follow/unfollow artist, add/remove library album
 */
@RequiredArgsConstructor
public class LibraryController {
    private final LikedSongRepository       likedSongRepo;
    private final LikedAlbumRepository      likedAlbumRepo;
    private final UserFollowArtistRepository followRepo;
    private final PlaylistRepository        playlistRepo;
    private final SongRepository            songRepo;
    private final AlbumRepository           albumRepo;
    private final ArtistRepository          artistRepo;
    private final UserRepository            userRepo;

    /** tabSongs – bài hát yêu thích (isFavorite=true trong Song.java Android) */
    public ApiResponse<List<SongResponse>> likedSongs(String userId) {
        List<LikedSong> pg = likedSongRepo.findByUserIdOrderByLikedAtDesc(userId);
        List<SongResponse> list = pg.stream().map(ls -> toSong(ls.getSong(), true)).collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    /** tabArtists – nghệ sĩ yêu thích (isFavorite=true trong Artist.java Android) */
    public ApiResponse<List<ArtistResponse>> followingArtists(String userId) {
        List<ArtistResponse> list = followRepo.findByUserIdOrderByFollowedAtDesc(userId).stream()
                .map(f -> ArtistResponse.builder().id(f.getArtist().getId()).name(f.getArtist().getName())
                        .image(f.getArtist().getImage()).bio(f.getArtist().getBio())
                        .followersCount(f.getArtist().getFollowersCount()).following(true).build())
                .collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    /** tabAlbums – album yêu thích */
    public ApiResponse<List<AlbumResponse>> likedAlbums(String userId) {
        List<AlbumResponse> list = likedAlbumRepo.findByUserIdOrderByAddedAtDesc(userId).stream()
                .map(la -> AlbumResponse.builder().id(la.getAlbum().getId()).name(la.getAlbum().getName())
                        .artistId(la.getAlbum().getArtist().getId()).artistName(la.getAlbum().getArtist().getName())
                        .image(la.getAlbum().getImage()).releaseDate(la.getAlbum().getReleaseDate()).liked(true).build())
                .collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    /** tabCustomPlaylists – list tự tạo */
    public ApiResponse<List<PlaylistResponse>> myPlaylists(String userId) {
        List<PlaylistResponse> list = playlistRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(p -> PlaylistResponse.builder().id(p.getId()).name(p.getName()).image(p.getImage())
                        .userId(p.getUser().getId()).userName(p.getUser().getName())
                        .isPublic(p.isPublic()).createdAt(p.getCreatedAt()).build())
                .collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    /** Like bài hát (isFavorite toggle trong Song.java Android) */
    public ApiResponse<Void> likeSong(String songId, String userId) {
        if (likedSongRepo.existsByUserIdAndSongId(userId, songId))
            return ApiResponse.success("Đã yêu thích rồi", null);
        User u = userRepo.findById(userId).orElseThrow();
        Song s = songRepo.findById(songId).orElseThrow();
        likedSongRepo.save(LikedSong.builder().user(u).song(s).build());
        return ApiResponse.success("Đã thêm vào yêu thích", null);
    }

    public ApiResponse<Void> unlikeSong(String songId, String userId) {
        likedSongRepo.deleteByUserIdAndSongId(userId, songId);
        return ApiResponse.success("Đã bỏ yêu thích", null);
    }

    /** Follow/Unfollow nghệ sĩ (isFavorite toggle trong Artist.java Android) */
    public ApiResponse<Void> follow(String artistId, String userId) {
        if (followRepo.existsByUserIdAndArtistId(userId, artistId))
            return ApiResponse.success("Đang theo dõi rồi", null);
        User u = userRepo.findById(userId).orElseThrow();
        Artist a = artistRepo.findById(artistId).orElseThrow();
        followRepo.save(UserFollowArtist.builder().user(u).artist(a).build());
        artistRepo.incrementFollowers(artistId);
        return ApiResponse.success("Đã theo dõi nghệ sĩ", null);
    }

    public ApiResponse<Void> unfollow(String artistId, String userId) {
        followRepo.deleteByUserIdAndArtistId(userId, artistId);
        artistRepo.decrementFollowers(artistId);
        return ApiResponse.success("Đã bỏ theo dõi", null);
    }

    /** Thêm/xóa album khỏi thư viện */
    public ApiResponse<Void> addAlbum(String albumId, String userId) {
        if (!likedAlbumRepo.existsByUserIdAndAlbumId(userId, albumId)) {
            User u = userRepo.findById(userId).orElseThrow();
            Album a = albumRepo.findById(albumId).orElseThrow();
            likedAlbumRepo.save(LikedAlbum.builder().user(u).album(a).build());
        }
        return ApiResponse.success("Đã thêm vào thư viện", null);
    }

    public ApiResponse<Void> removeAlbum(String albumId, String userId) {
        likedAlbumRepo.deleteByUserIdAndAlbumId(userId, albumId);
        return ApiResponse.success("Đã xóa khỏi thư viện", null);
    }

    private SongResponse toSong(Song s, boolean liked) {
        return SongResponse.builder().id(s.getId()).name(s.getName())
                .artistId(s.getArtist().getId()).artistName(s.getArtist().getName())
                .albumId(s.getAlbum()!=null?s.getAlbum().getId():null)
                .albumName(s.getAlbum()!=null?s.getAlbum().getName():null)
                .duration(s.getDuration()).url(s.getUrl()).image(s.getImage())
                .playCount(s.getPlayCount()).likeCount(s.getLikeCount()).liked(liked).build();
    }
}
