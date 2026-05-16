package com.ptithcm.waveapp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

// FIX 1: "extends UserRepository" → không có gì (xóa hẳn)
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

    /** Tìm theo email HOẶC username (dùng cho màn login) */
    public Optional<User> findByIdentifier(String identifier) {
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.COL_USER_EMAIL + "=? OR " +
                DatabaseHelper.COL_USER_USERNAME + "=?",
                new String[]{identifier, identifier});
        if (c != null && c.moveToFirst()) { User u = map(c); c.close(); return Optional.of(u); }
        if (c != null) c.close();
        return Optional.empty();
    }

    public boolean existsByEmail(String email)       { return findByEmail(email).isPresent(); }
    public boolean existsByUsername(String username) { return findByUsername(username).isPresent(); }

    public void save(User user) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_ID,       user.getId());
        cv.put(DatabaseHelper.COL_USER_USERNAME,  user.getUsername());
        cv.put(DatabaseHelper.COL_USER_EMAIL,     user.getEmail());
        cv.put(DatabaseHelper.COL_USER_PASSWORD,  user.getPassword());
        cv.put(DatabaseHelper.COL_USER_NAME,      user.getName());
        cv.put(DatabaseHelper.COL_USER_AVATAR,    user.getAvatar());
        // FIX 6: role là String, không phải enum nữa → dùng getRole() trực tiếp
        cv.put(DatabaseHelper.COL_USER_ROLE,      user.getRole());
        cv.put(DatabaseHelper.COL_USER_VERIFIED,  user.isVerified() ? 1 : 0);
        db.insertWithOnConflict(DatabaseHelper.TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateAvatar(String userId, String imagePath) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_AVATAR, imagePath);
        db.update(DatabaseHelper.TABLE_USERS, cv, DatabaseHelper.COL_USER_ID + "=?", new String[]{userId});
    }

    private Optional<User> query(String where, String[] args) {
        Cursor c = db.query(DatabaseHelper.TABLE_USERS, null, where, args, null, null, null);
        if (c != null && c.moveToFirst()) { User u = map(c); c.close(); return Optional.of(u); }
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
                // FIX 6: role là String → set thẳng String
                .role(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE)))
                .verified(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_VERIFIED)) == 1)
                .build();
    }

    /** Xử lý lưu hoặc cập nhật user từ Google */
    public void saveOrUpdateGoogleUser(String id, String username, String email, String name, String avatar) {
        User user = User.builder()
                .id(id)
                .username(username)
                .email(email)
                .name(name)
                .avatar(avatar)
                .password("") // Google login không dùng password
                .role("USER")
                .verified(true)
                .build();
        save(user);
    }

    public void updatePassword(String email, String rawPassword) {
        // Mã hóa mật khẩu trước khi lưu
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_PASSWORD, hashedPassword);

        String whereClause = DatabaseHelper.COL_USER_EMAIL + "=?";
        String[] whereArgs = new String[]{email};

        db.update(DatabaseHelper.TABLE_USERS, cv, whereClause, whereArgs);
    }
}
