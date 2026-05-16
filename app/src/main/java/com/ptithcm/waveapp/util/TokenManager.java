package com.ptithcm.waveapp.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lưu thông tin user đang đăng nhập vào SharedPreferences.
 * Đặt file này tại:
 *   app/src/main/java/com/ptithcm/waveapp/util/TokenManager.java
 */
public class TokenManager {

    private static final String PREF_NAME    = "wave_prefs";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAME     = "name";
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_AVATAR   = "avatar";
    private static final String KEY_ROLE     = "role";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Lưu thông tin sau khi đăng nhập thành công */
    public void saveLogin(String userId, String username, String name,
                          String email, String avatar, String role) {
        prefs.edit()
                .putString(KEY_USER_ID,  userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_NAME,     name)
                .putString(KEY_EMAIL,    email)
                .putString(KEY_AVATAR,   avatar)
                .putString(KEY_ROLE,     role)
                .apply();
    }

    public String  getUserId()   { return prefs.getString(KEY_USER_ID,  null); }
    public String  getUsername() { return prefs.getString(KEY_USERNAME, null); }
    public String  getName()     { return prefs.getString(KEY_NAME,     null); }
    public String  getEmail()    { return prefs.getString(KEY_EMAIL,    null); }
    public String  getAvatar()   { return prefs.getString(KEY_AVATAR,   null); }
    public String  getRole()     { return prefs.getString(KEY_ROLE,     null); }
    public boolean isLoggedIn()  { return getUserId() != null; }

    /** Gọi khi đăng xuất */
    public void logout() {
        prefs.edit().clear().apply();
    }
}