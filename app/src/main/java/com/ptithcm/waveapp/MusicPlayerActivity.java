package com.ptithcm.waveapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.LikedSong;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.util.TokenManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity implements MusicPlayerService.PlaybackCallback {

    private static final String TAG = "MusicPlayerActivity";
    private static final int REQUEST_NOTIFICATIONS = 1001;

    private ShapeableImageView ivAlbumArt;
    private TextView tvSongTitle, tvArtistName, tvCurrentTime, tvTotalTime, tvHeaderTitle, tvLyricsPreview;
    private Button btnLyricsDetail;
    private ImageButton btnPlay, btnPrevious, btnNext, btnBack, btnShuffle, btnTimer, btnQueue, btnAdd, btnOptions;
    private SeekBar seekBar;

    private MusicPlayerService musicService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;
    private Runnable sleepTimerRunnable;
    private boolean serviceBound = false;
    private boolean intentHandled = false;
    private boolean isPlaying = false;
    private boolean isLiked = false;
    private boolean shuffleEnabled = false;
    private boolean lyricsExpanded = false;
    private boolean isUserSeeking = false;

    private Song currentSong;
    private String songId;
    private final List<Song> playbackQueue = new ArrayList<>();
    private final Random random = new Random();
    private int currentIndex = -1;

    private SongRepository songRepo;
    private LikedSongRepository likedSongRepo;
    private TokenManager tokenManager;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();
            musicService.setPlaybackCallback(MusicPlayerActivity.this);
            serviceBound = true;
            if (!intentHandled) {
                intentHandled = true;
                handleIntent();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            musicService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_song);

        initRepositories();
        initViews();
        setupListeners();
        requestNotificationPermissionIfNeeded();
        bindMusicService();
    }

    private void initRepositories() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        songRepo = new SongRepository(dbHelper);
        likedSongRepo = new LikedSongRepository(dbHelper);
        tokenManager = new TokenManager(this);
    }

    private void bindMusicService() {
        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS
            );
        }
    }

    private void initViews() {
        ivAlbumArt = findViewById(R.id.ivAlbumArt);
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvLyricsPreview = findViewById(R.id.tvLyricsPreview);
        btnLyricsDetail = findViewById(R.id.btnLyricsDetail);

        btnPlay = findViewById(R.id.btnPlay);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnTimer = findViewById(R.id.btnTimer);
        btnQueue = findViewById(R.id.btnQueue);
        btnAdd = findViewById(R.id.btnAdd);
        btnOptions = findViewById(R.id.btnOptions);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setProgress(0);
        tvCurrentTime.setText(formatDuration(0));
        updateShuffleButtonUI();
    }

    private void handleIntent() {
        songId = getIntent().getStringExtra("SONG_ID");
        if (songId != null) {
            loadSongDetails(songId);
            return;
        }

        currentSong = (Song) getIntent().getSerializableExtra("SONG_DATA");
        if (currentSong != null) {
            songId = currentSong.getId();
            updateUI(currentSong);
            loadLikedStatus(songId);
            loadPlaybackQueue(songId);
            prepareMusicService(currentSong.getUrl());
        } else {
            Toast.makeText(this, "Khong tim thay bai hat", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadSongDetails(String id) {
        new Thread(() -> {
            Optional<Song> songOptional = songRepo.findById(id);
            if (songOptional.isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Khong tim thay bai hat", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            Song song = songOptional.get();
            currentSong = song;
            String userId = tokenManager.getUserId();
            isLiked = userId != null && likedSongRepo.existsByUserIdAndSongId(userId, id);

            runOnUiThread(() -> {
                updateUI(song);
                updateLikeButtonUI();
                loadPlaybackQueue(id);
                prepareMusicService(song.getUrl());
            });
        }).start();
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

    private int findSongIndex(String id) {
        if (id == null) return -1;
        for (int i = 0; i < playbackQueue.size(); i++) {
            if (id.equals(playbackQueue.get(i).getId())) return i;
        }
        return -1;
    }

    private void updateUI(Song song) {
        tvSongTitle.setText(song.getName());
        if (song.getArtist() != null) {
            tvArtistName.setText(song.getArtist().getName());
            tvHeaderTitle.setText(song.getArtist().getName());
        } else {
            tvArtistName.setText("");
            tvHeaderTitle.setText(getString(R.string.liked_songs));
        }
        tvTotalTime.setText(formatDuration(song.getDuration()));
        updateLyricsPreview(song.getLyrics());

        Glide.with(this)
                .load(song.getImage())
                .placeholder(R.drawable.ic_music_note)
                .into(ivAlbumArt);
    }

    private void prepareMusicService(String url) {
        stopSeekBarUpdates();

        if (url == null || url.trim().isEmpty()) {
            isPlaying = false;
            btnPlay.setImageResource(R.drawable.playmusic);
            seekBar.setProgress(0);
            tvCurrentTime.setText(formatDuration(0));
            Toast.makeText(this, "Bai hat chua co duong dan phat", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!serviceBound || musicService == null) {
            Log.e(TAG, "Music service is not connected");
            Toast.makeText(this, "Trinh phat nhac chua san sang", Toast.LENGTH_SHORT).show();
            return;
        }

        String artistName = currentSong != null && currentSong.getArtist() != null
                ? currentSong.getArtist().getName()
                : "";
        String title = currentSong != null ? currentSong.getName() : "WaveApp";
        String imageUrl = currentSong != null ? currentSong.getImage() : null;
        String currentId = currentSong != null ? currentSong.getId() : songId;

        if (isSameSongAlreadyLoaded(currentId)) {
            syncUiWithCurrentPlayback();
            return;
        }

        ContextCompat.startForegroundService(this, new Intent(this, MusicPlayerService.class));
        musicService.playNewSong(currentId, url, title, artistName, imageUrl);
    }

    private boolean isSameSongAlreadyLoaded(String currentId) {
        return musicService != null
                && musicService.hasPlayer()
                && currentId != null
                && currentId.equals(musicService.getCurrentSongId());
    }

    private void syncUiWithCurrentPlayback() {
        int durationMs = musicService.getDuration();
        int currentPositionMs = musicService.getCurrentPosition();

        if (durationMs > 0) {
            seekBar.setMax(durationMs);
            tvTotalTime.setText(formatDuration(durationMs / 1000));
        }
        seekBar.setProgress(currentPositionMs);
        tvCurrentTime.setText(formatDuration(currentPositionMs / 1000));

        isPlaying = musicService.isPlaying();
        btnPlay.setImageResource(isPlaying ? R.drawable.pausemusic : R.drawable.playmusic);
        if (isPlaying) {
            startSeekBarUpdates();
        } else {
            stopSeekBarUpdates();
        }
    }

    @Override
    public void onPrepared(int durationMs) {
        runOnUiThread(() -> {
            isPlaying = true;
            seekBar.setMax(durationMs);
            tvTotalTime.setText(formatDuration(durationMs / 1000));
            btnPlay.setImageResource(R.drawable.pausemusic);
            startSeekBarUpdates();
            if (songId != null) {
                new Thread(() -> songRepo.incrementPlayCount(songId)).start();
            }
        });
    }

    @Override
    public void onCompletion() {
        runOnUiThread(() -> {
            isPlaying = false;
            btnPlay.setImageResource(R.drawable.playmusic);
            seekBar.setProgress(0);
            tvCurrentTime.setText(formatDuration(0));
            stopSeekBarUpdates();
            playNextSong();
        });
    }

    @Override
    public void onPlaybackStateChanged(boolean playing) {
        runOnUiThread(() -> {
            this.isPlaying = playing;
            btnPlay.setImageResource(playing ? R.drawable.pausemusic : R.drawable.playmusic);
            if (playing) {
                startSeekBarUpdates();
            } else {
                stopSeekBarUpdates();
            }
        });
    }

    private void startSeekBarUpdates() {
        stopSeekBarUpdates();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (musicService != null && musicService.isPlaying()) {
                    if (!isUserSeeking) {
                        int currentPos = musicService.getCurrentPosition();
                        seekBar.setProgress(currentPos);
                        tvCurrentTime.setText(formatDuration(currentPos / 1000));
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(progressRunnable);
    }

    private void stopSeekBarUpdates() {
        if (progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }

    private void playNextSong() {
        playSongByDirection(1);
    }

    private void playPreviousSong() {
        playSongByDirection(-1);
    }

    private void playSongByDirection(int direction) {
        if (playbackQueue.isEmpty()) {
            Toast.makeText(this, "Danh sach phat dang tai", Toast.LENGTH_SHORT).show();
            loadPlaybackQueue(songId);
            return;
        }

        if (currentIndex < 0) currentIndex = findSongIndex(songId);

        if (currentIndex < 0) {
            currentIndex = 0;
        } else if (shuffleEnabled && playbackQueue.size() > 1) {
            int nextIndex;
            do {
                nextIndex = random.nextInt(playbackQueue.size());
            } while (nextIndex == currentIndex);
            currentIndex = nextIndex;
        } else {
            currentIndex = (currentIndex + direction + playbackQueue.size()) % playbackQueue.size();
        }

        playSong(playbackQueue.get(currentIndex));
    }

    private void playSong(Song song) {
        currentSong = song;
        songId = song.getId();
        seekBar.setProgress(0);
        tvCurrentTime.setText(formatDuration(0));
        updateUI(song);
        loadLikedStatus(songId);
        prepareMusicService(song.getUrl());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlay.setOnClickListener(v -> {
            if (!serviceBound || musicService == null) {
                if (currentSong != null) {
                    prepareMusicService(currentSong.getUrl());
                } else {
                    Toast.makeText(this, "Trinh phat nhac chua san sang", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (!musicService.hasPlayer()) {
                if (currentSong != null) {
                    prepareMusicService(currentSong.getUrl());
                }
                return;
            }

            boolean shouldPlay = !musicService.isPlaying();
            musicService.togglePlayback();
            isPlaying = shouldPlay;
            btnPlay.setImageResource(shouldPlay ? R.drawable.pausemusic : R.drawable.playmusic);
            if (shouldPlay) {
                startSeekBarUpdates();
            } else {
                stopSeekBarUpdates();
            }
        });

        btnAdd.setOnClickListener(v -> toggleLike());
        btnShuffle.setOnClickListener(v -> toggleShuffle());
        btnPrevious.setOnClickListener(v -> playPreviousSong());
        btnNext.setOnClickListener(v -> playNextSong());
        btnTimer.setOnClickListener(v -> showSleepTimerDialog());
        btnQueue.setOnClickListener(v -> showQueueDialog());
        btnOptions.setOnClickListener(v -> showMoreOptionsDialog());
        btnLyricsDetail.setOnClickListener(v -> toggleLyricsExpanded());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    tvCurrentTime.setText(formatDuration(progress / 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicService != null) {
                    musicService.seekTo(seekBar.getProgress());
                }
                isUserSeeking = false;
                if (isPlaying) {
                    startSeekBarUpdates();
                }
            }
        });
    }

    private void loadLikedStatus(String id) {
        new Thread(() -> {
            String userId = tokenManager.getUserId();
            isLiked = userId != null && likedSongRepo.existsByUserIdAndSongId(userId, id);
            runOnUiThread(this::updateLikeButtonUI);
        }).start();
    }

    private void updateLikeButtonUI() {
        if (isLiked) {
            btnAdd.setImageResource(R.drawable.ic_heart_filled);
            btnAdd.setColorFilter(ContextCompat.getColor(this, R.color.spotify_green));
        } else {
            btnAdd.setImageResource(R.drawable.add);
            btnAdd.clearColorFilter();
        }
    }

    private void toggleLike() {
        if (currentSong == null) return;
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui long dang nhap de thich bai hat", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (isLiked) {
                likedSongRepo.deleteByUserIdAndSongId(userId, currentSong.getId());
                songRepo.decrementLikeCount(currentSong.getId());
            } else {
                User user = new User();
                user.setId(userId);

                LikedSong ls = new LikedSong();
                ls.setUser(user);
                ls.setSong(currentSong);
                ls.setLikedAt(LocalDateTime.now().toString());

                likedSongRepo.save(ls);
                songRepo.incrementLikeCount(currentSong.getId());
            }
            isLiked = !isLiked;
            runOnUiThread(this::updateLikeButtonUI);
        }).start();
    }

    private void toggleShuffle() {
        shuffleEnabled = !shuffleEnabled;
        updateShuffleButtonUI();
    }

    private void updateShuffleButtonUI() {
        int color = shuffleEnabled
                ? ContextCompat.getColor(this, R.color.spotify_green)
                : ContextCompat.getColor(this, R.color.white);
        btnShuffle.setColorFilter(color);
    }

    private void showQueueDialog() {
        if (playbackQueue.isEmpty()) {
            Toast.makeText(this, "Danh sach phat dang tai", Toast.LENGTH_SHORT).show();
            loadPlaybackQueue(songId);
            return;
        }

        String[] songTitles = new String[playbackQueue.size()];
        for (int i = 0; i < playbackQueue.size(); i++) {
            Song song = playbackQueue.get(i);
            String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown artist";
            String prefix = i == currentIndex ? "Dang phat: " : "";
            songTitles[i] = prefix + song.getName() + " - " + artistName;
        }

        new AlertDialog.Builder(this)
                .setTitle("Queue")
                .setItems(songTitles, (dialog, which) -> {
                    currentIndex = which;
                    playSong(playbackQueue.get(which));
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showSleepTimerDialog() {
        String[] options = {"Tat hen gio", "5 phut", "15 phut", "30 phut", "60 phut"};
        int[] minutes = {0, 5, 15, 30, 60};

        new AlertDialog.Builder(this)
                .setTitle("Sleep timer")
                .setItems(options, (dialog, which) -> scheduleSleepTimer(minutes[which]))
                .setNegativeButton("Close", null)
                .show();
    }

    private void scheduleSleepTimer(int minutes) {
        cancelSleepTimer();
        if (minutes <= 0) {
            Toast.makeText(this, "Da tat hen gio", Toast.LENGTH_SHORT).show();
            return;
        }

        sleepTimerRunnable = () -> {
            if (musicService != null) musicService.pausePlayback();
            Toast.makeText(this, "Da dung phat theo hen gio", Toast.LENGTH_SHORT).show();
        };
        handler.postDelayed(sleepTimerRunnable, minutes * 60L * 1000L);
        Toast.makeText(this, "Se dung sau " + minutes + " phut", Toast.LENGTH_SHORT).show();
    }

    private void cancelSleepTimer() {
        if (sleepTimerRunnable != null) {
            handler.removeCallbacks(sleepTimerRunnable);
            sleepTimerRunnable = null;
        }
    }

    private void showMoreOptionsDialog() {
        String likeText = isLiked ? "Bo thich bai hat" : "Thich bai hat";
        String[] options = {likeText, "Phat lai tu dau", "Chia se bai hat", "Thong tin bai hat"};

        new AlertDialog.Builder(this)
                .setTitle("More")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) toggleLike();
                    else if (which == 1) replayCurrentSong();
                    else if (which == 2) shareCurrentSong();
                    else if (which == 3) showSongInfoDialog();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void replayCurrentSong() {
        if (musicService == null || !musicService.hasPlayer()) return;
        musicService.seekTo(0);
        seekBar.setProgress(0);
        tvCurrentTime.setText(formatDuration(0));
        musicService.resumePlayback();
    }

    private void shareCurrentSong() {
        if (currentSong == null) return;
        String artistName = currentSong.getArtist() != null ? currentSong.getArtist().getName() : "Unknown artist";
        String text = currentSong.getName() + " - " + artistName + "\n" + currentSong.getUrl();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Share song"));
    }

    private void showSongInfoDialog() {
        if (currentSong == null) return;
        String artistName = currentSong.getArtist() != null ? currentSong.getArtist().getName() : "Unknown artist";
        String message = "Song: " + currentSong.getName()
                + "\nArtist: " + artistName
                + "\nDuration: " + formatDuration(currentSong.getDuration())
                + "\nPlays: " + currentSong.getPlayCount()
                + "\nLikes: " + currentSong.getLikeCount();

        new AlertDialog.Builder(this)
                .setTitle("Song info")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateLyricsPreview(String lyrics) {
        String cleanLyrics = getDisplayLyrics(lyrics);
        tvLyricsPreview.setText(cleanLyrics);
        btnLyricsDetail.setEnabled(!cleanLyrics.equals("Chua co loi bai hat"));
        btnLyricsDetail.setAlpha(btnLyricsDetail.isEnabled() ? 1f : 0.5f);
        setLyricsExpanded(false);
    }

    private String getDisplayLyrics(String lyrics) {
        if (lyrics == null || lyrics.trim().isEmpty()) return "Chua co loi bai hat";
        return lyrics.trim();
    }

    private void toggleLyricsExpanded() {
        if (currentSong == null || !btnLyricsDetail.isEnabled()) return;
        setLyricsExpanded(!lyricsExpanded);
    }

    private void setLyricsExpanded(boolean expanded) {
        lyricsExpanded = expanded;
        tvLyricsPreview.setMaxLines(expanded ? Integer.MAX_VALUE : 3);
        tvLyricsPreview.setEllipsize(expanded ? null : TextUtils.TruncateAt.END);
        btnLyricsDetail.setText(expanded ? "Thu gon" : "Xem chi tiet");
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
    }

    @Override
    protected void onDestroy() {
        stopSeekBarUpdates();
        cancelSleepTimer();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        super.onDestroy();
    }
}
