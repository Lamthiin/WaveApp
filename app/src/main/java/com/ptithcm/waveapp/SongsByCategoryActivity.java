package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.service.CategoryService;

import java.util.ArrayList;
import java.util.List;

public class SongsByCategoryActivity extends BaseMiniPlayerActivity {

    private static final int PAGE_SIZE = 10;

    private CategoryService categoryService;
    private SongAdapter songAdapter;

    private RecyclerView rvSongs;
    private TextView tvTitle, tvSongCount, tvPageInfo;
    private ImageView imgCategoryBanner;
    private ImageButton btnPrevPage, btnNextPage;

    private String genreId;
    private String genreImageUrl;

    private List<Song> allSongs = new ArrayList<>();

    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_by_category);

        categoryService = ServiceLocator.getInstance().getCategoryService();

        tvTitle = findViewById(R.id.tv_category_name);
        tvSongCount = findViewById(R.id.tv_song_count);
        tvPageInfo = findViewById(R.id.tv_page_info);
        imgCategoryBanner = findViewById(R.id.img_category_banner);

        btnPrevPage = findViewById(R.id.btn_prev_page);
        btnNextPage = findViewById(R.id.btn_next_page);

        rvSongs = findViewById(R.id.rv_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        songAdapter = new SongAdapter();
        rvSongs.setAdapter(songAdapter);

        genreId = getIntent().getStringExtra("GENRE_ID");
        String genreName = getIntent().getStringExtra("GENRE_NAME");
        genreImageUrl = getIntent().getStringExtra("GENRE_IMAGE_URL");

        if (genreName != null) {
            tvTitle.setText(genreName);
        }

        loadCategoryImage();

        if (genreId != null) {
            loadSongs(genreId);
        }

        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            intent.putExtra("QUEUE_LIST", new ArrayList<>(allSongs));
            startActivity(intent);
        });

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                showCurrentPage();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                showCurrentPage();
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadCategoryImage() {
        if (imgCategoryBanner == null) return;

        Glide.with(this)
                .load(genreImageUrl)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .fitCenter()
                .into(imgCategoryBanner);
    }

    private void loadSongs(String genreId) {
        allSongs = categoryService.getSongsByCategory(genreId);

        if (allSongs == null) {
            allSongs = new ArrayList<>();
        }

        currentPage = 1;
        totalPages = Math.max(1, (int) Math.ceil(allSongs.size() * 1.0 / PAGE_SIZE));

        tvSongCount.setText(allSongs.size() + " bài hát");

        showCurrentPage();
    }

    private void showCurrentPage() {
        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allSongs.size());

        List<Song> pageSongs;

        if (allSongs.isEmpty()) {
            pageSongs = new ArrayList<>();
        } else {
            pageSongs = allSongs.subList(fromIndex, toIndex);
        }

        songAdapter.setSongs(pageSongs);

        tvPageInfo.setText("Trang " + currentPage + " / " + totalPages);

        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);

        btnPrevPage.setAlpha(currentPage > 1 ? 1f : 0.35f);
        btnNextPage.setAlpha(currentPage < totalPages ? 1f : 0.35f);

        rvSongs.scrollToPosition(0);
    }
}