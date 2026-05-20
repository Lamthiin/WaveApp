package com.ptithcm.waveapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class MusicPlayerService extends Service {

    public interface PlaybackCallback {
        void onPrepared(int durationMs);
        void onCompletion();
        void onPlaybackStateChanged(boolean isPlaying);
    }

    public static final String ACTION_TOGGLE_PLAYBACK = "com.ptithcm.waveapp.action.TOGGLE_PLAYBACK";
    public static final String ACTION_STOP = "com.ptithcm.waveapp.action.STOP";

    private static final String TAG = "MusicPlayerService";
    private static final String CHANNEL_ID = "wave_playback";
    private static final int NOTIFICATION_ID = 101;

    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private PlaybackCallback callback;
    private String currentSongId;
    private String currentImageUrl;
    private String currentUrl;
    private String currentTitle = "WaveApp";
    private String currentArtist = "";
    private boolean prepared = false;

    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (ACTION_TOGGLE_PLAYBACK.equals(intent.getAction())) {
                togglePlayback();
            } else if (ACTION_STOP.equals(intent.getAction())) {
                stopPlayback();
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    public void setPlaybackCallback(PlaybackCallback callback) {
        this.callback = callback;
    }

    public void playNewSong(String url, String title, String artist) {
        playNewSong(null, url, title, artist, null);
    }

    public void playNewSong(String songId, String url, String title, String artist, String imageUrl) {
        currentSongId = songId;
        currentImageUrl = imageUrl;
        currentUrl = url;
        currentTitle = title != null ? title : "WaveApp";
        currentArtist = artist != null ? artist : "";
        prepared = false;

        releasePlayer();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> {
                prepared = true;
                mp.start();
                startForeground(NOTIFICATION_ID, buildNotification(true));
                if (callback != null) {
                    callback.onPrepared(mp.getDuration());
                    callback.onPlaybackStateChanged(true);
                }
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                stopForeground(false);
                if (callback != null) {
                    callback.onCompletion();
                    callback.onPlaybackStateChanged(false);
                }
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                stopForeground(false);
                if (callback != null) {
                    callback.onPlaybackStateChanged(false);
                }
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Cannot play song", e);
            if (callback != null) {
                callback.onPlaybackStateChanged(false);
            }
        }
    }

    public void togglePlayback() {
        if (mediaPlayer == null || !prepared) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification(false);
            if (callback != null) callback.onPlaybackStateChanged(false);
        } else {
            mediaPlayer.start();
            startForeground(NOTIFICATION_ID, buildNotification(true));
            if (callback != null) callback.onPlaybackStateChanged(true);
        }
    }

    public void pausePlayback() {
        if (mediaPlayer != null && prepared && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification(false);
            if (callback != null) callback.onPlaybackStateChanged(false);
        }
    }

    public void resumePlayback() {
        if (mediaPlayer != null && prepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            startForeground(NOTIFICATION_ID, buildNotification(true));
            if (callback != null) callback.onPlaybackStateChanged(true);
        }
    }

    public void seekTo(int positionMs) {
        if (mediaPlayer != null && prepared) {
            mediaPlayer.seekTo(positionMs);
        }
    }

    public int getDuration() {
        return mediaPlayer != null && prepared ? mediaPlayer.getDuration() : 0;
    }

    public int getCurrentPosition() {
        return mediaPlayer != null && prepared ? mediaPlayer.getCurrentPosition() : 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && prepared && mediaPlayer.isPlaying();
    }

    public boolean hasPlayer() {
        return mediaPlayer != null;
    }

    public String getCurrentSongId() {
        return currentSongId;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public String getCurrentArtist() {
        return currentArtist;
    }

    public String getCurrentImageUrl() {
        return currentImageUrl;
    }

    private void stopPlayback() {
        releasePlayer();
        stopForeground(true);
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        prepared = false;
    }

    private Notification buildNotification(boolean playing) {
        Intent openIntent = new Intent(this, MusicPlayerActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent toggleIntent = new Intent(this, MusicPlayerService.class);
        toggleIntent.setAction(ACTION_TOGGLE_PLAYBACK);
        PendingIntent togglePendingIntent = PendingIntent.getService(
                this,
                1,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, MusicPlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                2,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int playPauseIcon = playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        String playPauseTitle = playing ? "Tạm dừng" : "Phát";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(currentTitle)
                .setContentText(currentArtist)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(playing)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(playPauseIcon, playPauseTitle, togglePendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tắt", stopPendingIntent)
                .build();
    }

    private void updateNotification(boolean playing) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(playing));
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Wave playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Điều khiển phát nhạc của WaveApp");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
