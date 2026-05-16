package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SongRepository {

    private final SQLiteDatabase db;

    // FIX 5: không cần inject 3 repo khác – dùng JOIN thay N+1
    public SongRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public List<Song> findAll() { return query("", null, null); }
    public List<Song> findByActiveTrue() { return findAll(); }

    public List<Song> findByAlbumIdAndActiveTrue(String albumId) {
        return query("WHERE s." + DatabaseHelper.COL_SONG_ALBUM_ID + "=?", new String[]{albumId}, null);
    }

    public List<Song> findByGenreIdAndActiveTrue(String genreId) {
        return query("WHERE s." + DatabaseHelper.COL_SONG_GENRE_ID + "=?", new String[]{genreId}, null);
    }

    public List<Song> findByArtistIdAndActiveTrue(String artistId) {
        return query("WHERE s." + DatabaseHelper.COL_SONG_ARTIST_ID + "=?", new String[]{artistId}, null);
    }

    public List<Song> searchByName(String kw) {
        return query("WHERE s." + DatabaseHelper.COL_SONG_NAME + " LIKE ?",
                new String[]{"%" + kw + "%"}, null);
    }

    public List<Song> findTopByPlayCount(int limit) {
        return query("", null, "ORDER BY s." + DatabaseHelper.COL_SONG_PLAY_COUNT + " DESC LIMIT " + limit);
    }

    public List<Song> findTopByLikeCount(int limit) {
        return query("", null, "ORDER BY s." + DatabaseHelper.COL_SONG_LIKE_COUNT + " DESC LIMIT " + limit);
    }

    public List<Song> findLikedByUser(String userId) {
        String sql = buildSql() +
                " JOIN " + DatabaseHelper.TABLE_LIKED_SONGS + " ls ON s." +
                DatabaseHelper.COL_SONG_ID + "=ls." + DatabaseHelper.COL_LS_SONG_ID +
                " WHERE ls." + DatabaseHelper.COL_LS_USER_ID + "=?" +
                " ORDER BY ls." + DatabaseHelper.COL_LS_LIKED_AT + " DESC";
        return rawList(sql, new String[]{userId});
    }

    public List<Song> findByPlaylistId(String playlistId) {
        String sql = buildSql() +
                " JOIN " + DatabaseHelper.TABLE_PLAYLIST_SONGS + " ps ON s." +
                DatabaseHelper.COL_SONG_ID + "=ps." + DatabaseHelper.COL_PS_SONG_ID +
                " WHERE ps." + DatabaseHelper.COL_PS_PLAYLIST_ID + "=?" +
                " ORDER BY ps." + DatabaseHelper.COL_PS_POSITION + " ASC";
        return rawList(sql, new String[]{playlistId});
    }

    public List<Artist> findArtistsFollowedByUser(String userId) {
        String sql = "SELECT a.* FROM " + DatabaseHelper.TABLE_ARTISTS + " a " +
                " JOIN " + DatabaseHelper.TABLE_FOLLOW_ARTISTS + " fa ON a." + DatabaseHelper.COL_ARTIST_ID + "=fa." + DatabaseHelper.COL_FA_ARTIST_ID +
                " WHERE fa." + DatabaseHelper.COL_FA_USER_ID + "=?" +
                " ORDER BY fa." + DatabaseHelper.COL_FA_FOLLOWED_AT + " DESC";
        Cursor c = db.rawQuery(sql, new String[]{userId});
        List<Artist> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(Artist.builder()
                        .id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_ID)))
                        .name(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_NAME)))
                        .image(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_IMAGE)))
                        .bio(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_BIO)))
                        .followersCount(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ARTIST_FOLLOWERS)))
                        .build());
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public List<Album> findAlbumsLikedByUser(String userId) {
        String sql = "SELECT a.*, ar." + DatabaseHelper.COL_ARTIST_NAME + " AS artist_name " +
                " FROM " + DatabaseHelper.TABLE_ALBUMS + " a " +
                " LEFT JOIN " + DatabaseHelper.TABLE_ARTISTS + " ar ON a." + DatabaseHelper.COL_ALBUM_ARTIST_ID + "=ar." + DatabaseHelper.COL_ARTIST_ID +
                " JOIN " + DatabaseHelper.TABLE_LIKED_ALBUMS + " la ON a." + DatabaseHelper.COL_ALBUM_ID + "=la." + DatabaseHelper.COL_LA_ALBUM_ID +
                " WHERE la." + DatabaseHelper.COL_LA_USER_ID + "=?" +
                " ORDER BY la." + DatabaseHelper.COL_LA_ADDED_AT + " DESC";
        Cursor c = db.rawQuery(sql, new String[]{userId});
        List<Album> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                Artist artist = null;
                int artistIdCol = c.getColumnIndex(DatabaseHelper.COL_ALBUM_ARTIST_ID);
                if (artistIdCol != -1 && !c.isNull(artistIdCol)) {
                    artist = new Artist();
                    artist.setId(c.getString(artistIdCol));
                    int nameCol = c.getColumnIndex("artist_name");
                    if (nameCol != -1) artist.setName(c.getString(nameCol));
                }
                Album album = new Album();
                album.setId(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_ID)));
                album.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_NAME)));
                album.setImage(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_IMAGE)));
                album.setArtist(artist);
                list.add(album);
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public Optional<Song> findById(String id) {
        List<Song> list = query("WHERE s." + DatabaseHelper.COL_SONG_ID + "=?", new String[]{id}, null);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void save(Song song) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_SONG_ID, song.getId());
        cv.put(DatabaseHelper.COL_SONG_NAME, song.getName());
        cv.put(DatabaseHelper.COL_SONG_URL, song.getUrl());
        cv.put(DatabaseHelper.COL_SONG_IMAGE, song.getImage());
        cv.put(DatabaseHelper.COL_SONG_DURATION, song.getDuration());
        cv.put(DatabaseHelper.COL_SONG_LYRICS, song.getLyrics());
        if (song.getArtist() != null) cv.put(DatabaseHelper.COL_SONG_ARTIST_ID, song.getArtist().getId());
        if (song.getAlbum()  != null) cv.put(DatabaseHelper.COL_SONG_ALBUM_ID,  song.getAlbum().getId());
        if (song.getGenre()  != null) cv.put(DatabaseHelper.COL_SONG_GENRE_ID,  song.getGenre().getId());
        db.insertWithOnConflict(DatabaseHelper.TABLE_SONGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void incrementPlayCount(String id) {
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_SONGS + " SET " +
                DatabaseHelper.COL_SONG_PLAY_COUNT + "=" + DatabaseHelper.COL_SONG_PLAY_COUNT +
                "+1 WHERE " + DatabaseHelper.COL_SONG_ID + "=?", new String[]{id});
    }

    public void incrementLikeCount(String id) {
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_SONGS + " SET " +
                DatabaseHelper.COL_SONG_LIKE_COUNT + "=" + DatabaseHelper.COL_SONG_LIKE_COUNT +
                "+1 WHERE " + DatabaseHelper.COL_SONG_ID + "=?", new String[]{id});
    }

    public void decrementLikeCount(String id) {
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_SONGS + " SET " +
                DatabaseHelper.COL_SONG_LIKE_COUNT + "=MAX(0," + DatabaseHelper.COL_SONG_LIKE_COUNT +
                "-1) WHERE " + DatabaseHelper.COL_SONG_ID + "=?", new String[]{id});
    }

    public void deleteById(String id) {
        db.delete(DatabaseHelper.TABLE_SONGS, DatabaseHelper.COL_SONG_ID + "=?", new String[]{id});
    }

    // ── private ──────────────────────────────────────────────────
    private String buildSql() {
        return "SELECT s.*, " +
                "ar." + DatabaseHelper.COL_ARTIST_NAME  + " AS artist_name," +
                "ar." + DatabaseHelper.COL_ARTIST_IMAGE + " AS artist_img," +
                "al." + DatabaseHelper.COL_ALBUM_NAME   + " AS album_name," +
                "al." + DatabaseHelper.COL_ALBUM_IMAGE  + " AS album_img," +
                "g."  + DatabaseHelper.COL_GENRE_NAME   + " AS genre_name " +
                "FROM " + DatabaseHelper.TABLE_SONGS + " s " +
                "LEFT JOIN " + DatabaseHelper.TABLE_ARTISTS + " ar ON s." + DatabaseHelper.COL_SONG_ARTIST_ID + "=ar." + DatabaseHelper.COL_ARTIST_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_ALBUMS  + " al ON s." + DatabaseHelper.COL_SONG_ALBUM_ID  + "=al." + DatabaseHelper.COL_ALBUM_ID  + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_GENRES  + " g  ON s." + DatabaseHelper.COL_SONG_GENRE_ID  + "=g."  + DatabaseHelper.COL_GENRE_ID;
    }

    private List<Song> query(String where, String[] args, String extra) {
        String sql = buildSql() + " " + where + " " + (extra != null ? extra : "");
        return rawList(sql, args);
    }

    private List<Song> rawList(String sql, String[] args) {
        Cursor c = db.rawQuery(sql, args);
        List<Song> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    private Song map(Cursor c) {
        Artist artist = null;
        int artIdCol = c.getColumnIndex(DatabaseHelper.COL_SONG_ARTIST_ID);
        if (artIdCol != -1 && !c.isNull(artIdCol)) {
            artist = new Artist();
            artist.setId(c.getString(artIdCol));
            int n = c.getColumnIndex("artist_name"); if (n != -1) artist.setName(c.getString(n));
            int i = c.getColumnIndex("artist_img");  if (i != -1) artist.setImage(c.getString(i));
        }
        Album album = null;
        int albIdCol = c.getColumnIndex(DatabaseHelper.COL_SONG_ALBUM_ID);
        if (albIdCol != -1 && !c.isNull(albIdCol)) {
            album = new Album();
            album.setId(c.getString(albIdCol));
            int n = c.getColumnIndex("album_name"); if (n != -1) album.setName(c.getString(n));
            int i = c.getColumnIndex("album_img");  if (i != -1) album.setImage(c.getString(i));
        }
        Genre genre = null;
        int genIdCol = c.getColumnIndex(DatabaseHelper.COL_SONG_GENRE_ID);
        if (genIdCol != -1 && !c.isNull(genIdCol)) {
            genre = new Genre();
            genre.setId(c.getString(genIdCol));
            int n = c.getColumnIndex("genre_name"); if (n != -1) genre.setName(c.getString(n));
        }
        Song s = new Song();
        s.setId(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_ID)));
        s.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_NAME)));
        s.setUrl(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_URL)));
        s.setImage(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_IMAGE)));
        s.setDuration(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_DURATION)));
        s.setLyrics(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_LYRICS)));
        s.setPlayCount(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_PLAY_COUNT)));
        s.setLikeCount(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_LIKE_COUNT)));
        s.setArtist(artist);
        s.setAlbum(album);
        s.setGenre(genre);
        return s;
    }
}
