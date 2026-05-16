package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaylistRepository {

    private final SQLiteDatabase db;

    public PlaylistRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public List<Playlist> findByUserIdOrderByCreatedAtDesc(String userId) {
        List<Playlist> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_PLAYLISTS, null,
                DatabaseHelper.COL_PLAYLIST_USER_ID + "=?", new String[]{userId},
                null, null, DatabaseHelper.COL_PLAYLIST_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public Optional<Playlist> findById(String id) {
        Cursor c = db.query(DatabaseHelper.TABLE_PLAYLISTS, null,
                DatabaseHelper.COL_PLAYLIST_ID + "=?", new String[]{id}, null, null, null);
        if (c != null && c.moveToFirst()) { Playlist p = map(c); c.close(); return Optional.of(p); }
        if (c != null) c.close();
        return Optional.empty();
    }

    public void save(Playlist playlist) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PLAYLIST_ID,   playlist.getId());
        // FIX: Playlist.java đã bỏ userId riêng → lấy từ user.getId()
        cv.put(DatabaseHelper.COL_PLAYLIST_USER_ID, playlist.getUser() != null ? playlist.getUser().getId() : null);
        cv.put(DatabaseHelper.COL_PLAYLIST_NAME,  playlist.getName());
        cv.put(DatabaseHelper.COL_PLAYLIST_IMAGE, playlist.getImage());
        db.insertWithOnConflict(DatabaseHelper.TABLE_PLAYLISTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteById(String id) {
        db.delete(DatabaseHelper.TABLE_PLAYLISTS, DatabaseHelper.COL_PLAYLIST_ID + "=?", new String[]{id});
    }

    private Playlist map(Cursor c) {
        String userId = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_USER_ID));
        return Playlist.builder()
                .id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_ID)))
                .user(User.builder().id(userId).build())  // FIX: set user thay vì userId
                .name(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_NAME)))
                .image(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_IMAGE)))
                // FIX: createdAt là String trong model → không parse LocalDateTime
                .createdAt(null)
                .build();
    }
}
