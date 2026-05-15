package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.model.PlaylistSong;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.PlaylistSongRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.LocalDateTime; // Đảm bảo đã import thư viện này
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class SqlPlaylistSongRepository implements PlaylistSongRepository {
    private final DatabaseHelper dbHelper;

    public SqlPlaylistSongRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public List<PlaylistSong> findByPlaylistIdOrderByPosition(String playlistId) {
        List<PlaylistSong> playlistSongs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, DatabaseHelper.COL_PS_PLAYLIST_ID + "=?",
                new String[]{playlistId}, null, null, DatabaseHelper.COL_PS_POSITION + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlistSongs.add(mapCursorToPlaylistSong(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return playlistSongs;
    }

    @Override
    public Optional<PlaylistSong> findByPlaylistIdAndSongId(String playlistId, String songId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null,
                DatabaseHelper.COL_PS_PLAYLIST_ID + "=? AND " + DatabaseHelper.COL_PS_SONG_ID + "=?",
                new String[]{playlistId, songId}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            PlaylistSong ps = mapCursorToPlaylistSong(cursor);
            cursor.close();
            return Optional.of(ps);
        }
        if (cursor != null) cursor.close();
        return Optional.empty();
    }

    @Override
    public boolean existsByPlaylistIdAndSongId(String playlistId, String songId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null,
                DatabaseHelper.COL_PS_PLAYLIST_ID + "=? AND " + DatabaseHelper.COL_PS_SONG_ID + "=?",
                new String[]{playlistId, songId}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    @Override
    public void deleteByPlaylistIdAndSongId(String userId, String songId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS,
                DatabaseHelper.COL_PS_PLAYLIST_ID + "=? AND " + DatabaseHelper.COL_PS_SONG_ID + "=?",
                new String[]{userId, songId});
    }

    @Override
    public int countByPlaylistId(String playlistId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PLAYLIST_SONGS + " WHERE " + DatabaseHelper.COL_PS_PLAYLIST_ID + "=?", new String[]{playlistId});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    @Override
    public void save(PlaylistSong playlistSong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // SỬA LỖI: Lấy ID từ đối tượng Playlist và Song
        if (playlistSong.getPlaylist() != null) {
            values.put(DatabaseHelper.COL_PS_PLAYLIST_ID, playlistSong.getPlaylist().getId());
        }
        if (playlistSong.getSong() != null) {
            values.put(DatabaseHelper.COL_PS_SONG_ID, playlistSong.getSong().getId());
        }

        values.put(DatabaseHelper.COL_PS_POSITION, playlistSong.getPosition());
        db.insertWithOnConflict(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }


// ... bên trong class SqlPlaylistSongRepository ...

    private PlaylistSong mapCursorToPlaylistSong(Cursor cursor) {
        String playlistId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PS_PLAYLIST_ID));
        String songId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PS_SONG_ID));

        // 1. Lấy chuỗi ngày tháng từ Database
        String addedAtStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PS_ADDED_AT));

        // 2. Chuyển đổi String thành LocalDateTime
        // Mặc định LocalDateTime.parse() hỗ trợ định dạng ISO-8601 (VD: 2023-10-27T10:15:30)
        LocalDateTime addedAt = null;
        if (addedAtStr != null && !addedAtStr.isEmpty()) {
            try {
                addedAt = LocalDateTime.parse(addedAtStr);
            } catch (Exception e) {
                // Nếu lưu format khác, bạn có thể dùng DateTimeFormatter để parse
                // addedAt = LocalDateTime.parse(addedAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                e.printStackTrace();
            }
        }

        return PlaylistSong.builder()
                .playlist(Playlist.builder().id(playlistId).build())
                .song(Song.builder().id(songId).build())
                .position(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PS_POSITION)))
                // 3. Truyền đối tượng LocalDateTime vào đây (Không còn lỗi nữa)
                .addedAt(addedAt)
                .build();
    }
}