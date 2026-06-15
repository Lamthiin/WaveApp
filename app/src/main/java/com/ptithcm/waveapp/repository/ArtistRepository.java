package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Artist;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ArtistRepository {

    private final SQLiteDatabase db;

    public ArtistRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public List<Artist> findByActiveTrue() { return findAll(); }

    public List<Artist> findByHidden() { return findAllByActive(false); }

    public List<Artist> searchByName(String kw) {
        List<Artist> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_ARTISTS, null,
                DatabaseHelper.COL_ARTIST_NAME + " LIKE ? AND " + DatabaseHelper.COL_ARTIST_ACTIVE + "=?",
                new String[]{"%" + kw + "%", "1"}, null, null, null);
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public List<Artist> findTopArtists() {
        List<Artist> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_ARTISTS, null,
                DatabaseHelper.COL_ARTIST_ACTIVE + "=?",
                new String[]{"1"},
                null, null,
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
        cv.put(DatabaseHelper.COL_ARTIST_FOLLOWERS, artist.getFollowersCount());
        cv.put(DatabaseHelper.COL_ARTIST_ACTIVE, artist.isActive() ? 1 : 0);
        db.insertWithOnConflict(DatabaseHelper.TABLE_ARTISTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Artist createArtist(String name, String image, String bio) {
        Artist artist = Artist.builder()
                .id(generateArtistId())
                .name(name)
                .image(image)
                .bio(bio)
                .followersCount(0)
                .active(true)
                .build();
        save(artist);
        return artist;
    }

    public boolean updateArtist(String id, String name, String image, String bio) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ARTIST_NAME, name);
        cv.put(DatabaseHelper.COL_ARTIST_IMAGE, image);
        cv.put(DatabaseHelper.COL_ARTIST_BIO, bio);
        int rows = db.update(DatabaseHelper.TABLE_ARTISTS, cv, DatabaseHelper.COL_ARTIST_ID + "=?", new String[]{id});
        return rows > 0;
    }

    public boolean deleteArtist(String id) {
        int rows = db.delete(DatabaseHelper.TABLE_ARTISTS, DatabaseHelper.COL_ARTIST_ID + "=?", new String[]{id});
        return rows > 0;
    }

    public boolean hideArtist(String id) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ARTIST_ACTIVE, 0);
        int rows = db.update(DatabaseHelper.TABLE_ARTISTS, cv, DatabaseHelper.COL_ARTIST_ID + "=?", new String[]{id});
        return rows > 0;
    }

    public boolean restoreArtist(String id) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ARTIST_ACTIVE, 1);
        int rows = db.update(DatabaseHelper.TABLE_ARTISTS, cv, DatabaseHelper.COL_ARTIST_ID + "=?", new String[]{id});
        return rows > 0;
    }

    public int countAlbumsByArtist(String artistId) {
        return countByColumn(DatabaseHelper.TABLE_ALBUMS, DatabaseHelper.COL_ALBUM_ARTIST_ID, artistId);
    }

    public int countSongsByArtist(String artistId) {
        return countByColumn(DatabaseHelper.TABLE_SONGS, DatabaseHelper.COL_SONG_ARTIST_ID, artistId);
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
        return findAllByActive(true);
    }

    private List<Artist> findAllByActive(boolean active) {
        List<Artist> list = new ArrayList<>();
        Cursor c = db.query(
                DatabaseHelper.TABLE_ARTISTS,
                null,
                DatabaseHelper.COL_ARTIST_ACTIVE + "=?",
                new String[]{active ? "1" : "0"},
                null,
                null,
                null
        );
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    private int countByColumn(String tableName, String columnName, String value) {
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + "=?",
                new String[]{value}
        );
        try {
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private String generateArtistId() {
        Cursor c = db.rawQuery(
                "SELECT " + DatabaseHelper.COL_ARTIST_ID +
                        " FROM " + DatabaseHelper.TABLE_ARTISTS +
                        " WHERE " + DatabaseHelper.COL_ARTIST_ID + " GLOB 'a[0-9]*'" +
                        " ORDER BY CAST(SUBSTR(" + DatabaseHelper.COL_ARTIST_ID + ", 2) AS INTEGER) DESC LIMIT 1",
                null
        );
        try {
            int nextNumber = 1;
            if (c != null && c.moveToFirst()) {
                String lastId = c.getString(0);
                if (lastId != null && lastId.length() > 1) {
                    nextNumber = Integer.parseInt(lastId.substring(1)) + 1;
                }
            }
            return String.format(Locale.US, "a%03d", nextNumber);
        } catch (NumberFormatException e) {
            return "a001";
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private Artist map(Cursor c) {
        return Artist.builder()
                .id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_ID)))
                .name(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_NAME)))
                .image(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_IMAGE)))
                .bio(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_BIO)))
                .followersCount(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_FOLLOWERS)))
                .active(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_ACTIVE)) == 1)
                .build();
    }
}
