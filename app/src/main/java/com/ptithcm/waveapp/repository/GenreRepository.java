package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Genre;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreRepository {

    private final SQLiteDatabase db;

    public GenreRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public List<Genre> findAll() {
        List<Genre> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_GENRES, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public Optional<Genre> findById(String id) {
        Cursor c = db.query(DatabaseHelper.TABLE_GENRES, null,
                DatabaseHelper.COL_GENRE_ID + "=?", new String[]{id}, null, null, null);
        if (c != null && c.moveToFirst()) { Genre g = map(c); c.close(); return Optional.of(g); }
        if (c != null) c.close();
        return Optional.empty();
    }

    public Optional<Genre> findByName(String name) {
        Cursor c = db.query(DatabaseHelper.TABLE_GENRES, null,
                DatabaseHelper.COL_GENRE_NAME + "=?", new String[]{name}, null, null, null);
        if (c != null && c.moveToFirst()) { Genre g = map(c); c.close(); return Optional.of(g); }
        if (c != null) c.close();
        return Optional.empty();
    }

    public void save(Genre genre) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_GENRE_ID, genre.getId());
        cv.put(DatabaseHelper.COL_GENRE_NAME, genre.getName());
        cv.put(DatabaseHelper.COL_GENRE_DESCRIPTION, genre.getDescription());
        cv.put(DatabaseHelper.COL_GENRE_IMAGE_URL, genre.getImageUrl());
        db.insertWithOnConflict(DatabaseHelper.TABLE_GENRES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private Genre map(Cursor c) {
        return Genre.builder()
                .id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE_ID)))
                .name(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE_NAME)))
                .description(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE_DESCRIPTION)))
                .imageUrl(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE_IMAGE_URL)))
                .build();
    }
}
