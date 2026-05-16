package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.LikedSong;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.model.User;
import java.util.ArrayList;
import java.util.List;

public class LikedSongRepository {

    private final SQLiteDatabase db;

    public LikedSongRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    // FIX: implement thật, không để return false/empty
    public boolean existsByUserIdAndSongId(String userId, String songId) {
        Cursor c = db.query(DatabaseHelper.TABLE_LIKED_SONGS, null,
                DatabaseHelper.COL_LS_USER_ID + "=? AND " + DatabaseHelper.COL_LS_SONG_ID + "=?",
                new String[]{userId, songId}, null, null, null);
        boolean ex = c != null && c.getCount() > 0;
        if (c != null) c.close();
        return ex;
    }

    public void deleteByUserIdAndSongId(String userId, String songId) {
        db.delete(DatabaseHelper.TABLE_LIKED_SONGS,
                DatabaseHelper.COL_LS_USER_ID + "=? AND " + DatabaseHelper.COL_LS_SONG_ID + "=?",
                new String[]{userId, songId});
    }

    public List<LikedSong> findByUserIdOrderByLikedAtDesc(String userId) {
        List<LikedSong> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_LIKED_SONGS, null,
                DatabaseHelper.COL_LS_USER_ID + "=?", new String[]{userId},
                null, null, DatabaseHelper.COL_LS_LIKED_AT + " DESC");
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public void save(LikedSong ls) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_LS_USER_ID, ls.getUser().getId());
        cv.put(DatabaseHelper.COL_LS_SONG_ID, ls.getSong().getId());
        // FIX: likedAt là String trong model
        if (ls.getLikedAt() != null) cv.put(DatabaseHelper.COL_LS_LIKED_AT, ls.getLikedAt());
        db.insertWithOnConflict(DatabaseHelper.TABLE_LIKED_SONGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private LikedSong map(Cursor c) {
        return LikedSong.builder()
                .user(User.builder().id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LS_USER_ID))).build())
                .song(Song.builder().id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LS_SONG_ID))).build())
                .likedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LS_LIKED_AT)))
                .build();
    }
}
