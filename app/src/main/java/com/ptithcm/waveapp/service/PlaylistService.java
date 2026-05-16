package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.model.*;
import com.ptithcm.waveapp.repository.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MyPlaylistsActivity, PlaylistDetailActivity, AddSongsToPlaylistActivity
 */
public class PlaylistService {

    private final PlaylistRepository     playlistRepo;
    private final PlaylistSongRepository playlistSongRepo;
    private final SongRepository         songRepo;
    private final UserRepository         userRepo;
    private final LikedSongRepository    likedSongRepo;

    public PlaylistService(PlaylistRepository playlistRepo, PlaylistSongRepository playlistSongRepo,
                           SongRepository songRepo, UserRepository userRepo,
                           LikedSongRepository likedSongRepo) {
        this.playlistRepo     = playlistRepo;
        this.playlistSongRepo = playlistSongRepo;
        this.songRepo         = songRepo;
        this.userRepo         = userRepo;
        this.likedSongRepo    = likedSongRepo;
    }

    /** MyPlaylistsActivity: danh sách playlist */
    public List<Playlist> getMyPlaylists(String userId) {
        return playlistRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Playlist getPlaylistById(String playlistId) {
        return playlistRepo.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist không tồn tại"));
    }

    /** MyPlaylistsActivity: tạo playlist (FAB) */
    public Playlist createPlaylist(String userId, String name, String imagePath) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        Playlist p = Playlist.builder()
                .id(UUID.randomUUID().toString())
                .user(user).name(name).image(imagePath)
                .build();
        playlistRepo.save(p);
        return p;
    }

    /** PlaylistDetailActivity: bài hát trong playlist hoặc album */
    public List<Song> getSongsInPlaylist(String playlistId) {
        List<Song> songs = songRepo.findByPlaylistId(playlistId);
        // Nếu không có bài trong playlist, thử tìm bài trong Album (trường hợp click từ Album Home)
        if (songs.isEmpty()) {
            songs = songRepo.findByAlbumIdAndActiveTrue(playlistId);
        }
        return songs;
    }

    /** PlaylistDetailActivity: đổi tên */
    public void renamePlaylist(String playlistId, String userId, String newName) {
        Playlist p = findOwned(playlistId, userId);
        p.setName(newName);
        playlistRepo.save(p);
    }

    /** MyPlaylistsActivity: xóa playlist */
    public void deletePlaylist(String playlistId, String userId) {
        findOwned(playlistId, userId);
        playlistRepo.deleteById(playlistId);
    }

    /** AddSongsToPlaylistActivity: bài hát chưa có trong playlist */
    public List<Song> getAvailableSongs(String playlistId, String userId, String keyword) {
        findOwned(playlistId, userId);

        // FIX 5: tìm kiếm hoặc lấy tất cả — đúng method
        List<Song> allSongs = (keyword != null && !keyword.isEmpty())
                ? songRepo.searchByName(keyword)
                : songRepo.findByActiveTrue();

        Set<String> inPlaylist = playlistSongRepo.findByPlaylistIdOrderByPosition(playlistId)
                .stream().map(ps -> ps.getSong().getId()).collect(Collectors.toSet());

        return allSongs.stream()
                .filter(s -> !inPlaylist.contains(s.getId()))
                .collect(Collectors.toList());
    }

    /** AddSongsToPlaylistActivity: thêm bài */
    public void addSong(String playlistId, String songId, String userId) {
        findOwned(playlistId, userId);
        if (playlistSongRepo.existsByPlaylistIdAndSongId(playlistId, songId))
            throw new RuntimeException("Bài hát đã có trong playlist");

        Playlist p = playlistRepo.findById(playlistId).get();
        Song s = songRepo.findById(songId)
                .orElseThrow(() -> new RuntimeException("Bài hát không tồn tại"));
        int pos = playlistSongRepo.countByPlaylistId(playlistId);
        playlistSongRepo.save(PlaylistSong.builder().playlist(p).song(s).position(pos).build());
    }

    /** PlaylistDetailActivity: xóa bài */
    public void removeSong(String playlistId, String songId, String userId) {
        findOwned(playlistId, userId);
        playlistSongRepo.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    private Playlist findOwned(String playlistId, String userId) {
        Playlist p = playlistRepo.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist không tồn tại"));
        if (!p.getUser().getId().equals(userId))
            throw new RuntimeException("Bạn không có quyền thao tác playlist này");
        return p;
    }
}
