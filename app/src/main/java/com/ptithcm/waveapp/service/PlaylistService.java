package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.model.PlaylistSong;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.PlaylistRepository;
import com.ptithcm.waveapp.repository.PlaylistSongRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaylistService {

    private final PlaylistRepository playlistRepo;
    private final PlaylistSongRepository playlistSongRepo;
    private final SongRepository songRepo;
    private final UserRepository userRepo;
    private final LikedSongRepository likedSongRepo;

    public PlaylistService(PlaylistRepository playlistRepo,
                           PlaylistSongRepository playlistSongRepo,
                           SongRepository songRepo,
                           UserRepository userRepo,
                           LikedSongRepository likedSongRepo) {
        this.playlistRepo = playlistRepo;
        this.playlistSongRepo = playlistSongRepo;
        this.songRepo = songRepo;
        this.userRepo = userRepo;
        this.likedSongRepo = likedSongRepo;
    }

    public List<Playlist> getMyPlaylists(String userId) {
        return playlistRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Playlist getPlaylistById(String playlistId) {
        return playlistRepo.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist không tồn tại"));
    }

    public Playlist createPlaylist(String userId, String name, String imagePath) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        Playlist p = Playlist.builder()
                .id(null)
                .user(user)
                .name(name)
                .image(imagePath)
                .build();

        playlistRepo.save(p);
        return p;
    }

    public List<Song> getSongsInPlaylist(String playlistId) {
        List<Song> songs = songRepo.findByPlaylistId(playlistId);

        if (songs.isEmpty()) {
            songs = songRepo.findByAlbumIdAndActiveTrue(playlistId);
        }

        return songs;
    }

    public void renamePlaylist(String playlistId, String userId, String newName) {
        findOwned(playlistId, userId);

        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException("Tên playlist không được để trống");
        }

        playlistRepo.updateName(playlistId, newName.trim());
    }

    public void deletePlaylist(String playlistId, String userId) {
        findOwned(playlistId, userId);
        playlistSongRepo.deleteByPlaylistId(playlistId);
        playlistRepo.deleteById(playlistId);
    }

    public List<Song> getAvailableSongs(String playlistId, String userId, String keyword) {
        findOwned(playlistId, userId);

        List<Song> allSongs = keyword != null && !keyword.isEmpty()
                ? songRepo.searchByName(keyword)
                : songRepo.findByActiveTrue();

        Set<String> inPlaylist = playlistSongRepo.findByPlaylistIdOrderByPosition(playlistId)
                .stream()
                .map(ps -> ps.getSong().getId())
                .collect(Collectors.toSet());

        return allSongs.stream()
                .filter(s -> !inPlaylist.contains(s.getId()))
                .collect(Collectors.toList());
    }

    public void addSong(String playlistId, String songId, String userId) {
        findOwned(playlistId, userId);

        if (playlistSongRepo.existsByPlaylistIdAndSongId(playlistId, songId)) {
            throw new RuntimeException("Bài hát đã có trong playlist");
        }

        Playlist p = playlistRepo.findById(playlistId).get();

        Song s = songRepo.findById(songId)
                .orElseThrow(() -> new RuntimeException("Bài hát không tồn tại"));

        int pos = playlistSongRepo.countByPlaylistId(playlistId);

        playlistSongRepo.save(
                PlaylistSong.builder()
                        .playlist(p)
                        .song(s)
                        .position(pos)
                        .build()
        );

        playlistRepo.touchUpdatedAt(playlistId);
    }

    public void removeSong(String playlistId, String songId, String userId) {
        findOwned(playlistId, userId);
        playlistSongRepo.deleteByPlaylistIdAndSongId(playlistId, songId);
        playlistRepo.touchUpdatedAt(playlistId);
    }

    public void updatePlaylistImage(String playlistId, String userId, String imageUrl) {
        findOwned(playlistId, userId);
        playlistRepo.updateImage(playlistId, imageUrl);
    }

    private Playlist findOwned(String playlistId, String userId) {
        Playlist p = playlistRepo.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist không tồn tại"));

        if (p.getUser() == null || !p.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thao tác playlist này");
        }

        return p;
    }
}