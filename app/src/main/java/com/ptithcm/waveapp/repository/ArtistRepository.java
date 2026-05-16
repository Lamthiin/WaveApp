package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Artist;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArtistRepository {

    private final SQLiteDatabase db;

    public ArtistRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public List<Artist> findByActiveTrue() { return findAll(); }

    public List<Artist> searchByName(String kw) {
        List<Artist> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_ARTISTS, null,
                DatabaseHelper.COL_ARTIST_NAME + " LIKE ?",
                new String[]{"%" + kw + "%"}, null, null, null);
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public List<Artist> findTopArtists() {
        List<Artist> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_ARTISTS, null, null, null, null, null,
                DatabaseHelper.COL_ARTIST_FOLLOWERS + " DESC", "10");
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public Optional<Artist> findById(String id) {
        Cursor c = db.query(DatabaseHelper.TABLE_ARTISTS, null,
                DatabaseHelper.COL_ARTIST_ID + "=?", new String[]{id}, null, null, null);
        if (c != null && c.moveToFirst()) { Artist a = map(c); c.close(); return Optional.of(a); }
        if (c != null) c.close();
        return Optional.empty();
    }

    public void save(Artist artist) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ARTIST_ID, artist.getId());
        cv.put(DatabaseHelper.COL_ARTIST_NAME, artist.getName());
        cv.put(DatabaseHelper.COL_ARTIST_IMAGE, artist.getImage());
        cv.put(DatabaseHelper.COL_ARTIST_BIO, artist.getBio());
        db.insertWithOnConflict(DatabaseHelper.TABLE_ARTISTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void incrementFollowers(String id) {
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_ARTISTS + " SET " +
                DatabaseHelper.COL_ARTIST_FOLLOWERS + "=" + DatabaseHelper.COL_ARTIST_FOLLOWERS +
                "+1 WHERE " + DatabaseHelper.COL_ARTIST_ID + "=?", new String[]{id});
    }

    public void decrementFollowers(String id) {
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_ARTISTS + " SET " +
                DatabaseHelper.COL_ARTIST_FOLLOWERS + "=MAX(0," + DatabaseHelper.COL_ARTIST_FOLLOWERS +
                "-1) WHERE " + DatabaseHelper.COL_ARTIST_ID + "=?", new String[]{id});
    }

    private List<Artist> findAll() {
        List<Artist> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_ARTISTS, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    private Artist map(Cursor c) {
        return Artist.builder()
                .id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_ID)))
                .name(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_NAME)))
                .image(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_IMAGE)))
                .bio(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_BIO)))
                .followersCount(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_FOLLOWERS)))
                .build();
    }
}
