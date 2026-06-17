package com.ptithcm.waveapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.SongRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BaseMiniPlayerActivity extends AppCompatActivity implements MusicPlayerService.PlaybackCallback {

    private LinearLayout layoutMiniPlayer;
    private ShapeableImageView ivMiniAlbumArt;
    private TextView tvMiniSongTitle;
    private TextView tvMiniArtistName;
    private ImageButton btnMiniShuffle;
    private ImageButton btnMiniPlay;

    protected MusicPlayerService musicService;
    private boolean serviceBound;
    private SongRepository songRepo;
    private final List<Song> playbackQueue = new ArrayList<>();
    private final Random random = new Random();
    private boolean shuffleEnabled = false;
    private int currentIndex = -1;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            musicService.setPlaybackCallback(BaseMiniPlayerActivity.this);
            musicService.setNavigationCallback(new MusicPlayerService.NavigationCallback() {
                @Override public void onSkipToPrevious() { runOnUiThread(() -> playMiniSongByDirection(-1)); }
                @Override public void onSkipToNext()     { runOnUiThread(() -> playMiniSongByDirection(1)); }
            });
            updateMiniPlayerFromService();
            loadPlaybackQueue(musicService.getCurrentSongId());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            musicService = null;
            hideMiniPlayer();
        }
    };

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        attachMiniPlayerOverlay();
    }

    private void attachMiniPlayerOverlay() {
        ViewGroup contentRoot = findViewById(android.R.id.content);
        if (contentRoot == null || contentRoot.getChildCount() == 0) {
            return;
        }

        View existing = contentRoot.findViewById(R.id.layoutMiniPlayer);
        if (existing != null) {
            layoutMiniPlayer = (LinearLayout) existing;
        } else {
            View overlay = LayoutInflater.from(this).inflate(R.layout.layout_mini_player, contentRoot, false);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM;
            params.leftMargin = dpToPx(8);
            params.rightMargin = dpToPx(8);
            params.bottomMargin = dpToPx(18);
            overlay.setLayoutParams(params);
            contentRoot.addView(overlay);
            layoutMiniPlayer = overlay.findViewById(R.id.layoutMiniPlayer);
        }

        songRepo = new SongRepository(DatabaseHelper.getInstance(this));
        initMiniPlayerViews();
        setupMiniPlayerListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, MusicPlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (musicService != null) {
            musicService.setPlaybackCallback(null);
            musicService.setNavigationCallback(null);
        }
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (serviceBound) {
            updateMiniPlayerFromService();
            if (musicService != null) {
                loadPlaybackQueue(musicService.getCurrentSongId());
            }
        }
    }

    private void initMiniPlayerViews() {
        if (layoutMiniPlayer == null) return;
        ivMiniAlbumArt = findViewById(R.id.ivMiniAlbumArt);
        tvMiniSongTitle = findViewById(R.id.tvMiniSongTitle);
        tvMiniArtistName = findViewById(R.id.tvMiniArtistName);
        btnMiniShuffle = findViewById(R.id.btnMiniShuffle);
        btnMiniPlay = findViewById(R.id.btnMiniPlay);
        updateMiniShuffleButtonUI();
        updateMiniPlayButtonUI();
    }

    private void setupMiniPlayerListeners() {
        if (layoutMiniPlayer == null) return;
        ImageButton btnMiniPrevious = findViewById(R.id.btnMiniPrevious);
        ImageButton btnMiniNext = findViewById(R.id.btnMiniNext);

        layoutMiniPlayer.setOnClickListener(v -> openCurrentSongDetail());
        btnMiniShuffle.setOnClickListener(v -> {
            shuffleEnabled = !shuffleEnabled;
            updateMiniShuffleButtonUI();
        });
        btnMiniPlay.setOnClickListener(v -> toggleMiniPlayback());
        btnMiniPrevious.setOnClickListener(v -> playMiniSongByDirection(-1));
        btnMiniNext.setOnClickListener(v -> playMiniSongByDirection(1));
    }

    protected void updateMiniPlayerFromService() {
        if (isFinishing() || isDestroyed() || layoutMiniPlayer == null) {
            return;
        }

        if (musicService == null || !musicService.hasPlayer()) {
            hideMiniPlayer();
            return;
        }

        layoutMiniPlayer.setVisibility(View.VISIBLE);
        tvMiniSongTitle.setText(musicService.getCurrentTitle());
        tvMiniArtistName.setText(musicService.getCurrentArtist());
        updateMiniPlayButtonUI();
        if (ivMiniAlbumArt != null) {
            Glide.with(this)
                    .load(musicService.getCurrentImageUrl())
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .into(ivMiniAlbumArt);
        }
    }

    protected void hideMiniPlayer() {
        if (layoutMiniPlayer != null && !isDestroyed()) {
            layoutMiniPlayer.setVisibility(View.GONE);
        }
    }

    private void loadPlaybackQueue(String selectedSongId) {
        new Thread(() -> {
            List<Song> songs = songRepo.findByActiveTrue();
            runOnUiThread(() -> {
                playbackQueue.clear();
                playbackQueue.addAll(songs);
                currentIndex = findSongIndex(selectedSongId);
            });
        }).start();
    }

    private int findSongIndex(String selectedSongId) {
        if (selectedSongId == null) return -1;
        for (int i = 0; i < playbackQueue.size(); i++) {
            if (selectedSongId.equals(playbackQueue.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private void playMiniSongByDirection(int direction) {
        if (musicService == null) return;
        if (playbackQueue.isEmpty()) {
            loadPlaybackQueue(musicService.getCurrentSongId());
            return;
        }

        if (currentIndex < 0) {
            currentIndex = findSongIndex(musicService.getCurrentSongId());
        }
        if (currentIndex < 0) currentIndex = 0;

        if (shuffleEnabled && playbackQueue.size() > 1) {
            int nextIndex;
            do {
                nextIndex = random.nextInt(playbackQueue.size());
            } while (nextIndex == currentIndex);
            currentIndex = nextIndex;
        } else {
            currentIndex = (currentIndex + direction + playbackQueue.size()) % playbackQueue.size();
        }

        playMiniSong(playbackQueue.get(currentIndex));
    }

    private void playMiniSong(Song song) {
        if (isFinishing() || isDestroyed() || musicService == null || layoutMiniPlayer == null) {
            return;
        }
        String artistName = getArtistName(song);
        ContextCompat.startForegroundService(this, new Intent(this, MusicPlayerService.class));
        musicService.playNewSong(song.getId(), song.getUrl(), song.getName(), artistName, song.getImage());
        layoutMiniPlayer.setVisibility(View.VISIBLE);
        tvMiniSongTitle.setText(song.getName());
        tvMiniArtistName.setText(artistName);
        updateMiniPlayButtonUI();
        if (ivMiniAlbumArt != null) {
            Glide.with(this)
                    .load(song.getImage())
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .into(ivMiniAlbumArt);
        }
    }

    private String getArtistName(Song song) {
        Artist artist = song.getArtist();
        if (artist != null && artist.getName() != null && !artist.getName().isEmpty()) {
            return artist.getName();
        }
        return "";
    }

    protected void openCurrentSongDetail() {
        if (musicService == null || musicService.getCurrentSongId() == null) return;
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        intent.putExtra("SONG_ID", musicService.getCurrentSongId());
        startActivity(intent);
    }

    private void toggleMiniPlayback() {
        if (musicService == null || !musicService.hasPlayer()) return;
        musicService.togglePlayback();
        updateMiniPlayButtonUI();
    }

    private void updateMiniShuffleButtonUI() {
        if (btnMiniShuffle != null) {
            btnMiniShuffle.setColorFilter(shuffleEnabled ? Color.parseColor("#1ED760") : Color.WHITE);
        }
    }

    private void updateMiniPlayButtonUI() {
        if (btnMiniPlay == null) return;
        boolean playing = musicService != null && musicService.isPlaying();
        btnMiniPlay.setImageResource(playing ? R.drawable.pausemusic : R.drawable.playmusic);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onPrepared(int durationMs) {
        if (!isFinishing() && !isDestroyed()) {
            updateMiniPlayerFromService();
        }
    }

    @Override
    public void onCompletion() {
        if (!isFinishing() && !isDestroyed()) {
            playMiniSongByDirection(1);
        }
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        if (!isFinishing() && !isDestroyed()) {
            updateMiniPlayerFromService();
        }
    }

    @Override
    public void onRepeatModeChanged(boolean isRepeatOne) {
        // Có thể cập nhật UI nếu mini player có nút repeat
    }
}
