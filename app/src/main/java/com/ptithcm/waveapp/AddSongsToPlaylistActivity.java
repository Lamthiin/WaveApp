package com.ptithcm.waveapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình tìm kiếm và thêm bài hát vào playlist cá nhân.
 */
public class AddSongsToPlaylistActivity extends AppCompatActivity {

    private PlaylistService playlistService;
    private TokenManager    tokenManager;
    private String          playlistId;

    private EditText     etSearch;
    private RecyclerView rvResults;
    private ProgressBar  progressBar;
    private SongAdapter  songAdapter;
    private final List<Song> songList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs_to_playlist);

        playlistService = ServiceLocator.getInstance().getPlaylistService();
        tokenManager    = new TokenManager(this);
        playlistId      = getIntent().getStringExtra("PLAYLIST_ID");

        if (playlistId == null) {
            Toast.makeText(this, "Thiếu ID Playlist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSearch();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Load ban đầu (tất cả bài hát chưa có trong playlist)
        searchSongs("");
    }

    private void initViews() {
        etSearch    = findViewById(R.id.et_search);
        rvResults   = findViewById(R.id.rv_search_results);
        progressBar = findViewById(R.id.progress_loading);
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter(songList);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(songAdapter);

        // Click vào dấu "+" hoặc nút More để thêm bài
        songAdapter.setOnMoreClickListener((song, position) -> {
            addSongToPlaylist(song, position);
        });
        
        // Hoặc click vào item cũng thêm luôn (tùy UX)
        songAdapter.setOnSongClickListener(song -> {
            // Có thể mở Player nghe thử hoặc thêm luôn
            Toast.makeText(this, "Nhấn icon bên phải để thêm vào playlist", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSongs(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchSongs(String keyword) {
        progressBar.setVisibility(View.VISIBLE);
        try {
            List<Song> available = playlistService.getAvailableSongs(playlistId, tokenManager.getUserId(), keyword);
            songList.clear();
            songList.addAll(available);
            songAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void addSongToPlaylist(Song song, int position) {
        try {
            playlistService.addSong(playlistId, song.getId(), tokenManager.getUserId());
            Toast.makeText(this, "Đã thêm " + song.getName(), Toast.LENGTH_SHORT).show();
            
            // Xóa khỏi danh sách kết quả tìm kiếm sau khi thêm thành công
            songList.remove(position);
            songAdapter.notifyItemRemoved(position);
            
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
