package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.model.UserFollowArtist;
import java.util.ArrayList;
import java.util.List;

public class UserFollowArtistRepository {

    private final SQLiteDatabase db;

    public UserFollowArtistRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public boolean existsByUserIdAndArtistId(String userId, String artistId) {
        Cursor c = db.query(DatabaseHelper.TABLE_FOLLOW_ARTISTS, null,
                DatabaseHelper.COL_FA_USER_ID + "=? AND " + DatabaseHelper.COL_FA_ARTIST_ID + "=?",
                new String[]{userId, artistId}, null, null, null);
        boolean ex = c != null && c.getCount() > 0;
        if (c != null) c.close();
        return ex;
    }

    public void deleteByUserIdAndArtistId(String userId, String artistId) {
        db.delete(DatabaseHelper.TABLE_FOLLOW_ARTISTS,
                DatabaseHelper.COL_FA_USER_ID + "=? AND " + DatabaseHelper.COL_FA_ARTIST_ID + "=?",
                new String[]{userId, artistId});
    }

    public List<UserFollowArtist> findByUserIdOrderByFollowedAtDesc(String userId) {
        List<UserFollowArtist> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_FOLLOW_ARTISTS, null,
                DatabaseHelper.COL_FA_USER_ID + "=?", new String[]{userId},
                null, null, DatabaseHelper.COL_FA_FOLLOWED_AT + " DESC");
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public void save(UserFollowArtist ufa) {
        ContentValues cv = new ContentValues();
        if (ufa.getUser()   != null) cv.put(DatabaseHelper.COL_FA_USER_ID,   ufa.getUser().getId());
        if (ufa.getArtist() != null) cv.put(DatabaseHelper.COL_FA_ARTIST_ID, ufa.getArtist().getId());
        // FIX: followedAt là String
        if (ufa.getFollowedAt() != null) cv.put(DatabaseHelper.COL_FA_FOLLOWED_AT, ufa.getFollowedAt());
        db.insertWithOnConflict(DatabaseHelper.TABLE_FOLLOW_ARTISTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private UserFollowArtist map(Cursor c) {
        return UserFollowArtist.builder()
                .user(User.builder().id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_FA_USER_ID))).build())
                .artist(Artist.builder().id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_FA_ARTIST_ID))).build())
                .followedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_FA_FOLLOWED_AT)))
                .build();
    }
}
