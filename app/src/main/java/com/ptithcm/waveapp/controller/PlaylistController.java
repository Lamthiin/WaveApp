package com.ptithcm.waveapp.controller;

import com.ptithcm.waveapp.dto.request.PlaylistRequest;
import com.ptithcm.waveapp.dto.response.*;
import com.ptithcm.waveapp.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * MyPlaylistsActivity.java        → /api/playlists
 * PlaylistDetailActivity.java     → /api/playlists/{id}
 * AddSongsToPlaylistActivity.java → /api/playlists/{id}/songs/available
 */
@RequiredArgsConstructor
public class PlaylistController {
    private final PlaylistService playlistService;

    /** MyPlaylistsActivity: danh sách playlist của tôi */
    public ApiResponse<List<PlaylistResponse>> myPlaylists(String userId) {
        return ApiResponse.success(playlistService.getMyPlaylists(userId));
    }

    /** MyPlaylistsActivity: createPlaylistButton (FAB) */
    public ApiResponse<PlaylistResponse> create(PlaylistRequest req, String userId) {
        return ApiResponse.success("Tạo playlist thành công",
                playlistService.createPlaylist(userId, req));
    }

    /** PlaylistDetailActivity: chi tiết + danh sách bài */
    public ApiResponse<PlaylistResponse> detail(String id, String userId) {
        return ApiResponse.success(playlistService.getPlaylistDetail(id, userId));
    }

    /** PlaylistDetailActivity: đổi tên */
    public ApiResponse<PlaylistResponse> update(String id, PlaylistRequest req, String userId) {
        return ApiResponse.success("Cập nhật thành công",
                playlistService.updatePlaylist(id, userId, req));
    }

    /** MyPlaylistsActivity: xóa playlist */
    public ApiResponse<Void> delete(String id, String userId) {
        playlistService.deletePlaylist(id, userId);
        return ApiResponse.success("Đã xóa playlist", null);
    }

    /** AddSongsToPlaylistActivity: lấy bài hát chưa có trong playlist */
    public ApiResponse<List<SongResponse>> availableSongs(String id, String keyword, String userId) {
        return ApiResponse.success(playlistService.getAvailableSongs(id, userId, keyword));
    }

    /** AddSongsToPlaylistActivity: thêm bài vào playlist */
    public ApiResponse<Void> addSong(String id, String songId, String userId) {
        playlistService.addSong(id, songId, userId);
        return ApiResponse.success("Đã thêm vào playlist", null);
    }

    /** PlaylistDetailActivity: xóa bài khỏi playlist */
    public ApiResponse<Void> removeSong(String id, String songId, String userId) {
        playlistService.removeSong(id, songId, userId);
        return ApiResponse.success("Đã xóa khỏi playlist", null);
    }
}
