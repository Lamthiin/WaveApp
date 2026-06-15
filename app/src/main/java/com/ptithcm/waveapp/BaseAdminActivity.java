package com.ptithcm.waveapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ptithcm.waveapp.auth.LoginActivity;
import com.ptithcm.waveapp.util.TokenManager;

public abstract class BaseAdminActivity extends AppCompatActivity {

    private static final String ADMIN_NAV_PREFS = "admin_nav_prefs";
    private static final String KEY_PENDING_SEARCH_RESET = "pending_search_reset";

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
            markPendingSearchReset(targetActivity);
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
        consumePendingSearchReset();
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

    protected void clearAdminSearchIfPresent() {
        EditText searchInput = findViewById(R.id.etSearchAdmin);
        if (searchInput != null) {
            searchInput.setText("");
            searchInput.clearFocus();
        }

        View clearButton = findViewById(R.id.btnClearSearchAdmin);
        if (clearButton != null) {
            clearButton.setVisibility(View.GONE);
        }
    }

    private void markPendingSearchReset(Class<?> targetActivity) {
        SharedPreferences prefs = getSharedPreferences(ADMIN_NAV_PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_PENDING_SEARCH_RESET, targetActivity.getName()).apply();
    }

    private void consumePendingSearchReset() {
        SharedPreferences prefs = getSharedPreferences(ADMIN_NAV_PREFS, MODE_PRIVATE);
        String pendingClassName = prefs.getString(KEY_PENDING_SEARCH_RESET, null);
        if (pendingClassName == null || !pendingClassName.equals(getClass().getName())) {
            return;
        }

        clearAdminSearchIfPresent();
        prefs.edit().remove(KEY_PENDING_SEARCH_RESET).apply();
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_profile, null, false);
        TextView tvProfileAvatar = dialogView.findViewById(R.id.tvProfileAvatar);
        TextView tvProfileName = dialogView.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = dialogView.findViewById(R.id.tvProfileEmail);
        TextView tvProfileRole = dialogView.findViewById(R.id.tvProfileRole);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseProfileDialog);
        MaterialButton btnLogout = dialogView.findViewById(R.id.btnLogoutProfileDialog);

        tvProfileAvatar.setText(adminName.substring(0, 1).toUpperCase());
        tvProfileName.setText(adminName);
        tvProfileEmail.setText(adminEmail);
        tvProfileRole.setText(adminRole);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            showLogoutConfirmDialog();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void showLogoutConfirmDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_logout_confirm, null, false);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelLogoutDialog);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmLogoutDialog);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performLogout();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
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
