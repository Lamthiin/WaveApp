package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.repository.AlbumRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlAlbumRepository implements AlbumRepository {

    // Giu 1 instance DB, khong mo lai moi method
    private final SQLiteDatabase db;

    public SqlAlbumRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getReadableDatabase();
    }

    @Override public List<Album> findByActiveTrue()           { return findAll(); }

    @Override
    public List<Album> findByArtistIdAndActiveTrue(String artistId) {
        return query("WHERE a." + DatabaseHelper.COL_ALBUM_ARTIST_ID + "=?",
                new String[]{artistId}, null);
    }

    @Override
    public List<Album> searchByName(String kw) {
        return query("WHERE a." + DatabaseHelper.COL_ALBUM_NAME + " LIKE ?",
                new String[]{"%" + kw + "%"}, null);
    }

    @Override
    public List<Album> findFeaturedAlbums() {
        return query("", null,
                "ORDER BY a." + DatabaseHelper.COL_ALBUM_PLAY_COUNT + " DESC LIMIT 10");
    }

    @Override
    public Optional<Album> findById(String id) {
        List<Album> list = query(
                "WHERE a." + DatabaseHelper.COL_ALBUM_ID + "=?",
                new String[]{id}, null);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public void save(Album album) {
        SQLiteDatabase wdb = DatabaseHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ALBUM_ID,   album.getId());
        cv.put(DatabaseHelper.COL_ALBUM_NAME,  album.getName());
        if (album.getArtist() != null)
            cv.put(DatabaseHelper.COL_ALBUM_ARTIST_ID, album.getArtist().getId());

        // image la TEXT: luu path file (vi du: "images/albums/al001.jpg")
        // Truoc khi goi save(), da goi ImageFileHelper.saveImageFromUri() de luu file
        // va lay duoc path. Roi set vao album.setImagePath(path)
        cv.put(DatabaseHelper.COL_ALBUM_IMAGE, album.getImagePath());

        wdb.insertWithOnConflict(DatabaseHelper.TABLE_ALBUMS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    // ── Dung chung 1 ham query voi JOIN ──────────────────
    private List<Album> findAll() {
        return query("", null, null);
    }

    private List<Album> query(String where, String[] args, String extra) {
        String sql =
                "SELECT a.*," +
                        "       ar." + DatabaseHelper.COL_ARTIST_NAME  + " AS artist_name," +
                        "       ar." + DatabaseHelper.COL_ARTIST_IMAGE + " AS artist_image_path " +
                        "FROM "  + DatabaseHelper.TABLE_ALBUMS  + " a " +
                        "LEFT JOIN " + DatabaseHelper.TABLE_ARTISTS + " ar " +
                        "   ON a." + DatabaseHelper.COL_ALBUM_ARTIST_ID + " = ar." + DatabaseHelper.COL_ARTIST_ID + " " +
                        where + " " + (extra != null ? extra : "");

        Cursor c = db.rawQuery(sql, args);
        List<Album> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do { list.add(mapCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    // ── Map Cursor -> Album ───────────────────────────────
    // image la TEXT: doc getString(), khong phai getBlob()
    private Album mapCursor(Cursor c) {
        Artist artist = null;
        int artistIdCol = c.getColumnIndex(DatabaseHelper.COL_ALBUM_ARTIST_ID);
        if (artistIdCol != -1 && !c.isNull(artistIdCol)) {
            artist = new Artist();
            artist.setId(c.getString(artistIdCol));

            int nameCol = c.getColumnIndex("artist_name");
            if (nameCol != -1) artist.setName(c.getString(nameCol));

            // Anh nghe si: doc path, hien thi bang ImageFileHelper.loadIntoImageView()
            int imgCol = c.getColumnIndex("artist_image_path");
            if (imgCol != -1) artist.setImagePath(c.getString(imgCol));
        }

        Album album = new Album();
        album.setId(         c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_ID)));
        album.setName(       c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_NAME)));
        album.setArtist(     artist);
        album.setReleaseDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_RELEASE_DATE)));
        album.setPlayCount(  c.getLong(  c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_PLAY_COUNT)));

        // Doc path file anh (TEXT) tu SQLite
        // De hien thi: ImageFileHelper.loadIntoImageView(context, album.getImagePath(), imageView, R.drawable.ic_album)
        int imageCol = c.getColumnIndexOrThrow(DatabaseHelper.COL_ALBUM_IMAGE);
        album.setImagePath(c.isNull(imageCol) ? null : c.getString(imageCol));

        return album;
    }
}