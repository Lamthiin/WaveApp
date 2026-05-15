package com.ptithcm.waveapp.repository.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;

import java.util.Optional;

public class SqlUserRepository implements UserRepository {
    private final DatabaseHelper dbHelper;

    public SqlUserRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{email}, null, null, null);
        return mapCursorToUser(cursor);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.COL_USER_USERNAME + "=?",
                new String[]{username}, null, null, null);
        return mapCursorToUser(cursor);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        // SQLite table currently doesn't have phone, would need to add if needed
        return Optional.empty();
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByPhone(String phone) {
        return false;
    }

    @Override
    public void save(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_ID, user.getId());
        values.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COL_USER_NAME, user.getName());
        values.put(DatabaseHelper.COL_USER_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COL_USER_ROLE, user.getRole().name());
        values.put(DatabaseHelper.COL_USER_VERIFIED, user.isVerified() ? 1 : 0);

        db.insertWithOnConflict(DatabaseHelper.TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public Optional<User> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.COL_USER_ID + "=?",
                new String[]{id}, null, null, null);
        return mapCursorToUser(cursor);
    }

    private Optional<User> mapCursorToUser(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            User user = User.builder()
                    .id(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)))
                    .username(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)))
                    .email(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)))
                    .password(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)))
                    .name(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)))
                    .avatar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)))
                    .role(User.Role.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE))))
                    .verified(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_VERIFIED)) == 1)
                    .build();
            cursor.close();
            return Optional.of(user);
        }
        if (cursor != null) cursor.close();
        return Optional.empty();
    }
}
