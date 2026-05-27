package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.adapter.AlbumAdapter;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAlbumsActivity extends BaseMiniPlayerActivity {

    private UserProfileService userProfileService;
    private TokenManager tokenManager;
    private AlbumAdapter albumAdapter;
    private List<Album> allAlbums = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_albums);

        userProfileService = ServiceLocator.getInstance().getUserProfileService();
        tokenManager = new TokenManager(this);

        ImageButton btnBack = findViewById(R.id.btn_back_albums);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_favorite_albums);
        rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

        albumAdapter = new AlbumAdapter();
        albumAdapter.setOnAlbumClickListener(album -> {
            Intent intent = new Intent(this, PlaylistDetailActivity.class);
            intent.putExtra("ALBUM_ID", album.getId());
            startActivity(intent);
        });
        rv.setAdapter(albumAdapter);

        EditText etSearch = findViewById(R.id.et_search_albums);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { filterAlbums(s.toString()); }
            });
        }

        loadLikedAlbums();
    }

    private void loadLikedAlbums() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        new Thread(() -> {
            allAlbums = userProfileService.getLikedAlbums(userId);
            runOnUiThread(() -> albumAdapter.setAlbums(allAlbums));
        }).start();
    }

    private void filterAlbums(String query) {
        if (query.trim().isEmpty()) {
            albumAdapter.setAlbums(allAlbums);
            return;
        }
        List<Album> filtered = new ArrayList<>();
        for (Album a : allAlbums) {
            if (a.getName() != null && a.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(a);
            }
        }
        albumAdapter.setAlbums(filtered);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLikedAlbums();
    }
}
