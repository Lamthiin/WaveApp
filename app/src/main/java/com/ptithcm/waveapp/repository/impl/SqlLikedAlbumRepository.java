package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.LikedAlbum;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;

import java.util.ArrayList;
import java.util.List;

public class SqlLikedAlbumRepository implements LikedAlbumRepository {
    private final DatabaseHelper dbHelper;

    public SqlLikedAlbumRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean existsByUserIdAndAlbumId(String userId, String albumId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LIKED_ALBUMS, null,
                DatabaseHelper.COL_LA_USER_ID + "=? AND " + DatabaseHelper.COL_LA_ALBUM_ID + "=?",
                new String[]{userId, albumId}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    @Override
    public void deleteByUserIdAndAlbumId(String userId, String albumId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_LIKED_ALBUMS,
                DatabaseHelper.COL_LA_USER_ID + "=? AND " + DatabaseHelper.COL_LA_ALBUM_ID + "=?",
                new String[]{userId, albumId});
    }

    @Override
    public List<LikedAlbum> findByUserIdOrderByAddedAtDesc(String userId) {
        List<LikedAlbum> likedAlbums = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LIKED_ALBUMS, null, DatabaseHelper.COL_LA_USER_ID + "=?",
                new String[]{userId}, null, null, DatabaseHelper.COL_LA_ADDED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                likedAlbums.add(mapCursorToLikedAlbum(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return likedAlbums;
    }

    @Override
    public void save(LikedAlbum likedAlbum) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Sửa lỗi: Lấy ID từ đối tượng User và Album bên trong LikedAlbum
        if (likedAlbum.getUser() != null) {
            values.put(DatabaseHelper.COL_LA_USER_ID, likedAlbum.getUser().getId());
        }
        if (likedAlbum.getAlbum() != null) {
            values.put(DatabaseHelper.COL_LA_ALBUM_ID, likedAlbum.getAlbum().getId());
        }

        // Xử lý thời gian (nếu addedAt là LocalDateTime, hãy dùng toString() hoặc formatter)
        if (likedAlbum.getAddedAt() != null) {
            values.put(DatabaseHelper.COL_LA_ADDED_AT, likedAlbum.getAddedAt().toString());
        }

        db.insertWithOnConflict(DatabaseHelper.TABLE_LIKED_ALBUMS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private LikedAlbum mapCursorToLikedAlbum(Cursor cursor) {
        // Ánh xạ ngược từ Database sang Object
        // Lưu ý: Ở đây ta tạm thời tạo các Object User/Album chỉ có ID
        String userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LA_USER_ID));
        String albumId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LA_ALBUM_ID));
        String addedAtStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LA_ADDED_AT));

        return LikedAlbum.builder()
                .user(User.builder().id(userId).build())
                .album(Album.builder().id(albumId).build())
                // Nếu Model dùng LocalDateTime, bạn cần parse chuỗi addedAtStr tại đây
                .build();
    }
}