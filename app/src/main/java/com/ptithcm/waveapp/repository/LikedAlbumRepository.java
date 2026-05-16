package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.LikedAlbum;
import com.ptithcm.waveapp.model.User;
import java.util.ArrayList;
import java.util.List;

public class LikedAlbumRepository {

    private final SQLiteDatabase db;

    public LikedAlbumRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public boolean existsByUserIdAndAlbumId(String userId, String albumId) {
        Cursor c = db.query(DatabaseHelper.TABLE_LIKED_ALBUMS, null,
                DatabaseHelper.COL_LA_USER_ID + "=? AND " + DatabaseHelper.COL_LA_ALBUM_ID + "=?",
                new String[]{userId, albumId}, null, null, null);
        boolean ex = c != null && c.getCount() > 0;
        if (c != null) c.close();
        return ex;
    }

    public void deleteByUserIdAndAlbumId(String userId, String albumId) {
        db.delete(DatabaseHelper.TABLE_LIKED_ALBUMS,
                DatabaseHelper.COL_LA_USER_ID + "=? AND " + DatabaseHelper.COL_LA_ALBUM_ID + "=?",
                new String[]{userId, albumId});
    }

    public List<LikedAlbum> findByUserIdOrderByAddedAtDesc(String userId) {
        List<LikedAlbum> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_LIKED_ALBUMS, null,
                DatabaseHelper.COL_LA_USER_ID + "=?", new String[]{userId},
                null, null, DatabaseHelper.COL_LA_ADDED_AT + " DESC");
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public void save(LikedAlbum la) {
        ContentValues cv = new ContentValues();
        if (la.getUser()  != null) cv.put(DatabaseHelper.COL_LA_USER_ID,  la.getUser().getId());
        if (la.getAlbum() != null) cv.put(DatabaseHelper.COL_LA_ALBUM_ID, la.getAlbum().getId());
        // FIX: addedAt là String
        if (la.getAddedAt() != null) cv.put(DatabaseHelper.COL_LA_ADDED_AT, la.getAddedAt());
        db.insertWithOnConflict(DatabaseHelper.TABLE_LIKED_ALBUMS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private LikedAlbum map(Cursor c) {
        return LikedAlbum.builder()
                .user(User.builder().id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LA_USER_ID))).build())
                .album(Album.builder().id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LA_ALBUM_ID))).build())
                .addedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LA_ADDED_AT)))
                .build();
    }
}
