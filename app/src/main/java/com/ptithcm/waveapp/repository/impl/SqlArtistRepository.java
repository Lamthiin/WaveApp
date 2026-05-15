package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.repository.ArtistRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlArtistRepository implements ArtistRepository {
    private final DatabaseHelper dbHelper;

    public SqlArtistRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public List<Artist> findByActiveTrue() {
        return findAll();
    }

    @Override
    public List<Artist> searchByName(String kw) {
        List<Artist> artists = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTISTS, null, DatabaseHelper.COL_ARTIST_NAME + " LIKE ?",
                new String[]{"%" + kw + "%"}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                artists.add(mapCursorToArtist(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return artists;
    }

    @Override
    public List<Artist> findTopArtists() {
        List<Artist> artists = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTISTS, null, null, null, null, null, DatabaseHelper.COL_ARTIST_FOLLOWERS + " DESC", "10");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                artists.add(mapCursorToArtist(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return artists;
    }

    @Override
    public void incrementFollowers(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_ARTISTS + " SET " + DatabaseHelper.COL_ARTIST_FOLLOWERS + " = " + DatabaseHelper.COL_ARTIST_FOLLOWERS + " + 1 WHERE " + DatabaseHelper.COL_ARTIST_ID + " = ?", new String[]{id});
    }

    @Override
    public void decrementFollowers(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_ARTISTS + " SET " + DatabaseHelper.COL_ARTIST_FOLLOWERS + " = MAX(0, " + DatabaseHelper.COL_ARTIST_FOLLOWERS + " - 1) WHERE " + DatabaseHelper.COL_ARTIST_ID + " = ?", new String[]{id});
    }

    @Override
    public Optional<Artist> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTISTS, null, DatabaseHelper.COL_ARTIST_ID + "=?",
                new String[]{id}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Artist artist = mapCursorToArtist(cursor);
            cursor.close();
            return Optional.of(artist);
        }
        if (cursor != null) cursor.close();
        return Optional.empty();
    }

    @Override
    public void save(Artist artist) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ARTIST_ID, artist.getId());
        values.put(DatabaseHelper.COL_ARTIST_NAME, artist.getName());
        values.put(DatabaseHelper.COL_ARTIST_IMAGE, artist.getImage());
        values.put(DatabaseHelper.COL_ARTIST_BIO, artist.getBio());
        // Note: followers count usually managed separately but can be included
        db.insertWithOnConflict(DatabaseHelper.TABLE_ARTISTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private List<Artist> findAll() {
        List<Artist> artists = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTISTS, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                artists.add(mapCursorToArtist(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return artists;
    }

    private Artist mapCursorToArtist(Cursor cursor) {
        return Artist.builder()
                .id(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_ID)))
                .name(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_NAME)))
                .image(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_IMAGE)))
                .bio(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_BIO)))
                .build();
    }
}
