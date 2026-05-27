package com.ptithcm.waveapp.service;

import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.*;

import java.util.List;

/**
 * UserProfileActivity: xem profile, cập nhật name/avatar, thống kê
 */
public class UserProfileService {

    private final UserRepository             userRepo;
    private final LikedSongRepository        likedSongRepo;
    private final LikedAlbumRepository       likedAlbumRepo;
    private final UserFollowArtistRepository  followRepo;
    private final PlaylistRepository          playlistRepo;
    private final SongRepository              songRepo;
    private final AlbumRepository             albumRepo;

    public UserProfileService(UserRepository userRepo, LikedSongRepository likedSongRepo,
                               LikedAlbumRepository likedAlbumRepo,
                               UserFollowArtistRepository followRepo,
                               PlaylistRepository playlistRepo,
                               SongRepository songRepo,
                               AlbumRepository albumRepo) {
        this.userRepo      = userRepo;
        this.likedSongRepo = likedSongRepo;
        this.likedAlbumRepo = likedAlbumRepo;
        this.followRepo    = followRepo;
        this.playlistRepo  = playlistRepo;
        this.songRepo      = songRepo;
        this.albumRepo     = albumRepo;
    }

    /** Lấy thông tin user */
    public User getProfile(String userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    /** Cập nhật name và avatar */
    public User updateProfile(String userId, String name, String avatarPath) {
        User user = getProfile(userId);
        if (name       != null) user.setName(name);
        if (avatarPath != null) user.setAvatar(avatarPath);
        userRepo.save(user);
        return user;
    }

    /** Số bài hát đã like (fragment_library tabSongs) */
    public int getLikedSongsCount(String userId) {
        return likedSongRepo.findByUserIdOrderByLikedAtDesc(userId).size();
    }

    /** Số album đã lưu (fragment_library tabAlbums) */
    public int getLikedAlbumsCount(String userId) {
        return likedAlbumRepo.findByUserIdOrderByAddedAtDesc(userId).size();
    }

    /** Số nghệ sĩ đang follow (fragment_library tabArtists) */
    public int getFollowingArtistsCount(String userId) {
        return followRepo.findByUserIdOrderByFollowedAtDesc(userId).size();
    }

    /** Số playlist (fragment_library tabCustomPlaylists) */
    public int getPlaylistsCount(String userId) {
        return playlistRepo.findByUserIdOrderByCreatedAtDesc(userId).size();
    }

    public List<Song> getLikedSongs(String userId) {
        return songRepo.findLikedByUser(userId);
    }

    public List<Artist> getFollowingArtists(String userId) {
        return songRepo.findArtistsFollowedByUser(userId);
    }

    public List<Album> getLikedAlbums(String userId) {
        return albumRepo.findLikedByUser(userId);
    }

    public List<Playlist> getMyPlaylists(String userId) {
        return playlistRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // FIX 6: role là String → dùng user.getRole() trực tiếp, không .name()
    // Không cần method riêng vì User.getRole() đã trả về String "USER"/"ADMIN"
}
