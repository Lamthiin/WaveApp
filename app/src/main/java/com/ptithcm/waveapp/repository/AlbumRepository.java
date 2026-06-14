package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// FIX 1: xóa "implements AlbumRepository" – không tự implements chính mình
public class AlbumRepository {

    // FIX 2: mở DB 1 lần trong constructor, dùng writable để đọc + ghi đều được
    private final SQLiteDatabase db;

    public AlbumRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    // FIX 3: xóa @Override vì không còn interface
    public List<Album> findByActiveTrue() { return findAll(); }

    public List<Album> findByArtistIdAndActiveTrue(String artistId) {
        return query("WHERE a." + DatabaseHelper.COL_ALBUM_ARTIST_ID + "=?",
                new String[]{artistId}, null);
    }

    public List<Album> searchByName(String kw) {
        return query("WHERE a." + DatabaseHelper.COL_ALBUM_NAME + " LIKE ?",
                new String[]{"%" + kw + "%"}, null);
    }

    public List<Album> findFeaturedAlbums() {
        return query("", null, "ORDER BY a." + DatabaseHelper.COL_ALBUM_PLAY_COUNT + " DESC LIMIT 10");
    }

    public List<Album> findLikedByUser(String userId) {
        return query("JOIN " + DatabaseHelper.TABLE_LIKED_ALBUMS + " la ON a." +
                        DatabaseHelper.COL_ALBUM_ID + "=la." + DatabaseHelper.COL_LA_ALBUM_ID,
                null,
                "WHERE la." + DatabaseHelper.COL_LA_USER_ID + "='" + userId + "' " +
                        "ORDER BY la." + DatabaseHelper.COL_LA_ADDED_AT + " DESC");
    }

    public Optional<Album> findById(String id) {
        List<Album> list = query("WHERE a." + DatabaseHelper.COL_ALBUM_ID + "=?", new String[]{id}, null);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void save(Album album) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ALBUM_ID, album.getId());
        cv.put(DatabaseHelper.COL_ALBUM_NAME, album.getName());
        if (album.getArtist() != null)
            cv.put(DatabaseHelper.COL_ALBUM_ARTIST_ID, album.getArtist().getId());
        // FIX 4: getImage() không phải getImagePath()
        cv.put(DatabaseHelper.COL_ALBUM_IMAGE, album.getImage());
        db.insertWithOnConflict(DatabaseHelper.TABLE_ALBUMS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteById(String id) {
        db.delete(DatabaseHelper.TABLE_ALBUMS, DatabaseHelper.COL_ALBUM_ID + "=?", new String[]{id});
    }

    private List<Album> findAll() { return query("", null, null); }

    private List<Album> query(String where, String[] args, String extra) {
        // FIX 5: JOIN 1 lần thay vì N+1 query
        String sql = "SELECT a.*, ar." + DatabaseHelper.COL_ARTIST_NAME + " AS artist_name, " +
                "ar." + DatabaseHelper.COL_ARTIST_IMAGE + " AS artist_img " +
                "FROM " + DatabaseHelper.TABLE_ALBUMS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_ARTISTS + " ar ON a." +
                DatabaseHelper.COL_ALBUM_ARTIST_ID + "=ar." + DatabaseHelper.COL_ARTIST_ID +
                " " + where + " " + (extra != null ? extra : "");
        Cursor c = db.rawQuery(sql, args);
        List<Album> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do { list.add(mapCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    private Album mapCursor(Cursor c) {
        Artist artist = null;
        int artistIdCol = c.getColumnIndex(DatabaseHelper.COL_ALBUM_ARTIST_ID);
        if (artistIdCol != -1 && !c.isNull(artistIdCol)) {
            artist = new Artist();
            artist.setId(c.getString(artistIdCol));
            int nameCol = c.getColumnIndex("artist_name");
            if (nameCol != -1) artist.setName(c.getString(nameCol));
            // FIX 4: setImage() không phải setImagePath()
            int imgCol = c.getColumnIndex("artist_img");
            if (imgCol != -1) artist.setImage(c.getString(imgCol));
        }
        Album album = new Album();
        album.setId(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_ID)));
        album.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_NAME)));
        album.setArtist(artist);
        // FIX 4: setImage() không phải setImagePath()
        int imageCol = c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_IMAGE);
        album.setImage(c.isNull(imageCol) ? null : c.getString(imageCol));
        int rdCol = c.getColumnIndex(DatabaseHelper.COL_ALBUM_RELEASE_DATE);
        if (rdCol != -1 && !c.isNull(rdCol)) {
            try { album.setReleaseDate(java.time.LocalDate.parse(c.getString(rdCol))); } catch (Exception ignored) {}
        }
        int pcCol = c.getColumnIndex(DatabaseHelper.COL_ALBUM_PLAY_COUNT);
        if (pcCol != -1 && !c.isNull(pcCol)) {
            album.setPlayCount(c.getLong(pcCol));
        }
        return album;
    }
}
