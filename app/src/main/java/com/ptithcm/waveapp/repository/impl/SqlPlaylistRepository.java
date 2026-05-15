package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.repository.PlaylistRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlPlaylistRepository implements PlaylistRepository {
    private final DatabaseHelper dbHelper;

    public SqlPlaylistRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public List<Playlist> findByUserIdOrderByCreatedAtDesc(String userId) {
        List<Playlist> playlists = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLISTS, null, DatabaseHelper.COL_PLAYLIST_USER_ID + "=?",
                new String[]{userId}, null, null, DatabaseHelper.COL_PLAYLIST_CREATED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(mapCursorToPlaylist(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return playlists;
    }

    @Override
    public Optional<Playlist> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLISTS, null, DatabaseHelper.COL_PLAYLIST_ID + "=?",
                new String[]{id}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Playlist playlist = mapCursorToPlaylist(cursor);
            cursor.close();
            return Optional.of(playlist);
        }
        if (cursor != null) cursor.close();
        return Optional.empty();
    }

    @Override
    public void save(Playlist playlist) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PLAYLIST_ID, playlist.getId());
        values.put(DatabaseHelper.COL_PLAYLIST_USER_ID, playlist.getUserId());
        values.put(DatabaseHelper.COL_PLAYLIST_NAME, playlist.getName());
        values.put(DatabaseHelper.COL_PLAYLIST_IMAGE, playlist.getImage());
        db.insertWithOnConflict(DatabaseHelper.TABLE_PLAYLISTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLISTS, DatabaseHelper.COL_PLAYLIST_ID + "=?", new String[]{id});
    }

    private Playlist mapCursorToPlaylist(Cursor cursor) {
        return Playlist.builder()
                .id(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_ID)))
                .userId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_USER_ID)))
                .name(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_NAME)))
                .image(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_IMAGE)))
                .createdAt(LocalDateTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_CREATED_AT))))
                .build();
    }
}
