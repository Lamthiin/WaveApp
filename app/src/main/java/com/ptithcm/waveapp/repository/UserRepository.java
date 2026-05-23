package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Optional;

public class UserRepository {

    private final SQLiteDatabase db;

    public UserRepository(DatabaseHelper dbHelper) {
        this.db = dbHelper.getWritableDatabase();
    }

    public Optional<User> findByEmail(String email) {
        return query(DatabaseHelper.COL_USER_EMAIL + "=?", new String[]{email});
    }

    public Optional<User> findByUsername(String username) {
        return query(DatabaseHelper.COL_USER_USERNAME + "=?", new String[]{username});
    }

    public Optional<User> findById(String id) {
        return query(DatabaseHelper.COL_USER_ID + "=?", new String[]{id});
    }

    public Optional<User> findByIdentifier(String identifier) {
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                        " WHERE " + DatabaseHelper.COL_USER_EMAIL + "=? OR " +
                        DatabaseHelper.COL_USER_USERNAME + "=?",
                new String[]{identifier, identifier}
        );

        if (c != null && c.moveToFirst()) {
            User u = map(c);
            c.close();
            return Optional.of(u);
        }

        if (c != null) c.close();
        return Optional.empty();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public void save(User user) {
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.COL_USER_ID, user.getId());
        cv.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername());
        cv.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail());
        cv.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        cv.put(DatabaseHelper.COL_USER_NAME, user.getName());
        cv.put(DatabaseHelper.COL_USER_AVATAR, user.getAvatar());
        cv.put(DatabaseHelper.COL_USER_ROLE, user.getRole());
        cv.put(DatabaseHelper.COL_USER_VERIFIED, user.isVerified() ? 1 : 0);
        cv.put(DatabaseHelper.COL_USER_UPDATED_AT, LocalDateTime.now().toString());

        db.insertWithOnConflict(
                DatabaseHelper.TABLE_USERS,
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public void updateAvatar(String userId, String imagePath) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_AVATAR, imagePath);
        cv.put(DatabaseHelper.COL_USER_UPDATED_AT, LocalDateTime.now().toString());

        db.update(
                DatabaseHelper.TABLE_USERS,
                cv,
                DatabaseHelper.COL_USER_ID + "=?",
                new String[]{userId}
        );
    }

    public void updatePassword(String email, String rawPassword) {
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_PASSWORD, hashedPassword);
        cv.put(DatabaseHelper.COL_USER_UPDATED_AT, LocalDateTime.now().toString());

        db.update(
                DatabaseHelper.TABLE_USERS,
                cv,
                DatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{email}
        );
    }

    public void saveOrUpdateGoogleUser(String id, String username, String email, String name, String avatar) {
        User user = User.builder()
                .id(id)
                .username(username)
                .email(email)
                .name(name)
                .avatar(avatar)
                .password("")
                .role("USER")
                .verified(true)
                .build();

        save(user);
    }

    private Optional<User> query(String where, String[] args) {
        Cursor c = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                where,
                args,
                null,
                null,
                null
        );

        if (c != null && c.moveToFirst()) {
            User u = map(c);
            c.close();
            return Optional.of(u);
        }

        if (c != null) c.close();
        return Optional.empty();
    }

    private User map(Cursor c) {
        return User.builder()
                .id(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)))
                .username(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)))
                .email(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)))
                .password(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)))
                .name(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)))
                .avatar(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)))
                .role(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE)))
                .verified(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_VERIFIED)) == 1)
                .createdAt(parseDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_CREATED_AT))))
                .updatedAt(parseDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_UPDATED_AT))))
                .build();
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(value.replace(" ", "T"));
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}