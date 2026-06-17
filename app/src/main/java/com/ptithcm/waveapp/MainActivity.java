package com.ptithcm.waveapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.ptithcm.waveapp.auth.LoginActivity;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MusicPlayerService.PlaybackCallback {

    private LinearLayout layoutMiniPlayer;
    private ShapeableImageView ivMiniAlbumArt;
    private TextView tvMiniSongTitle;
    private TextView tvMiniArtistName;
    private ImageButton btnMiniShuffle;
    private ImageButton btnMiniPlay;

    private MusicPlayerService musicService;
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
            musicService.setPlaybackCallback(MainActivity.this);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenManager tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        songRepo = new SongRepository(DatabaseHelper.getInstance(this));
        initMiniPlayerViews();
        setupMiniPlayerListeners();
        bindMusicService();

        syncDataFromFirebase();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();

            } else if (item.getItemId() == R.id.nav_search) {
                selectedFragment = new SearchFragment();

            } else if (item.getItemId() == R.id.nav_library) {
                selectedFragment = new LibraryFragment();

            } else if (item.getItemId() == R.id.nav_add_playlist) {
                startActivity(new Intent(this, MyPlaylistsActivity.class));
                return false;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }

    private void initMiniPlayerViews() {
        layoutMiniPlayer = findViewById(R.id.layoutMiniPlayer);
        ivMiniAlbumArt = findViewById(R.id.ivMiniAlbumArt);
        tvMiniSongTitle = findViewById(R.id.tvMiniSongTitle);
        tvMiniArtistName = findViewById(R.id.tvMiniArtistName);
        btnMiniShuffle = findViewById(R.id.btnMiniShuffle);
        btnMiniPlay = findViewById(R.id.btnMiniPlay);
        updateMiniShuffleButtonUI();
        updateMiniPlayButtonUI();
    }

    private void setupMiniPlayerListeners() {
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

    private void bindMusicService() {
        bindService(new Intent(this, MusicPlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void updateMiniPlayerFromService() {
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

    private void hideMiniPlayer() {
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
        updateMiniPlayerFromSong(song, artistName);
    }

    private void updateMiniPlayerFromSong(Song song, String artistName) {
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

    private void openCurrentSongDetail() {
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

    @Override
    protected void onDestroy() {
        if (musicService != null) {
            musicService.setPlaybackCallback(null);
            musicService.setNavigationCallback(null);
        }
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        super.onDestroy();
    }

    @Override
    public void onPrepared(int durationMs) {
        updateMiniPlayerFromService();
    }

    @Override
    public void onCompletion() {
        playMiniSongByDirection(1);
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        updateMiniPlayerFromService();
        updateMiniPlayButtonUI();
    }

    @Override
    public void onRepeatModeChanged(boolean isRepeatOne) {
        // Có thể update UI nếu mini player có nút repeat
    }

    private void syncDataFromFirebase() {
        // Firebase sync can be added here if remote data is needed later.
    }
}
