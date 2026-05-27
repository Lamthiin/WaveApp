package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.adapter.ArtistAdapter;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class FavoriteArtistsActivity extends BaseMiniPlayerActivity {

    private UserProfileService userProfileService;
    private TokenManager tokenManager;
    private ArtistAdapter artistAdapter;
    private List<Artist> allArtists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_artists);

        userProfileService = ServiceLocator.getInstance().getUserProfileService();
        tokenManager = new TokenManager(this);

        ImageButton btnBack = findViewById(R.id.btn_back_artists);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_favorite_artists);
        rv.setLayoutManager(new LinearLayoutManager(this));

        artistAdapter = new ArtistAdapter();
        artistAdapter.setOnArtistClickListener(artist -> {
            Intent intent = new Intent(this, ArtistDetailActivity.class);
            intent.putExtra("ARTIST_ID", artist.getId());
            startActivity(intent);
        });
        rv.setAdapter(artistAdapter);

        EditText etSearch = findViewById(R.id.et_search_artists);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { filterArtists(s.toString()); }
            });
        }

        loadFollowingArtists();
    }

    private void loadFollowingArtists() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        new Thread(() -> {
            allArtists = userProfileService.getFollowingArtists(userId);
            runOnUiThread(() -> artistAdapter.setArtists(allArtists));
        }).start();
    }

    private void filterArtists(String query) {
        if (query.trim().isEmpty()) {
            artistAdapter.setArtists(allArtists);
            return;
        }
        List<Artist> filtered = new ArrayList<>();
        for (Artist a : allArtists) {
            if (a.getName() != null && a.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(a);
            }
        }
        artistAdapter.setArtists(filtered);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFollowingArtists();
    }
}
