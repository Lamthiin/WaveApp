package com.ptithcm.waveapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.service.HomeService;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlaylistDetailActivity extends BaseMiniPlayerActivity {

    private HomeService homeService;
    private PlaylistService playlistService;
    private TokenManager tokenManager;

    private ImageView imgAlbumArt;
    private ImageButton btnLikeAlbum;

    private TextView tvAlbumName, tvSongCount, tvPlaylistLabel, tvPlaylistSubtitle;
    private EditText etSearchSongs;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;

    private final List<Song> songList = new ArrayList<>();
    private final List<Song> filteredList = new ArrayList<>();

    private String currentId;
    private boolean isAlbum = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        homeService = ServiceLocator.getInstance().getHomeService();
        playlistService = ServiceLocator.getInstance().getPlaylistService();
        tokenManager = new TokenManager(this);

        initViews();
        setupRecyclerView();
        handleIntent();
        setupButtons();
    }

    private void initViews() {
        imgAlbumArt = findViewById(R.id.img_playlist_cover);
        btnLikeAlbum = findViewById(R.id.btn_like_album);

        tvAlbumName = findViewById(R.id.tv_playlist_name);
        tvSongCount = findViewById(R.id.tv_playlist_meta);
        tvPlaylistLabel = findViewById(R.id.tv_playlist_label);
        tvPlaylistSubtitle = findViewById(R.id.tv_playlist_subtitle);

        etSearchSongs = findViewById(R.id.et_search_songs);
        rvSongs = findViewById(R.id.rv_playlist_songs);
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter(filteredList);
        songAdapter.setActionIconMode(SongAdapter.ActionIconMode.MORE);

        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(songAdapter);

        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            intent.putExtra("QUEUE_LIST", new ArrayList<>(songList));
            startActivity(intent);
        });

        songAdapter.setOnMoreClickListener((song, position, anchor) -> {
            if (!isAlbum) showSongOptions(song);
        });

        if (etSearchSongs != null) {
            etSearchSongs.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(android.text.Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterSongs(s.toString());
                }
            });
        }
    }

    private void filterSongs(String query) {
        filteredList.clear();

        if (query == null || query.isEmpty()) {
            filteredList.addAll(songList);
        } else {
            String lowerQuery = query.toLowerCase();

            for (Song song : songList) {
                boolean matchName = song.getName() != null &&
                        song.getName().toLowerCase().contains(lowerQuery);

                boolean matchArtist = song.getArtist() != null &&
                        song.getArtist().getName() != null &&
                        song.getArtist().getName().toLowerCase().contains(lowerQuery);

                if (matchName || matchArtist) {
                    filteredList.add(song);
                }
            }
        }

        songAdapter.notifyDataSetChanged();
    }

    private void handleIntent() {
        currentId = getIntent().getStringExtra("ALBUM_ID");

        if (currentId != null) {
            isAlbum = true;
            loadData();
            setupAlbumUI();
            return;
        }

        currentId = getIntent().getStringExtra("PLAYLIST_ID");

        if (currentId != null) {
            isAlbum = false;
            loadData();
            setupPlaylistUI();
        } else {
            Toast.makeText(this, "Không tìm thấy playlist", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupAlbumUI() {
        findViewById(R.id.btn_add_song).setVisibility(android.view.View.GONE);
        findViewById(R.id.btn_edit_playlist).setVisibility(android.view.View.GONE);
        findViewById(R.id.btn_more_options).setVisibility(android.view.View.GONE);
        findViewById(R.id.tv_song_section_title).setVisibility(android.view.View.VISIBLE);
        if (btnLikeAlbum != null) {
            btnLikeAlbum.setVisibility(android.view.View.VISIBLE);
            updateLikeButtonUI();
        }

        if (tvPlaylistLabel != null) {
            tvPlaylistLabel.setText("ALBUM");
        }

        if (etSearchSongs != null) {
            etSearchSongs.setHint("Tìm trong album");
        }
    }

    private void setupPlaylistUI() {
        findViewById(R.id.btn_add_song).setVisibility(android.view.View.VISIBLE);
        findViewById(R.id.btn_edit_playlist).setVisibility(android.view.View.VISIBLE);
        findViewById(R.id.btn_more_options).setVisibility(android.view.View.VISIBLE);
        findViewById(R.id.tv_song_section_title).setVisibility(android.view.View.VISIBLE);

        songAdapter.setActionIconMode(SongAdapter.ActionIconMode.DELETE);
        if (tvPlaylistLabel != null) {
            tvPlaylistLabel.setText("PLAYLIST");
        }

        if (tvPlaylistSubtitle != null) {
            tvPlaylistSubtitle.setText("Playlist của bạn");
        }

        if (etSearchSongs != null) {
            etSearchSongs.setHint("Tìm trong playlist");
        }
    }

    private void setupButtons() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.fab_play).setOnClickListener(v -> playSongs(false));
        findViewById(R.id.btn_shuffle).setOnClickListener(v -> playSongs(true));

        if (btnLikeAlbum != null) {
            btnLikeAlbum.setOnClickListener(v -> toggleLikeAlbum());
        }

        findViewById(R.id.btn_add_song).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddSongsToPlaylistActivity.class);
            intent.putExtra("PLAYLIST_ID", currentId);
            startActivity(intent);
        });

        findViewById(R.id.btn_edit_playlist).setOnClickListener(v -> showRenameDialog());
        findViewById(R.id.btn_more_options).setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void toggleLikeAlbum() {
        String userId = tokenManager.getUserId();
        if (userId == null || currentId == null) return;

        new Thread(() -> {
            boolean exists = ServiceLocator.getInstance().likedAlbumRepository.existsByUserIdAndAlbumId(userId, currentId);
            final boolean willLike = !exists;
            if (exists) {
                ServiceLocator.getInstance().likedAlbumRepository.deleteByUserIdAndAlbumId(userId, currentId);
            } else {
                com.ptithcm.waveapp.model.User user = new com.ptithcm.waveapp.model.User();
                user.setId(userId);
                
                Album album = new Album();
                album.setId(currentId);

                com.ptithcm.waveapp.model.LikedAlbum la = new com.ptithcm.waveapp.model.LikedAlbum();
                la.setUser(user);
                la.setAlbum(album);
                la.setAddedAt(java.time.LocalDateTime.now().toString());

                ServiceLocator.getInstance().likedAlbumRepository.save(la);
            }
            runOnUiThread(() -> {
                updateLikeButtonUI();
                Toast.makeText(
                        this,
                        willLike ? "Đã thêm album vào yêu thích" : "Đã xóa album khỏi yêu thích",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }).start();
    }

    private void updateLikeButtonUI() {
        String userId = tokenManager.getUserId();
        if (userId == null || currentId == null || btnLikeAlbum == null) return;

        new Thread(() -> {
            boolean exists = ServiceLocator.getInstance().likedAlbumRepository.existsByUserIdAndAlbumId(userId, currentId);
            runOnUiThread(() -> {
                btnLikeAlbum.setImageResource(exists ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                btnLikeAlbum.setColorFilter(exists ? android.graphics.Color.parseColor("#1DB954") : android.graphics.Color.parseColor("#B3B3B3"));
            });
        }).start();
    }

    private void playSongs(boolean shuffle) {
        if (songList.isEmpty()) {
            Toast.makeText(this, "Playlist trống", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Song> targetList = new ArrayList<>(songList);

        if (shuffle) {
            Collections.shuffle(targetList);
        }

        Intent intent = new Intent(this, MusicPlayerActivity.class);
        intent.putExtra("SONG_DATA", targetList.get(0));
        intent.putExtra("QUEUE_LIST", new ArrayList<>(targetList));
        intent.putExtra("AUTO_PLAY", true);
        intent.putExtra("SHUFFLE_ENABLED", shuffle);
        startActivity(intent);
    }

    private void showRenameDialog() {
        EditText input = new EditText(this);
        input.setText(tvAlbumName.getText());
        input.setPadding(50, 20, 50, 20);

        new AlertDialog.Builder(this)
                .setTitle("Đổi tên playlist")
                .setView(input)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newName = input.getText().toString().trim();

                    if (!newName.isEmpty()) {
                        try {
                            playlistService.renamePlaylist(
                                    currentId,
                                    tokenManager.getUserId(),
                                    newName
                            );

                            tvAlbumName.setText(newName);
                            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa playlist")
                .setMessage("Bạn có chắc muốn xóa playlist này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    try {
                        playlistService.deletePlaylist(
                                currentId,
                                tokenManager.getUserId()
                        );

                        Toast.makeText(this, "Đã xóa playlist", Toast.LENGTH_SHORT).show();
                        finish();

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSongOptions(Song song) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài hát")
                .setMessage("Xóa bài hát \"" + song.getName() + "\" khỏi playlist?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try {
                        playlistService.removeSong(
                                currentId,
                                song.getId(),
                                tokenManager.getUserId()
                        );

                        songList.remove(song);
                        filteredList.remove(song);

                        songAdapter.notifyDataSetChanged();
                        updateMeta();

                        Toast.makeText(this, "Đã xóa khỏi playlist", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadData() {
        try {

            if (isAlbum) {

                Album album = homeService.getAlbumById(currentId);

                tvAlbumName.setText(album.getName());
                if (tvPlaylistSubtitle != null) {
                    String artistName = album.getArtist() != null ? album.getArtist().getName() : "Nghệ sĩ chưa cập nhật";
                    tvPlaylistSubtitle.setText(artistName);
                }

                Glide.with(this)
                        .load(album.getImage())
                        .placeholder(R.drawable.ic_logo)
                        .error(R.drawable.ic_logo)
                        .into(imgAlbumArt);

                updateAlbumMeta(album);

            } else {

                Playlist playlist = playlistService.getPlaylistById(currentId);

                tvAlbumName.setText(playlist.getName());
                if (tvPlaylistSubtitle != null) {
                    tvPlaylistSubtitle.setText("Playlist của bạn");
                }

                Glide.with(this)
                        .load(playlist.getImage())
                        .placeholder(R.drawable.ic_logo)
                        .error(R.drawable.ic_logo)
                        .into(imgAlbumArt);
            }

            List<Song> songs = playlistService.getSongsInPlaylist(currentId);

            songList.clear();
            songList.addAll(songs);

            filterSongs(
                    etSearchSongs != null
                            ? etSearchSongs.getText().toString()
                            : ""
            );

            updateMeta();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Lỗi tải dữ liệu: " + e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void updateMeta() {
        if (tvSongCount == null) return;
        if (isAlbum) {
            return;
        }
        tvSongCount.setText(songList.size() + " bài hát");
    }

    private void updateAlbumMeta(Album album) {
        if (tvSongCount == null || album == null) return;

        List<String> parts = new ArrayList<>();

        if (album.getReleaseDate() != null) {
            parts.add(String.valueOf(album.getReleaseDate().getYear()));
        }

        parts.add(songList.size() + " bài hát");

        if (album.getPlayCount() > 0) {
            parts.add(String.format(Locale.US, "%,d lượt nghe", album.getPlayCount()));
        }

        tvSongCount.setText(String.join(" • ", parts));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentId != null) {
            loadData();
        }
    }
}
