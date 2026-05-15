package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.GenreRepository;
import com.ptithcm.waveapp.repository.SongRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlSongRepository implements SongRepository {
    private final DatabaseHelper dbHelper;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;

    public SqlSongRepository(DatabaseHelper dbHelper, ArtistRepository artistRepository, AlbumRepository albumRepository, GenreRepository genreRepository) {
        this.dbHelper = dbHelper;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    public List<Song> findAll() {
        List<Song> songs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SONGS, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(mapCursorToSong(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return songs;
    }

    @Override
    public Optional<Song> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SONGS, null, DatabaseHelper.COL_SONG_ID + "=?",
                new String[]{id}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Song song = mapCursorToSong(cursor);
            cursor.close();
            return Optional.of(song);
        }
        if (cursor != null) cursor.close();
        return Optional.empty();
    }

    @Override
    public void save(Song song) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SONG_ID, song.getId());
        values.put(DatabaseHelper.COL_SONG_NAME, song.getName());
        values.put(DatabaseHelper.COL_SONG_URL, song.getUrl());
        values.put(DatabaseHelper.COL_SONG_IMAGE, song.getImage());
        values.put(DatabaseHelper.COL_SONG_DURATION, song.getDuration());
        if (song.getArtist() != null) values.put(DatabaseHelper.COL_SONG_ARTIST_ID, song.getArtist().getId());
        if (song.getAlbum() != null) values.put(DatabaseHelper.COL_SONG_ALBUM_ID, song.getAlbum().getId());
        if (song.getGenre() != null) values.put(DatabaseHelper.COL_SONG_GENRE_ID, song.getGenre().getId());
        values.put(DatabaseHelper.COL_SONG_LYRICS, song.getLyrics());

        db.insertWithOnConflict(DatabaseHelper.TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_SONGS, DatabaseHelper.COL_SONG_ID + "=?", new String[]{id});
    }

    @Override
    public List<Song> findByActiveTrue() {
        return findAll();
    }

    @Override
    public List<Song> findByAlbumIdAndActiveTrue(String albumId) {
        List<Song> songs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SONGS, null, DatabaseHelper.COL_SONG_ALBUM_ID + "=?",
                new String[]{albumId}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(mapCursorToSong(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return songs;
    }

    @Override
    public List<Song> searchByName(String kw) {
        List<Song> songs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SONGS, null, DatabaseHelper.COL_SONG_NAME + " LIKE ?",
                new String[]{"%" + kw + "%"}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(mapCursorToSong(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return songs;
    }

    @Override
    public void incrementPlayCount(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_SONGS + " SET " + DatabaseHelper.COL_SONG_PLAY_COUNT + " = " + DatabaseHelper.COL_SONG_PLAY_COUNT + " + 1 WHERE " + DatabaseHelper.COL_SONG_ID + " = ?", new String[]{id});
    }

    private Song mapCursorToSong(Cursor cursor) {
        String artistId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_ARTIST_ID));
        String albumId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_ALBUM_ID));
        String genreId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_GENRE_ID));

        return Song.builder()
                .id(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_ID)))
                .name(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_NAME)))
                .url(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_URL)))
                .image(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_IMAGE)))
                .duration(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_DURATION)))
                .lyrics(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_LYRICS)))
                .artist(artistRepository.findById(artistId).orElse(null))
                .album(albumRepository.findById(albumId).orElse(null))
                .genre(genreRepository.findById(genreId).orElse(null))
                .build();
    }
}
