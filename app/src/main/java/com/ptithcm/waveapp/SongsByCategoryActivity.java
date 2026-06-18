package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.LikedSong;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.service.CategoryService;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class SongsByCategoryActivity extends BaseMiniPlayerActivity {

    private static final int PAGE_SIZE = 10;

    private CategoryService categoryService;
    private PlaylistService playlistService;
    private TokenManager tokenManager;
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
        playlistService = ServiceLocator.getInstance().getPlaylistService();
        tokenManager = new TokenManager(this);

        tvTitle = findViewById(R.id.tv_category_name);
        tvSongCount = findViewById(R.id.tv_song_count);
        tvPageInfo = findViewById(R.id.tv_page_info);
        imgCategoryBanner = findViewById(R.id.img_category_banner);

        btnPrevPage = findViewById(R.id.btn_prev_page);
        btnNextPage = findViewById(R.id.btn_next_page);

        rvSongs = findViewById(R.id.rv_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        songAdapter = new SongAdapter();
        songAdapter.setActionIconMode(SongAdapter.ActionIconMode.MORE);
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
        songAdapter.setOnMoreClickListener((song, position, anchor) -> showSongActions(song, position));

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

    private void showSongActions(Song song, int position) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_search_song_actions, null, false);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);

        TextView tvSongName = dialogView.findViewById(R.id.tvSearchSongActionTitle);
        View optionFavorite = dialogView.findViewById(R.id.optionAddSongFavorite);
        View optionPlaylist = dialogView.findViewById(R.id.optionAddSongPlaylist);
        View optionCancel = dialogView.findViewById(R.id.tvSearchSongActionCancel);

        tvSongName.setText(song.getName());
        optionFavorite.setOnClickListener(v -> {
            dialog.dismiss();
            addSongToFavorites(song, position);
        });
        optionPlaylist.setOnClickListener(v -> {
            dialog.dismiss();
            showPlaylistPickerSheet(song);
        });
        optionCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void addSongToFavorites(Song song, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            boolean currentlyLiked = ServiceLocator.getInstance()
                    .likedSongRepository
                    .existsByUserIdAndSongId(userId, song.getId());
            if (currentlyLiked) {
                runOnUiThread(() -> Toast.makeText(this, "Bài hát đã có trong yêu thích", Toast.LENGTH_SHORT).show());
                return;
            }

            LikedSong likedSong = LikedSong.builder()
                    .user(com.ptithcm.waveapp.model.User.builder().id(userId).build())
                    .song(song)
                    .likedAt(LocalDateTime.now().toString())
                    .build();

            ServiceLocator.getInstance().likedSongRepository.save(likedSong);
            ServiceLocator.getInstance().songRepository.incrementLikeCount(song.getId());
            song.setLikeCount(song.getLikeCount() + 1);

            runOnUiThread(() -> {
                songAdapter.notifyItemChanged(position);
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showPlaylistPickerSheet(Song song) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            List<com.ptithcm.waveapp.model.Playlist> playlists = playlistService.getMyPlaylists(userId);
            runOnUiThread(() -> {
                if (playlists.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa có playlist nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                View dialogView = LayoutInflater.from(this)
                        .inflate(R.layout.dialog_search_playlist_picker, null, false);

                BottomSheetDialog dialog = new BottomSheetDialog(this);
                dialog.setContentView(dialogView);

                LinearLayout container = dialogView.findViewById(R.id.layoutPlaylistOptions);
                View cancelView = dialogView.findViewById(R.id.tvSearchPlaylistCancel);

                for (com.ptithcm.waveapp.model.Playlist playlist : playlists) {
                    TextView optionView = new TextView(this);
                    optionView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(50)
                    ));
                    optionView.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    optionView.setPadding(dpToPx(24), 0, dpToPx(24), 0);
                    optionView.setText(playlist.getName());
                    optionView.setTextColor(getColor(R.color.white));
                    optionView.setTextSize(18);
                    optionView.setOnClickListener(v -> {
                        dialog.dismiss();
                        addSongToPlaylist(playlist.getId(), song);
                    });
                    container.addView(optionView);
                }

                cancelView.setOnClickListener(v -> dialog.dismiss());
                dialog.show();
            });
        }).start();
    }

    private void addSongToPlaylist(String playlistId, Song song) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        new Thread(() -> {
            try {
                playlistService.addSong(playlistId, song.getId(), userId);
                runOnUiThread(() -> Toast.makeText(this, "Đã thêm vào playlist", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
