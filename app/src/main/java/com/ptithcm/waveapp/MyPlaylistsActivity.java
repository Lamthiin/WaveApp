package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ptithcm.waveapp.adapter.PlaylistAdapter;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.util.TokenManager;
import java.util.List;

public class MyPlaylistsActivity extends BaseMiniPlayerActivity {

    private PlaylistService playlistService;
    private TokenManager tokenManager;
    private PlaylistAdapter playlistAdapter;
    private RecyclerView rvPlaylists;
    private View layoutEmpty;
    private android.widget.TextView tvPlaylistCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_playlists);

        playlistService = ServiceLocator.getInstance().getPlaylistService();
        tokenManager = new TokenManager(this);

        rvPlaylists = findViewById(R.id.rv_playlists);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvPlaylistCount = findViewById(R.id.tv_playlist_count);
        
        rvPlaylists.setLayoutManager(new LinearLayoutManager(this));
        playlistAdapter = new PlaylistAdapter();
        rvPlaylists.setAdapter(playlistAdapter);

        loadPlaylists();

        playlistAdapter.setOnPlaylistClickListener(playlist -> {
            Intent intent = new Intent(this, PlaylistDetailActivity.class);
            intent.putExtra("PLAYLIST_ID", playlist.getId());
            startActivity(intent);
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_create_playlist).setOnClickListener(v -> showCreatePlaylistDialog());

        findViewById(R.id.btn_create_first_playlist).setOnClickListener(v -> showCreatePlaylistDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylists();
    }

    private void showCreatePlaylistDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_edit_playlist, null, false);
        TextInputLayout tilPlaylistName = dialogView.findViewById(R.id.til_playlist_name);
        TextInputEditText etPlaylistName = dialogView.findViewById(R.id.et_playlist_name);
        View btnCancel = dialogView.findViewById(R.id.btn_cancel);
        View btnSave = dialogView.findViewById(R.id.btn_save);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        dialog.setOnShowListener(unused -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etPlaylistName.getText() != null
                    ? etPlaylistName.getText().toString().trim()
                    : "";

            if (name.isEmpty()) {
                tilPlaylistName.setError("Tên playlist không được để trống");
                return;
            }

            tilPlaylistName.setError(null);
            dialog.dismiss();
            createNewPlaylist(name);
        });

        if (etPlaylistName != null) {
            etPlaylistName.requestFocus();
            etPlaylistName.setOnEditorActionListener((v, actionId, event) -> {
                btnSave.performClick();
                return true;
            });
        }

        dialog.show();
    }

    private void createNewPlaylist(String name) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        try {
            playlistService.createPlaylist(userId, name, null);
            Toast.makeText(this, "Đã tạo playlist", Toast.LENGTH_SHORT).show();
            loadPlaylists();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPlaylists() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        List<Playlist> playlists = playlistService.getMyPlaylists(userId);
        playlistAdapter.setPlaylists(playlists);
        if (tvPlaylistCount != null) {
            int count = playlists.size();
            tvPlaylistCount.setText(count == 1 ? "1 playlist" : count + " playlist");
        }

        boolean isEmpty = playlists.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvPlaylists.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
