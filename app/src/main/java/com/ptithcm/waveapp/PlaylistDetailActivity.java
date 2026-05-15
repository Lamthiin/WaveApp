package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.config.ServiceLocator;
import com.ptithcm.waveapp.controller.AlbumController;
import com.ptithcm.waveapp.dto.response.AlbumResponse;
import com.ptithcm.waveapp.dto.response.ApiResponse;
import com.ptithcm.waveapp.dto.response.SongResponse;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity {

    private AlbumController albumController;
    private ImageView imgAlbumArt;
    private TextView tvAlbumName, tvArtistName, tvSongCount;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<SongResponse> songList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        albumController = ServiceLocator.getInstance().getAlbumController();
        
        // Ánh xạ View chính xác theo activity_playlist_detail.xml
        imgAlbumArt = findViewById(R.id.img_playlist_cover);
        tvAlbumName = findViewById(R.id.tv_playlist_name);
        tvArtistName = findViewById(R.id.tv_owner);
        tvSongCount = findViewById(R.id.tv_playlist_meta);
        rvSongs = findViewById(R.id.rv_playlist_songs);

        // Thiết lập RecyclerView
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(songList, song -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });
        rvSongs.setAdapter(songAdapter);

        String albumId = getIntent().getStringExtra("ALBUM_ID");
        loadAlbumDetail(albumId != null ? albumId : "album-1");
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadAlbumDetail(String albumId) {
        ApiResponse<AlbumResponse> response = albumController.getAlbumDetail(albumId, null);
        
        if (response.isSuccess()) {
            AlbumResponse album = response.getData();
            tvAlbumName.setText(album.getName());
            tvArtistName.setText(album.getArtistName());
            tvSongCount.setText(album.getSongCount() + " bài hát");
            
            Glide.with(this).load(album.getImage()).placeholder(R.drawable.ic_logo).into(imgAlbumArt);
            
            songList.clear();
            songList.addAll(album.getSongs());
            songAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
