package com.ptithcm.waveapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.dto.response.SongResponse;

public class MusicPlayerActivity extends AppCompatActivity {

    private ImageView ivAlbumArt;
    private TextView tvSongTitle, tvArtistName, tvCurrentTime, tvTotalTime;
    private ImageButton btnPlay, btnPrevious, btnNext, btnBack;
    private SeekBar seekBar;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_song);

        initViews();
        handleIntent();
        setupListeners();
    }

    private void initViews() {
        ivAlbumArt = findViewById(R.id.ivAlbumArt);
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnPlay = findViewById(R.id.btnPlay);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        seekBar = findViewById(R.id.seekBar);
    }

    private void handleIntent() {
        SongResponse song = (SongResponse) getIntent().getSerializableExtra("SONG_DATA");
        if (song != null) {
            tvSongTitle.setText(song.getName());
            tvArtistName.setText(song.getArtistName());
            tvTotalTime.setText(formatDuration(song.getDuration()));
            Glide.with(this).load(song.getImage()).placeholder(R.drawable.sontung).into(ivAlbumArt);
            
            // Auto play (mock)
            isPlaying = true;
            btnPlay.setImageResource(R.drawable.playmusic); // Should be pause icon ideally
        }
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnPlay.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                // mock resume
            } else {
                // mock pause
            }
        });
    }
}
