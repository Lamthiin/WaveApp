package com.ptithcm.waveapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;

import java.io.IOException;

public class MusicPlayerService extends Service {

    public interface PlaybackCallback {
        void onPrepared(int durationMs);
        void onCompletion();
        void onPlaybackStateChanged(boolean isPlaying);
        void onRepeatModeChanged(boolean isRepeatOne);
    }

    public interface NavigationCallback {
        void onSkipToPrevious();
        void onSkipToNext();
    }

    public static final String ACTION_TOGGLE_PLAYBACK = "com.ptithcm.waveapp.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PREVIOUS        = "com.ptithcm.waveapp.action.PREVIOUS";
    public static final String ACTION_NEXT            = "com.ptithcm.waveapp.action.NEXT";
    public static final String ACTION_REPEAT          = "com.ptithcm.waveapp.action.REPEAT";
    public static final String ACTION_STOP            = "com.ptithcm.waveapp.action.STOP";

    private static final String TAG = "MusicPlayerService";
    private static final String CHANNEL_ID = "wave_playback";
    private static final int NOTIFICATION_ID = 101;

    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private PlaybackCallback callback;
    private NavigationCallback navigationCallback;
    private String currentSongId;
    private String currentImageUrl;
    private String currentUrl;
    private String currentTitle = "WaveApp";
    private String currentArtist = "";
    private boolean prepared = false;
    private boolean repeatOne = false;
    private boolean foregroundStarted = false;
    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable notificationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (prepared && mediaPlayer != null && mediaPlayer.isPlaying()) {
                updateNotification(true);
                handler.postDelayed(this, 1000);
            }
        }
    };

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
        ensureForegroundStarted();

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_TOGGLE_PLAYBACK:
                    togglePlayback();
                    break;
                case ACTION_PREVIOUS:
                    if (navigationCallback != null) navigationCallback.onSkipToPrevious();
                    break;
                case ACTION_NEXT:
                    if (navigationCallback != null) navigationCallback.onSkipToNext();
                    break;
                case ACTION_REPEAT:
                    toggleRepeatOne();
                    break;
                case ACTION_STOP:
                    stopPlayback();
                    stopSelf();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    public void setPlaybackCallback(PlaybackCallback callback) {
        this.callback = callback;
    }

    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
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

        ensureForegroundStarted();

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
                loadAlbumArtThenNotify(true);
                startNotificationUpdates();
                if (callback != null) {
                    callback.onPrepared(mp.getDuration());
                    callback.onPlaybackStateChanged(true);
                }
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (repeatOne) {
                    mp.seekTo(0);
                    mp.start();
                    updateNotification(true);
                } else {
                    stopNotificationUpdates();
                    stopForegroundSafely(false);
                    if (callback != null) {
                        callback.onCompletion();
                        callback.onPlaybackStateChanged(false);
                    }
                }
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                stopForegroundSafely(false);
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
            stopNotificationUpdates();
            updateNotification(false);
            if (callback != null) callback.onPlaybackStateChanged(false);
        } else {
            mediaPlayer.start();
            loadAlbumArtThenNotify(true);
            startNotificationUpdates();
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
            loadAlbumArtThenNotify(true);
            if (callback != null) callback.onPlaybackStateChanged(true);
        }
    }

    public void toggleRepeatOne() {
        repeatOne = !repeatOne;
        updateNotification(isPlaying());
        if (callback != null) callback.onRepeatModeChanged(repeatOne);
    }

    public boolean isRepeatOne() {
        return repeatOne;
    }

    public void setRepeatOne(boolean repeatOne) {
        this.repeatOne = repeatOne;
        updateNotification(isPlaying());
    }

    private void startNotificationUpdates() {
        handler.removeCallbacks(notificationUpdateRunnable);
        handler.post(notificationUpdateRunnable);
    }

    private void stopNotificationUpdates() {
        handler.removeCallbacks(notificationUpdateRunnable);
    }

    private void loadAlbumArtThenNotify(boolean playing) {
        if (currentImageUrl == null || currentImageUrl.isEmpty()) {
            startForegroundOrUpdate(buildNotification(playing, null));
            return;
        }
        new Thread(() -> {
            try {
                Bitmap bitmap = Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(currentImageUrl)
                        .submit(256, 256)
                        .get();
                startForegroundOrUpdate(buildNotification(playing, bitmap));
            } catch (Exception e) {
                startForegroundOrUpdate(buildNotification(playing, null));
            }
        }).start();
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
        stopNotificationUpdates();
        releasePlayer();
        stopForegroundSafely(true);
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        prepared = false;
    }

    private Notification buildNotification(boolean playing) {
        return buildNotification(playing, null);
    }

    private Notification buildNotification(boolean playing, Bitmap albumArt) {
        Intent openIntent = new Intent(this, MusicPlayerActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0,
                new Intent(this, MusicPlayerService.class).setAction(ACTION_PREVIOUS),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent togglePendingIntent = PendingIntent.getService(this, 1,
                new Intent(this, MusicPlayerService.class).setAction(ACTION_TOGGLE_PLAYBACK),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent nextPendingIntent = PendingIntent.getService(this, 2,
                new Intent(this, MusicPlayerService.class).setAction(ACTION_NEXT),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent repeatPendingIntent = PendingIntent.getService(this, 3,
                new Intent(this, MusicPlayerService.class).setAction(ACTION_REPEAT),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int playPauseIcon = playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        int repeatIcon = repeatOne ? R.drawable.ic_repeat : android.R.drawable.ic_menu_rotate; // Using ic_menu_rotate as "repeat off" placeholder if ic_repeat is only for "on"

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(currentTitle)
                .setContentText(currentArtist)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(playing)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous, "Trước", prevPendingIntent)
                .addAction(playPauseIcon, playing ? "Tạm dừng" : "Phát", togglePendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Tiếp", nextPendingIntent)
                .addAction(repeatIcon, "Lặp lại", repeatPendingIntent);

        if (albumArt != null) {
            builder.setLargeIcon(albumArt);
        }
        
        if (prepared) {
            builder.setProgress(getDuration(), getCurrentPosition(), false);
        }

        return builder.build();
    }

    private void ensureForegroundStarted() {
        startForegroundOrUpdate(buildNotification(false, null));
    }

    private void startForegroundOrUpdate(Notification notification) {
        if (!foregroundStarted) {
            startForeground(NOTIFICATION_ID, notification);
            foregroundStarted = true;
            return;
        }

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void stopForegroundSafely(boolean removeNotification) {
        if (!foregroundStarted) {
            return;
        }
        stopForeground(removeNotification);
        foregroundStarted = false;
    }

    private void updateNotification(boolean playing) {
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    Bitmap bitmap = Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(currentImageUrl)
                            .submit(256, 256)
                            .get();
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (manager != null) {
                        manager.notify(NOTIFICATION_ID, buildNotification(playing, bitmap));
                    }
                } catch (Exception e) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (manager != null) {
                        manager.notify(NOTIFICATION_ID, buildNotification(playing, null));
                    }
                }
            }).start();
        } else {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, buildNotification(playing, null));
            }
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
        stopNotificationUpdates();
        releasePlayer();
        foregroundStarted = false;
        super.onDestroy();
    }
}
