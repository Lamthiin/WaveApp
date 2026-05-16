package com.ptithcm.waveapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

/**
 * Hiển thị chi tiết Album hoặc Playlist cá nhân.
 */
public class PlaylistDetailActivity extends AppCompatActivity {

    private HomeService     homeService;
    private PlaylistService playlistService;
    private TokenManager    tokenManager;

    private ImageView    imgAlbumArt, btnLikeMain;
    private TextView     tvAlbumName, tvArtistName, tvSongCount;
    private EditText     etSearchSongs;
    private RecyclerView rvSongs;
    private SongAdapter  songAdapter;
    private final List<Song> songList = new ArrayList<>();
    private final List<Song> filteredList = new ArrayList<>();

    private String  currentId;
    private boolean isAlbum = false;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        // Khởi tạo services
        homeService     = ServiceLocator.getInstance().getHomeService();
        playlistService = ServiceLocator.getInstance().getPlaylistService();
        tokenManager    = new TokenManager(this);

        // Ánh xạ View
        initViews();

        // Cấu hình RecyclerView
        setupRecyclerView();

        // Nhận dữ liệu từ Intent
        handleIntent();

        // Gán sự kiện cho các nút
        setupButtons();
    }

    private void initViews() {
        imgAlbumArt  = findViewById(R.id.img_playlist_cover);
        tvAlbumName  = findViewById(R.id.tv_playlist_name);
        tvArtistName = findViewById(R.id.tv_owner);
        tvSongCount  = findViewById(R.id.tv_playlist_meta);
        etSearchSongs = findViewById(R.id.et_search_songs);
        rvSongs      = findViewById(R.id.rv_playlist_songs);
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter(filteredList);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(songAdapter);

        // Click vào bài hát -> Mở Player
        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });
        
        // Có thể thêm OnMoreClickListener cho bài hát ở đây nếu cần (ví dụ xóa bài khỏi playlist)
        songAdapter.setOnMoreClickListener((song, position) -> {
            if (!isAlbum) {
                showSongOptions(song, position);
            }
        });

        // Setup search listener
        if (etSearchSongs != null) {
            etSearchSongs.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterSongs(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void filterSongs(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(songList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Song song : songList) {
                if (song.getName().toLowerCase().contains(lowerQuery) || 
                   (song.getArtist() != null && song.getArtist().getName().toLowerCase().contains(lowerQuery))) {
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
        } else {
            currentId = getIntent().getStringExtra("PLAYLIST_ID");
            if (currentId != null) {
                isAlbum = false;
                loadData();
                setupPlaylistUI();
            } else {
                Toast.makeText(this, "Không tìm thấy nội dung", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupAlbumUI() {
        findViewById(R.id.btn_add_song).setVisibility(View.GONE);
        findViewById(R.id.btn_edit_playlist).setVisibility(View.GONE);
        findViewById(R.id.btn_more_options).setVisibility(View.GONE);
    }

    private void setupPlaylistUI() {
        findViewById(R.id.btn_add_song).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_edit_playlist).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_more_options).setVisibility(View.VISIBLE);
    }

    private void setupButtons() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Nút Play chính
        findViewById(R.id.fab_play).setOnClickListener(v -> playSongs(false));

        // Nút Shuffle
        findViewById(R.id.btn_shuffle).setOnClickListener(v -> playSongs(true));

        // Thêm bài hát (chỉ cho Playlist)
        findViewById(R.id.btn_add_song).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddSongsToPlaylistActivity.class);
            intent.putExtra("PLAYLIST_ID", currentId);
            startActivity(intent);
        });

        // Sửa tên playlist
        findViewById(R.id.btn_edit_playlist).setOnClickListener(v -> showRenameDialog());

        // Chia sẻ
        findViewById(R.id.btn_share).setOnClickListener(v -> shareContent());

        // More options (Xóa playlist)
        findViewById(R.id.btn_more_options).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Xóa playlist");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Xóa playlist")) {
                    showDeleteConfirmDialog();
                }
                return true;
            });
            popup.show();
        });
    }

    private void playSongs(boolean shuffle) {
        if (songList.isEmpty()) {
            Toast.makeText(this, "Danh sách trống", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Song> targetList = new ArrayList<>(songList);
        if (shuffle) Collections.shuffle(targetList);
        
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        intent.putExtra("SONG_DATA", targetList.get(0));
        // Nếu player hỗ trợ nhận playlist thì truyền thêm targetList
        startActivity(intent);
    }

    private void shareContent() {
        String shareText = "Nghe thử " + (isAlbum ? "Album: " : "Playlist: ") + tvAlbumName.getText() + " trên WaveApp!";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
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
                            playlistService.renamePlaylist(currentId, tokenManager.getUserId(), newName);
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
                        playlistService.deletePlaylist(currentId, tokenManager.getUserId());
                        Toast.makeText(this, "Đã xóa playlist", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSongOptions(Song song, int position) {
        PopupMenu popup = new PopupMenu(this, rvSongs.getChildAt(position));
        popup.getMenu().add("Xóa khỏi playlist");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Xóa khỏi playlist")) {
                playlistService.removeSong(currentId, song.getId(), tokenManager.getUserId());
                songList.remove(position);
                songAdapter.notifyItemRemoved(position);
                updateMeta();
            }
            return true;
        });
        popup.show();
    }

    private void loadData() {
        try {
            if (isAlbum) {
                Album album = homeService.getAlbumById(currentId);
                tvAlbumName.setText(album.getName());
                tvArtistName.setText(album.getArtist() != null ? album.getArtist().getName() : "Unknown Artist");
                Glide.with(this).load(album.getImage()).placeholder(R.drawable.ic_logo).into(imgAlbumArt);
            } else {
                Playlist playlist = playlistService.getPlaylistById(currentId);
                tvAlbumName.setText(playlist.getName());
                tvArtistName.setText(playlist.getUser() != null ? playlist.getUser().getName() : "Unknown User");
                Glide.with(this).load(playlist.getImage()).placeholder(R.drawable.ic_logo).into(imgAlbumArt);
            }

            List<Song> songs = playlistService.getSongsInPlaylist(currentId);
            songList.clear();
            songList.addAll(songs);
            filterSongs(etSearchSongs != null ? etSearchSongs.getText().toString() : "");
            updateMeta();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMeta() {
        tvSongCount.setText(songList.size() + " bài hát");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data khi quay lại từ màn hình Add Song
        if (currentId != null) loadData();
    }
}
