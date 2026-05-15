package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.dto.request.PlaylistRequest;
import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.exception.*;
import com.ptithcm.waveapp.model.*;
import com.ptithcm.waveapp.repository.*;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Xử lý:
 *   - MyPlaylistsActivity.java        → getMyPlaylists(), createPlaylist(), deletePlaylist()
 *   - PlaylistDetailActivity.java     → getPlaylistDetail(), updatePlaylist()
 *   - AddSongsToPlaylistActivity.java → getAvailableSongs(), addSong(), removeSong()
 */
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository     playlistRepo;
    private final PlaylistSongRepository playlistSongRepo;
    private final SongRepository         songRepo;
    private final UserRepository         userRepo;
    private final LikedSongRepository    likedSongRepo;

    // ── MyPlaylistsActivity: danh sách playlist của user ──
    public List<PlaylistResponse> getMyPlaylists(String userId) {
        return playlistRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toPlaylistSummary).collect(Collectors.toList());
    }

    // ── MyPlaylistsActivity: tạo playlist mới (createPlaylistButton FAB) ──
    public PlaylistResponse createPlaylist(String userId, PlaylistRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        Playlist p = Playlist.builder()
                .id(UUID.randomUUID().toString())
                .user(user).name(req.getName())
                .image(req.getImage()).isPublic(req.isPublic())
                .build();
        playlistRepo.save(p);
        return toPlaylistSummary(p);
    }

    // ── PlaylistDetailActivity: chi tiết playlist + danh sách bài ──
    public PlaylistResponse getPlaylistDetail(String playlistId, String userId) {
        Playlist p = findOwned(playlistId, userId);
        List<SongResponse> songs = playlistSongRepo.findByPlaylistIdOrderByPosition(playlistId)
                .stream().map(ps -> toSongResponse(ps.getSong(), userId)).collect(Collectors.toList());
        return PlaylistResponse.builder()
                .id(p.getId()).name(p.getName()).image(p.getImage())
                .userId(p.getUser().getId()).userName(p.getUser().getName())
                .isPublic(p.isPublic()).songCount(songs.size())
                .createdAt(p.getCreatedAt()).songs(songs)
                .build();
    }

    // ── PlaylistDetailActivity: đổi tên playlist ──
    public PlaylistResponse updatePlaylist(String playlistId, String userId, PlaylistRequest req) {
        Playlist p = findOwned(playlistId, userId);
        p.setName(req.getName());
        if (req.getImage() != null) p.setImage(req.getImage());
        p.setPublic(req.isPublic());
        playlistRepo.save(p);
        return toPlaylistSummary(p);
    }

    // ── MyPlaylistsActivity: xóa playlist ──
    public void deletePlaylist(String playlistId, String userId) {
        findOwned(playlistId, userId);
        playlistRepo.deleteById(playlistId);
    }

    // ── AddSongsToPlaylistActivity: lấy bài chưa có trong playlist ──
    public List<SongResponse> getAvailableSongs(String playlistId, String userId,
                                                         String keyword) {
        findOwned(playlistId, userId); // kiểm tra quyền
        List<Song> songPage = (keyword != null && !keyword.isEmpty())
                ? songRepo.searchByName(keyword)
                : songRepo.findByActiveTrue();

        // Lọc ra bài chưa có trong playlist
        Set<String> inPlaylist = playlistSongRepo.findByPlaylistIdOrderByPosition(playlistId)
                .stream().map(ps -> ps.getSong().getId()).collect(Collectors.toSet());

        return songPage.stream()
                .filter(s -> !inPlaylist.contains(s.getId()))
                .map(s -> toSongResponse(s, userId)).collect(Collectors.toList());
    }

    // ── AddSongsToPlaylistActivity: thêm bài vào playlist ──
    public void addSong(String playlistId, String songId, String userId) {
        findOwned(playlistId, userId);
        if (playlistSongRepo.existsByPlaylistIdAndSongId(playlistId, songId))
            throw new BadRequestException("Bài hát đã có trong playlist");

        Playlist p = playlistRepo.findById(playlistId).get();
        Song s = songRepo.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài hát không tồn tại"));
        int pos = playlistSongRepo.countByPlaylistId(playlistId);
        playlistSongRepo.save(PlaylistSong.builder().playlist(p).song(s).position(pos).build());
    }

    // ── PlaylistDetailActivity: xóa bài khỏi playlist ──
    public void removeSong(String playlistId, String songId, String userId) {
        findOwned(playlistId, userId);
        playlistSongRepo.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    // ── Helpers ──
    private Playlist findOwned(String playlistId, String userId) {
        Playlist p = playlistRepo.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist không tồn tại"));
        if (!p.getUser().getId().equals(userId))
            throw new BadRequestException("Bạn không có quyền thao tác playlist này");
        return p;
    }

    private PlaylistResponse toPlaylistSummary(Playlist p) {
        return PlaylistResponse.builder()
                .id(p.getId()).name(p.getName()).image(p.getImage())
                .userId(p.getUser().getId()).userName(p.getUser().getName())
                .isPublic(p.isPublic()).createdAt(p.getCreatedAt())
                .songCount(playlistSongRepo.countByPlaylistId(p.getId()))
                .build();
    }

    private SongResponse toSongResponse(Song s, String userId) {
        boolean liked = userId != null && likedSongRepo.existsByUserIdAndSongId(userId, s.getId());
        return SongResponse.builder()
                .id(s.getId()).name(s.getName())
                .artistId(s.getArtist().getId()).artistName(s.getArtist().getName())
                .albumId(s.getAlbum() != null ? s.getAlbum().getId() : null)
                .albumName(s.getAlbum() != null ? s.getAlbum().getName() : null)
                .duration(s.getDuration()).url(s.getUrl()).image(s.getImage())
                .playCount(s.getPlayCount()).likeCount(s.getLikeCount())
                .liked(liked).build();
    }
}
