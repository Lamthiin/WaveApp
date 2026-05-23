package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.model.User;

import java.time.LocalDateTime;
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

        Cursor c = db.query(
                DatabaseHelper.TABLE_PLAYLISTS,
                null,
                DatabaseHelper.COL_PLAYLIST_USER_ID + "=?",
                new String[]{userId},
                null,
                null,
                DatabaseHelper.COL_PLAYLIST_UPDATED_AT + " DESC"
        );

        if (c != null && c.moveToFirst()) {
            do {
                list.add(map(c));
            } while (c.moveToNext());
            c.close();
        }

        return list;
    }

    public Optional<Playlist> findById(String id) {
        Cursor c = db.query(
                DatabaseHelper.TABLE_PLAYLISTS,
                null,
                DatabaseHelper.COL_PLAYLIST_ID + "=?",
                new String[]{id},
                null,
                null,
                null
        );

        if (c != null && c.moveToFirst()) {
            Playlist p = map(c);
            c.close();
            return Optional.of(p);
        }

        if (c != null) c.close();
        return Optional.empty();
    }

    private String generateNextPlaylistId() {
        Cursor c = db.rawQuery(
                "SELECT " + DatabaseHelper.COL_PLAYLIST_ID +
                        " FROM " + DatabaseHelper.TABLE_PLAYLISTS +
                        " WHERE " + DatabaseHelper.COL_PLAYLIST_ID + " LIKE 'pl%'" +
                        " ORDER BY CAST(SUBSTR(" + DatabaseHelper.COL_PLAYLIST_ID + ", 3) AS INTEGER) DESC LIMIT 1",
                null
        );

        int nextNumber = 1;

        if (c != null && c.moveToFirst()) {
            String lastId = c.getString(0);

            if (lastId != null && lastId.startsWith("pl")) {
                try {
                    nextNumber = Integer.parseInt(lastId.substring(2)) + 1;
                } catch (Exception ignored) {
                }
            }

            c.close();
        }

        return String.format("pl%03d", nextNumber);
    }

    public void save(Playlist playlist) {
        String playlistId = playlist.getId();

        if (playlistId == null || playlistId.trim().isEmpty()) {
            playlistId = generateNextPlaylistId();
            playlist.setId(playlistId);
        }

        String now = LocalDateTime.now().toString();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PLAYLIST_ID, playlistId);
        cv.put(DatabaseHelper.COL_PLAYLIST_USER_ID,
                playlist.getUser() != null ? playlist.getUser().getId() : null);
        cv.put(DatabaseHelper.COL_PLAYLIST_NAME, playlist.getName());
        cv.put(DatabaseHelper.COL_PLAYLIST_IMAGE, playlist.getImage());
        cv.put(DatabaseHelper.COL_PLAYLIST_UPDATED_AT, now);

        if (findById(playlistId).isPresent()) {
            db.update(
                    DatabaseHelper.TABLE_PLAYLISTS,
                    cv,
                    DatabaseHelper.COL_PLAYLIST_ID + "=?",
                    new String[]{playlistId}
            );
        } else {
            cv.put(DatabaseHelper.COL_PLAYLIST_CREATED_AT, now);
            db.insert(DatabaseHelper.TABLE_PLAYLISTS, null, cv);
        }
    }

    public void updateName(String playlistId, String newName) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PLAYLIST_NAME, newName);
        cv.put(DatabaseHelper.COL_PLAYLIST_UPDATED_AT, LocalDateTime.now().toString());

        db.update(
                DatabaseHelper.TABLE_PLAYLISTS,
                cv,
                DatabaseHelper.COL_PLAYLIST_ID + "=?",
                new String[]{playlistId}
        );
    }

    public void updateImage(String playlistId, String imageUrl) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PLAYLIST_IMAGE, imageUrl);
        cv.put(DatabaseHelper.COL_PLAYLIST_UPDATED_AT, LocalDateTime.now().toString());

        db.update(
                DatabaseHelper.TABLE_PLAYLISTS,
                cv,
                DatabaseHelper.COL_PLAYLIST_ID + "=?",
                new String[]{playlistId}
        );
    }

    public void touchUpdatedAt(String playlistId) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PLAYLIST_UPDATED_AT, LocalDateTime.now().toString());

        db.update(
                DatabaseHelper.TABLE_PLAYLISTS,
                cv,
                DatabaseHelper.COL_PLAYLIST_ID + "=?",
                new String[]{playlistId}
        );
    }

    public void deleteById(String id) {
        db.delete(
                DatabaseHelper.TABLE_PLAYLISTS,
                DatabaseHelper.COL_PLAYLIST_ID + "=?",
                new String[]{id}
        );
    }

    private Playlist map(Cursor c) {
        String userId = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_USER_ID));

        return Playlist.builder()
                .id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_ID)))
                .user(User.builder().id(userId).build())
                .name(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_NAME)))
                .image(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_IMAGE)))
                .createdAt(parseDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_CREATED_AT))))
                .updatedAt(parseDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PLAYLIST_UPDATED_AT))))
                .build();
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(value.replace(" ", "T"));
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}