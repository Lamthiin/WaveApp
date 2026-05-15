package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.LikedSong;
import com.ptithcm.waveapp.model.User; // Đảm bảo đã import
import com.ptithcm.waveapp.model.Song; // Đảm bảo đã import
import com.ptithcm.waveapp.repository.LikedSongRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlLikedSongRepository implements LikedSongRepository {
    private final DatabaseHelper dbHelper;

    public SqlLikedSongRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean existsByUserIdAndSongId(String userId, String songId) {
        return false;
    }

    @Override
    public void deleteByUserIdAndSongId(String userId, String songId) {

    }

    @Override
    public List<LikedSong> findByUserIdOrderByLikedAtDesc(String userId) {
        return Collections.emptyList();
    }

    @Override
    public void save(LikedSong likedSong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // SỬA LỖI TẠI ĐÂY: Truy cập ID thông qua đối tượng User và Song
        // Giả sử trong lớp User có phương thức getId() trả về String hoặc Long
        values.put(DatabaseHelper.COL_LS_USER_ID, likedSong.getUser().getId());
        values.put(DatabaseHelper.COL_LS_SONG_ID, likedSong.getSong().getId());

        // Lưu ý: likedAt có thể cần convert sang String để lưu vào SQLite
        if (likedSong.getLikedAt() != null) {
            values.put(DatabaseHelper.COL_LS_LIKED_AT, likedSong.getLikedAt().toString());
        }

        db.insertWithOnConflict(DatabaseHelper.TABLE_LIKED_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Các phương thức khác (existsByUserIdAndSongId, delete...)
    // cũng cần cập nhật tương tự nếu bạn truyền vào đối tượng LikedSong.
}