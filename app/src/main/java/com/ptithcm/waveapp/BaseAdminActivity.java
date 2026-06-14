package com.ptithcm.waveapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ptithcm.waveapp.auth.LoginActivity;
import com.ptithcm.waveapp.util.TokenManager;

public abstract class BaseAdminActivity extends AppCompatActivity {

    protected TokenManager tokenManager;
    protected String adminName;
    protected String adminEmail;
    protected String adminRole;
    private BottomNavigationView bottomNavigationView;
    private int selectedNavId;
    private boolean syncingNavigationState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(this);
    }

    protected void setupAdminChrome(@IdRes int titleViewId, @IdRes int avatarViewId, @IdRes int navViewId,
                                    @IdRes int selectedNavId, String title) {
        TextView titleView = findViewById(titleViewId);
        TextView avatarView = findViewById(avatarViewId);
        bottomNavigationView = findViewById(navViewId);
        this.selectedNavId = selectedNavId;

        adminName = firstNonEmpty(tokenManager.getName(), tokenManager.getUsername(), "Admin");
        adminEmail = firstNonEmpty(tokenManager.getEmail(), "Chưa có email");
        adminRole = firstNonEmpty(tokenManager.getRole(), "ADMIN").toUpperCase();

        titleView.setText(title);
        avatarView.setText(adminName.substring(0, 1).toUpperCase());
        avatarView.setOnClickListener(v -> showAdminProfileDialog());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (syncingNavigationState) {
                return true;
            }

            int itemId = item.getItemId();
            if (itemId == selectedNavId) {
                return true;
            }

            Class<?> targetActivity = getAdminTargetActivity(itemId);
            if (targetActivity == null) {
                return false;
            }

            clearFocusAndHideKeyboard();
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        });

        syncBottomNavigationState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncBottomNavigationState();
    }

    private void syncBottomNavigationState() {
        if (bottomNavigationView == null || selectedNavId == 0) {
            return;
        }

        syncingNavigationState = true;
        bottomNavigationView.setSelectedItemId(selectedNavId);
        syncingNavigationState = false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocus = getCurrentFocus();
            if (currentFocus instanceof EditText) {
                Rect outRect = new Rect();
                currentFocus.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    clearFocusAndHideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    protected void clearFocusAndHideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    private Class<?> getAdminTargetActivity(int itemId) {
        if (itemId == R.id.nav_admin_dashboard) return AdminDashboardActivity.class;
        if (itemId == R.id.nav_admin_users) return AdminUserManagementActivity.class;
        if (itemId == R.id.nav_admin_artists) return AdminArtistManagementActivity.class;
        if (itemId == R.id.nav_admin_songs) return AdminSongManagementActivity.class;
        if (itemId == R.id.nav_admin_albums) return AdminAlbumManagementActivity.class;
        return null;
    }

    private void showAdminProfileDialog() {
        String message = adminEmail + "\nQuyền: " + adminRole;

        new AlertDialog.Builder(this)
                .setTitle(adminName)
                .setMessage(message)
                .setPositiveButton("Đăng xuất", (dialog, which) -> showLogoutConfirmDialog())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản admin không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performLogout() {
        tokenManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
