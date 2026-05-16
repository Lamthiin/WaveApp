package com.ptithcm.waveapp;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.io.IOException;

public class MusicPlayerActivity extends AppCompatActivity {

    private static final String TAG = "MusicPlayerActivity";

    private ImageView ivAlbumArt;
    private TextView tvSongTitle, tvArtistName, tvCurrentTime, tvTotalTime;
    private ImageButton btnPlay, btnPrevious, btnNext, btnBack;
    private SeekBar seekBar;
    
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private Song currentSong;

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
        currentSong = (Song) getIntent().getSerializableExtra("SONG_DATA");
        if (currentSong != null) {
            tvSongTitle.setText(currentSong.getName());
            if (currentSong.getArtist() != null) {
                tvArtistName.setText(currentSong.getArtist().getName());
            }
            tvTotalTime.setText(formatDuration(currentSong.getDuration()));
            
            ImageFileHelper.loadIntoImageView(this, currentSong.getImage(), ivAlbumArt, R.drawable.ic_music_note);
            
            String musicUrl = DatabaseHelper.getFirebaseStorageUrl(currentSong.getUrl());
            prepareMediaPlayer(musicUrl);
        }
    }

    private void prepareMediaPlayer(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                isPlaying = true;
                btnPlay.setImageResource(R.drawable.pausemusic);
                mp.start();
                updateSeekBar();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer Error: " + what + ", " + extra);
                Toast.makeText(this, "Không thể phát bài hát này", Toast.LENGTH_SHORT).show();
                return false;
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlay.setImageResource(R.drawable.playmusic);
                seekBar.setProgress(0);
            });
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source", e);
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            seekBar.setMax(mediaPlayer.getDuration());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPos = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPos);
                        tvCurrentTime.setText(formatDuration(currentPos / 1000));
                        handler.postDelayed(this, 1000);
                    }
                }
            }, 0);
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
            if (mediaPlayer == null) return;
            
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                btnPlay.setImageResource(R.drawable.playmusic);
            } else {
                mediaPlayer.start();
                isPlaying = true;
                btnPlay.setImageResource(R.drawable.pausemusic);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}
