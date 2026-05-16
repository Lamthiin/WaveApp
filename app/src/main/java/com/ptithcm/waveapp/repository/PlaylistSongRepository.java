package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.model.PlaylistSong;
import com.ptithcm.waveapp.model.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaylistSongRepository {

    private final SQLiteDatabase db;

    public PlaylistSongRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public List<PlaylistSong> findByPlaylistIdOrderByPosition(String playlistId) {
        List<PlaylistSong> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null,
                DatabaseHelper.COL_PS_PLAYLIST_ID + "=?", new String[]{playlistId},
                null, null, DatabaseHelper.COL_PS_POSITION + " ASC");
        if (c != null && c.moveToFirst()) { do { list.add(map(c)); } while (c.moveToNext()); c.close(); }
        return list;
    }

    public Optional<PlaylistSong> findByPlaylistIdAndSongId(String playlistId, String songId) {
        Cursor c = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null,
                DatabaseHelper.COL_PS_PLAYLIST_ID + "=? AND " + DatabaseHelper.COL_PS_SONG_ID + "=?",
                new String[]{playlistId, songId}, null, null, null);
        if (c != null && c.moveToFirst()) { PlaylistSong ps = map(c); c.close(); return Optional.of(ps); }
        if (c != null) c.close();
        return Optional.empty();
    }

    public boolean existsByPlaylistIdAndSongId(String playlistId, String songId) {
        Cursor c = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null,
                DatabaseHelper.COL_PS_PLAYLIST_ID + "=? AND " + DatabaseHelper.COL_PS_SONG_ID + "=?",
                new String[]{playlistId, songId}, null, null, null);
        boolean ex = c != null && c.getCount() > 0;
        if (c != null) c.close();
        return ex;
    }

    public int countByPlaylistId(String playlistId) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PLAYLIST_SONGS +
                " WHERE " + DatabaseHelper.COL_PS_PLAYLIST_ID + "=?", new String[]{playlistId});
        int cnt = 0;
        if (c != null && c.moveToFirst()) { cnt = c.getInt(0); c.close(); }
        return cnt;
    }

    public void save(PlaylistSong ps) {
        ContentValues cv = new ContentValues();
        if (ps.getPlaylist() != null) cv.put(DatabaseHelper.COL_PS_PLAYLIST_ID, ps.getPlaylist().getId());
        if (ps.getSong()     != null) cv.put(DatabaseHelper.COL_PS_SONG_ID,     ps.getSong().getId());
        cv.put(DatabaseHelper.COL_PS_POSITION, ps.getPosition());
        db.insertWithOnConflict(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteByPlaylistIdAndSongId(String playlistId, String songId) {
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS,
                DatabaseHelper.COL_PS_PLAYLIST_ID + "=? AND " + DatabaseHelper.COL_PS_SONG_ID + "=?",
                new String[]{playlistId, songId});
    }

    private PlaylistSong map(Cursor c) {
        return PlaylistSong.builder()
                .playlist(Playlist.builder().id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PS_PLAYLIST_ID))).build())
                .song(Song.builder().id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PS_SONG_ID))).build())
                .position(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PS_POSITION)))
                // FIX: addedAt là String trong model, lấy thẳng không parse LocalDateTime
                .addedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PS_ADDED_AT)))
                .build();
    }
}
