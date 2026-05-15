package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.model.UserFollowArtist;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlUserFollowArtistRepository implements UserFollowArtistRepository {
    private final DatabaseHelper dbHelper;

    public SqlUserFollowArtistRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean existsByUserIdAndArtistId(String userId, String artistId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FOLLOW_ARTISTS, null,
                DatabaseHelper.COL_FA_USER_ID + "=? AND " + DatabaseHelper.COL_FA_ARTIST_ID + "=?",
                new String[]{userId, artistId}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    @Override
    public void deleteByUserIdAndArtistId(String userId, String artistId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_FOLLOW_ARTISTS,
                DatabaseHelper.COL_FA_USER_ID + "=? AND " + DatabaseHelper.COL_FA_ARTIST_ID + "=?",
                new String[]{userId, artistId});
    }

    @Override
    public List<UserFollowArtist> findByUserIdOrderByFollowedAtDesc(String userId) {
        List<UserFollowArtist> followArtists = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FOLLOW_ARTISTS, null, DatabaseHelper.COL_FA_USER_ID + "=?",
                new String[]{userId}, null, null, DatabaseHelper.COL_FA_FOLLOWED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                followArtists.add(mapCursorToUserFollowArtist(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return followArtists;
    }

    @Override
    public void save(UserFollowArtist userFollowArtist) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // SỬA LỖI: Lấy ID từ đối tượng User và Artist
        if (userFollowArtist.getUser() != null) {
            values.put(DatabaseHelper.COL_FA_USER_ID, userFollowArtist.getUser().getId());
        }
        if (userFollowArtist.getArtist() != null) {
            values.put(DatabaseHelper.COL_FA_ARTIST_ID, userFollowArtist.getArtist().getId());
        }

        // Chuyển LocalDateTime thành String để lưu vào database
        if (userFollowArtist.getFollowedAt() != null) {
            values.put(DatabaseHelper.COL_FA_FOLLOWED_AT, userFollowArtist.getFollowedAt().toString());
        }

        db.insertWithOnConflict(DatabaseHelper.TABLE_FOLLOW_ARTISTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private UserFollowArtist mapCursorToUserFollowArtist(Cursor cursor) {
        String userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FA_USER_ID));
        String artistId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FA_ARTIST_ID));
        String followedAtStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FA_FOLLOWED_AT));

        // Chuyển đổi String ngược lại thành LocalDateTime
        LocalDateTime followedAt = null;
        if (followedAtStr != null && !followedAtStr.isEmpty()) {
            try {
                followedAt = LocalDateTime.parse(followedAtStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return UserFollowArtist.builder()
                .user(User.builder().id(userId).build())
                .artist(Artist.builder().id(artistId).build())
                .followedAt(followedAt)
                .build();
    }
}