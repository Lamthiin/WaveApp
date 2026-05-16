package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.service.CategoryService;
import java.util.List;

public class SongsByCategoryActivity extends AppCompatActivity {

    private CategoryService categoryService;
    private SongAdapter songAdapter;
    private RecyclerView rvSongs;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_by_category);

        categoryService = ServiceLocator.getInstance().getCategoryService();
        
        tvTitle = findViewById(R.id.tv_category_name);
        rvSongs = findViewById(R.id.rv_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        
        songAdapter = new SongAdapter();
        rvSongs.setAdapter(songAdapter);

        String genreId = getIntent().getStringExtra("GENRE_ID");
        String genreName = getIntent().getStringExtra("GENRE_NAME");
        
        if (genreName != null) tvTitle.setText(genreName);
        
        if (genreId != null) {
            loadSongs(genreId);
        }

        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadSongs(String genreId) {
        List<Song> songs = categoryService.getSongsByCategory(genreId);
        songAdapter.setSongs(songs);
    }
}
