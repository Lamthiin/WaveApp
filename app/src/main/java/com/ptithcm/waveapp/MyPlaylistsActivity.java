package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ptithcm.waveapp.adapter.PlaylistAdapter;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.util.TokenManager;
import java.util.List;

public class MyPlaylistsActivity extends AppCompatActivity {

    private PlaylistService playlistService;
    private TokenManager tokenManager;
    private PlaylistAdapter playlistAdapter;
    private RecyclerView rvPlaylists;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_playlists);

        playlistService = ServiceLocator.getInstance().getPlaylistService();
        tokenManager = new TokenManager(this);

        rvPlaylists = findViewById(R.id.rv_playlists);
        layoutEmpty = findViewById(R.id.layout_empty);
        
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

    private void showCreatePlaylistDialog() {
        EditText etName = new EditText(this);
        etName.setHint("Tên playlist");

        new AlertDialog.Builder(this)
                .setTitle("Tạo playlist mới")
                .setView(etName)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createNewPlaylist(name);
                    } else {
                        Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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

        boolean isEmpty = playlists.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvPlaylists.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
