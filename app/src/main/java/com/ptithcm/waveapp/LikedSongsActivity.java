package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.Collections;
import java.util.List;

public class LikedSongsActivity extends BaseMiniPlayerActivity {

    private UserProfileService userProfileService;
    private TokenManager tokenManager;
    private SongAdapter songAdapter;
    private List<Song> likedSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_songs);

        userProfileService = ServiceLocator.getInstance().getUserProfileService();
        tokenManager = new TokenManager(this);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_liked_songs);
        rv.setLayoutManager(new LinearLayoutManager(this));

        songAdapter = new SongAdapter();
        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });
        rv.setAdapter(songAdapter);

        FloatingActionButton fabPlay = findViewById(R.id.fab_play_liked);
        if (fabPlay != null) {
            fabPlay.setOnClickListener(v -> playAllShuffled());
        }

        loadLikedSongs();
    }

    private void loadLikedSongs() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        new Thread(() -> {
            likedSongs = userProfileService.getLikedSongs(userId);
            runOnUiThread(() -> {
                songAdapter.setSongs(likedSongs);
                TextView tvCount = findViewById(R.id.tvLikedSongsCount);
                if (tvCount != null) {
                    tvCount.setText(likedSongs.size() + " bài hát");
                }
            });
        }).start();
    }

    private void playAllShuffled() {
        if (likedSongs == null || likedSongs.isEmpty()) return;
        List<Song> shuffled = new java.util.ArrayList<>(likedSongs);
        Collections.shuffle(shuffled);
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        intent.putExtra("SONG_DATA", shuffled.get(0));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLikedSongs();
    }
}
