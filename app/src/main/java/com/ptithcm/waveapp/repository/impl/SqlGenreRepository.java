package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Genre;
import com.ptithcm.waveapp.repository.GenreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlGenreRepository implements GenreRepository {
    private final DatabaseHelper dbHelper;

    public SqlGenreRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Optional<Genre> findByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GENRES, null, DatabaseHelper.COL_GENRE_NAME + "=?",
                new String[]{name}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Genre genre = mapCursorToGenre(cursor);
            cursor.close();
            return Optional.of(genre);
        }
        if (cursor != null) cursor.close();
        return Optional.empty();
    }

    @Override
    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GENRES, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                genres.add(mapCursorToGenre(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return genres;
    }

    @Override
    public Optional<Genre> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GENRES, null, DatabaseHelper.COL_GENRE_ID + "=?",
                new String[]{id}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Genre genre = mapCursorToGenre(cursor);
            cursor.close();
            return Optional.of(genre);
        }
        if (cursor != null) cursor.close();
        return Optional.empty();
    }

    @Override
    public void save(Genre genre) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_GENRE_ID, genre.getId());
        values.put(DatabaseHelper.COL_GENRE_NAME, genre.getName());
        values.put(DatabaseHelper.COL_GENRE_DESCRIPTION, genre.getDescription());
        values.put(DatabaseHelper.COL_GENRE_IMAGE_URL, genre.getImageUrl());
        db.insertWithOnConflict(DatabaseHelper.TABLE_GENRES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private Genre mapCursorToGenre(Cursor cursor) {
        return Genre.builder()
                .id(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE_ID)))
                .name(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE_NAME)))
                .description(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE_DESCRIPTION)))
                .imageUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE_IMAGE_URL)))
                .build();
    }
}
