package com.ptithcm.waveapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Genre;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.GenreRepository;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.PlaylistRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.ServiceLocator;
import com.ptithcm.waveapp.service.HomeService;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeService homeService;
    private UserProfileService userProfileService;
    private TokenManager tokenManager;
    private LikedSongRepository likedSongRepository;
    private PlaylistService playlistService;

    private LinearLayout albumContainer;
    private LinearLayout artistContainer;
    private LinearLayout genreContainer;
    private LinearLayout chartContainer;
    private TextView btnChartToggle;
    private View chartSectionCard;

    private ScrollView homeScrollView;
    private RecyclerView rvAllSongs;

    private SongAdapter allSongsAdapter;

    private ImageView imgAvt;
    private ImageView imgLogo;

    private TextView btnFilterAll;
    private TextView btnFilterMusic;
    private final List<Song> topChartSongs = new ArrayList<>();
    private boolean chartsExpanded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());

        AlbumRepository albumRepo = new AlbumRepository(db);
        ArtistRepository artistRepo = new ArtistRepository(db);
        GenreRepository genreRepo = new GenreRepository(db);
        SongRepository songRepo = new SongRepository(db);
        UserRepository userRepo = new UserRepository(db);

        LikedSongRepository likedSongRepo = new LikedSongRepository(db);
        likedSongRepository = likedSongRepo;
        LikedAlbumRepository likedAlbumRepo = new LikedAlbumRepository(db);
        UserFollowArtistRepository followRepo = new UserFollowArtistRepository(db);
        PlaylistRepository playlistRepo = new PlaylistRepository(db);

        homeService = new HomeService(albumRepo, artistRepo, genreRepo, songRepo);

        userProfileService = new UserProfileService(
                userRepo,
                likedSongRepo,
                likedAlbumRepo,
                followRepo,
                playlistRepo,
                songRepo,
                albumRepo
        );

        playlistService = ServiceLocator.getInstance().getPlaylistService();
        tokenManager = new TokenManager(requireContext());

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        albumContainer = view.findViewById(R.id.album_container);
        artistContainer = view.findViewById(R.id.artist_container);
        genreContainer = view.findViewById(R.id.genre_container);
        chartContainer = view.findViewById(R.id.chart_container);
        btnChartToggle = view.findViewById(R.id.btn_chart_toggle);
        chartSectionCard = view.findViewById(R.id.chart_section_card);
        homeScrollView = view.findViewById(R.id.home_scroll_view);
        rvAllSongs = view.findViewById(R.id.rv_all_songs);
        imgAvt = view.findViewById(R.id.img_avt);
        imgLogo = view.findViewById(R.id.img_logo);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterMusic = view.findViewById(R.id.btn_filter_music);

        setupRecyclerView();
        setupHeaderListeners();

        new Thread(() -> {
            List<Album> albums = homeService.getFeaturedAlbums();
            List<Artist> artists = homeService.getFeaturedArtists();
            List<Genre> genres = homeService.getCategories();
            List<Song> top50 = homeService.getTop50();

            requireActivity().runOnUiThread(() -> {
                reloadUserAvatar();

                if (albums != null && albumContainer != null) {
                    displayAlbums(albums);
                }

                if (artists != null && artistContainer != null) {
                    displayArtists(artists);
                }

                if (genres != null && genreContainer != null) {
                    displayGenres(genres);
                }

                if (top50 != null && chartContainer != null) {
                    setupChartSection(top50);
                }
            });

        }).start();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadUserAvatar();
    }

    private void reloadUserAvatar() {
        String userId = tokenManager.getUserId();

        if (userId == null || imgAvt == null) {
            return;
        }

        new Thread(() -> {
            try {
                User user = userProfileService.getProfile(userId);

                requireActivity().runOnUiThread(() -> {
                    if (user != null &&
                            user.getAvatar() != null &&
                            !user.getAvatar().isEmpty()) {

                        Glide.with(this)
                                .load(user.getAvatar())
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar)
                                .error(R.drawable.avatar_default)
                                .into(imgAvt);

                    } else {
                        imgAvt.setImageResource(R.drawable.avatar_default);
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        imgAvt.setImageResource(R.drawable.avatar_default)
                );
            }
        }).start();
    }

    private void setupHeaderListeners() {
        if (imgAvt != null) {
            imgAvt.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), UserProfileActivity.class)));
        }

        if (imgLogo != null) {
            imgLogo.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Wave Music App", Toast.LENGTH_SHORT).show());
        }

        if (btnFilterAll != null) {
            btnFilterAll.setOnClickListener(v -> updateFilterUI(btnFilterAll));
        }

        if (btnFilterMusic != null) {
            btnFilterMusic.setOnClickListener(v -> updateFilterUI(btnFilterMusic));
        }
    }

    private void setupRecyclerView() {
        if (rvAllSongs != null) {
            allSongsAdapter = new SongAdapter();
            allSongsAdapter.setShowIndex(false);
            allSongsAdapter.setActionIconMode(SongAdapter.ActionIconMode.MORE);

            rvAllSongs.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAllSongs.setAdapter(allSongsAdapter);

            allSongsAdapter.setOnSongClickListener(song -> {
                Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
                intent.putExtra("SONG_ID", song.getId());
                intent.putExtra("SONG_DATA", song);
                intent.putExtra("QUEUE_LIST", new java.util.ArrayList<>(allSongsAdapter.getSongs()));
                startActivity(intent);
            });

            allSongsAdapter.setOnMoreClickListener((song, position, anchor) ->
                    showSongActionSheet(song, position));
        }
    }

    private void showSongActionSheet(Song song, int position) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_search_song_actions, null, false);
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        dialog.setContentView(dialogView);

        TextView tvSongName = dialogView.findViewById(R.id.tvSearchSongActionTitle);
        View optionFavorite = dialogView.findViewById(R.id.optionAddSongFavorite);
        View optionPlaylist = dialogView.findViewById(R.id.optionAddSongPlaylist);
        View optionCancel = dialogView.findViewById(R.id.tvSearchSongActionCancel);

        tvSongName.setText(song.getName());
        optionFavorite.setOnClickListener(v -> { dialog.dismiss(); addSongToFavorites(song, position); });
        optionPlaylist.setOnClickListener(v -> { dialog.dismiss(); showPlaylistPickerSheet(song); });
        optionCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void addSongToFavorites(Song song, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            boolean alreadyLiked = likedSongRepository.existsByUserIdAndSongId(userId, song.getId());
            if (alreadyLiked) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Bài hát đã có trong yêu thích", Toast.LENGTH_SHORT).show());
                return;
            }
            com.ptithcm.waveapp.model.LikedSong likedSong = com.ptithcm.waveapp.model.LikedSong.builder()
                    .user(User.builder().id(userId).build())
                    .song(song)
                    .likedAt(java.time.LocalDateTime.now().toString())
                    .build();
            likedSongRepository.save(likedSong);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                allSongsAdapter.notifyItemChanged(position);
                Toast.makeText(getContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showPlaylistPickerSheet(Song song) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            java.util.List<com.ptithcm.waveapp.model.Playlist> playlists = playlistService.getMyPlaylists(userId);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (playlists.isEmpty()) {
                    Toast.makeText(getContext(), "Bạn chưa có playlist nào", Toast.LENGTH_SHORT).show();
                    return;
                }
                View dialogView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.dialog_search_playlist_picker, null, false);
                com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                        new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
                dialog.setContentView(dialogView);
                LinearLayout container = dialogView.findViewById(R.id.layoutPlaylistOptions);
                View cancelView = dialogView.findViewById(R.id.tvSearchPlaylistCancel);
                for (com.ptithcm.waveapp.model.Playlist playlist : playlists) {
                    TextView optionView = new TextView(requireContext());
                    optionView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(50)));
                    optionView.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    optionView.setPadding(dpToPx(24), 0, dpToPx(24), 0);
                    optionView.setText(playlist.getName());
                    optionView.setTextColor(requireContext().getColor(R.color.white));
                    optionView.setTextSize(16);
                    optionView.setOnClickListener(v -> {
                        dialog.dismiss();
                        new Thread(() -> {
                            try {
                                playlistService.addSong(playlist.getId(), song.getId(), userId);
                                if (getActivity() == null) return;
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Đã thêm vào playlist", Toast.LENGTH_SHORT).show());
                            } catch (Exception e) {
                                if (getActivity() == null) return;
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }).start();
                    });
                    container.addView(optionView);
                }
                cancelView.setOnClickListener(v -> dialog.dismiss());
                dialog.show();
            });
        }).start();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void updateFilterUI(TextView selected) {
        if (btnFilterAll != null) {
            btnFilterAll.setBackgroundResource(R.drawable.bg_chip_unselected);
            btnFilterAll.setTextColor(Color.WHITE);
        }

        if (btnFilterMusic != null) {
            btnFilterMusic.setBackgroundResource(R.drawable.bg_chip_unselected);
            btnFilterMusic.setTextColor(Color.WHITE);
        }

        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        selected.setTextColor(Color.BLACK);

        if (selected == btnFilterMusic) {
            if (homeScrollView != null) {
                homeScrollView.setVisibility(View.GONE);
            }

            if (rvAllSongs != null) {
                rvAllSongs.setVisibility(View.VISIBLE);
                loadAllSongs();
            }

        } else {
            if (homeScrollView != null) {
                homeScrollView.setVisibility(View.VISIBLE);
            }

            if (rvAllSongs != null) {
                rvAllSongs.setVisibility(View.GONE);
            }
        }
    }

    private void loadAllSongs() {
        new Thread(() -> {
            List<Song> songs = homeService.getAllSongs();

            requireActivity().runOnUiThread(() -> {
                if (allSongsAdapter != null) {
                    allSongsAdapter.setSongs(songs);
                }
            });
        }).start();
    }

    private void displayAlbums(List<Album> albums) {
        albumContainer.removeAllViews();

        int itemWidth = (int) (150 * getResources().getDisplayMetrics().density);

        for (Album album : albums) {
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_album, albumContainer, false);

            ViewGroup.LayoutParams lp = item.getLayoutParams();
            lp.width = itemWidth;
            item.setLayoutParams(lp);

            ((TextView) item.findViewById(R.id.tvAlbumName)).setText(album.getName());

            if (album.getArtist() != null) {
                ((TextView) item.findViewById(R.id.tvArtistName))
                        .setText(album.getArtist().getName());
            }

            Glide.with(this)
                    .load(album.getImage())
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .centerCrop()
                    .into((ImageView) item.findViewById(R.id.imgAlbum));

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
                intent.putExtra("ALBUM_ID", album.getId());
                startActivity(intent);
            });

            albumContainer.addView(item);
        }
    }

    private void displayArtists(List<Artist> artists) {
        artistContainer.removeAllViews();

        int itemWidth = (int) (150 * getResources().getDisplayMetrics().density);

        for (Artist artist : artists) {
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_artist, artistContainer, false);

            ViewGroup.LayoutParams lp = item.getLayoutParams();
            lp.width = itemWidth;
            item.setLayoutParams(lp);

            ((TextView) item.findViewById(R.id.tvArtistName)).setText(artist.getName());

            Glide.with(this)
                    .load(artist.getImage())
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.avatar_default)
                    .into((ImageView) item.findViewById(R.id.imgArtist));

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
                intent.putExtra("ARTIST_ID", artist.getId());
                startActivity(intent);
            });

            artistContainer.addView(item);
        }
    }

    private void displayGenres(List<Genre> genres) {
        if (genres == null || genreContainer == null) return;

        genreContainer.removeAllViews();

        LinearLayout currentRow = null;

        for (int i = 0; i < genres.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                genreContainer.addView(currentRow);
            }

            Genre genre = genres.get(i);

            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_genre, currentRow, false);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) item.getLayoutParams();

            layoutParams.width = 0;
            layoutParams.weight = 1;
            layoutParams.topMargin = 6;
            layoutParams.bottomMargin = 6;

            if (i % 2 == 0) {
                layoutParams.setMarginStart(0);
                layoutParams.setMarginEnd(6);
            } else {
                layoutParams.setMarginStart(6);
                layoutParams.setMarginEnd(0);
            }

            item.setLayoutParams(layoutParams);

            TextView tvGenreName = item.findViewById(R.id.tvGenreName);
            ImageView imgGenre = item.findViewById(R.id.imgGenre);

            if (tvGenreName != null) {
                tvGenreName.setText(genre.getName());
            }

            if (imgGenre != null) {
                Glide.with(this)
                        .load(genre.getImageUrl())
                        .placeholder(R.drawable.ic_logo)
                        .error(R.drawable.ic_logo)
                        .centerCrop()
                        .into(imgGenre);
            }

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SongsByCategoryActivity.class);
                intent.putExtra("GENRE_ID", genre.getId());
                intent.putExtra("GENRE_NAME", genre.getName());
                intent.putExtra("GENRE_IMAGE_URL", genre.getImageUrl());
                startActivity(intent);
            });

            if (currentRow != null) {
                currentRow.addView(item);
            }
        }
    }

    private void setupChartSection(List<Song> songs) {
        topChartSongs.clear();
        topChartSongs.addAll(songs);
        chartsExpanded = false;

        if (btnChartToggle != null) {
            btnChartToggle.setOnClickListener(v -> {
                boolean wasExpanded = chartsExpanded;
                chartsExpanded = !chartsExpanded;
                displaySongs(topChartSongs, chartContainer);
                if (wasExpanded && !chartsExpanded) {
                    focusChartSectionTop();
                }
            });
        }

        displaySongs(topChartSongs, chartContainer);
    }

    private void displaySongs(List<Song> songs, LinearLayout container) {
        container.removeAllViews();
        if (songs == null || songs.isEmpty()) {
            if (btnChartToggle != null) {
                btnChartToggle.setVisibility(View.GONE);
            }
            return;
        }

        int visibleCount = chartsExpanded ? Math.min(10, songs.size()) : Math.min(5, songs.size());

        for (int index = 0; index < visibleCount; index++) {
            Song song = songs.get(index);
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_home_chart_song, container, false);

            TextView tvIndex = item.findViewById(R.id.tvChartIndex);
            TextView tvTitle = item.findViewById(R.id.tvChartSongTitle);
            TextView tvArtist = item.findViewById(R.id.tvChartSongArtist);
            TextView tvPlays = item.findViewById(R.id.tvChartSongPlays);
            ImageView imgSong = item.findViewById(R.id.imgChartSong);

            tvIndex.setText(String.valueOf(index + 1));
            tvTitle.setText(song.getName());
            tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "");
            tvPlays.setText(formatPlayCount(song.getPlayCount()) + " lượt nghe");

            Glide.with(this)
                    .load(song.getImage())
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .centerCrop()
                    .into(imgSong);

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
                intent.putExtra("SONG_ID", song.getId());
                intent.putExtra("SONG_DATA", song);
                intent.putExtra("QUEUE_LIST", new java.util.ArrayList<>(songs));
                startActivity(intent);
            });

            container.addView(item);
        }

        if (btnChartToggle != null) {
            boolean canExpand = songs.size() > 5;
            btnChartToggle.setVisibility(canExpand ? View.VISIBLE : View.GONE);
            btnChartToggle.setText(chartsExpanded ? "Thu gọn" : "Xem thêm");
        }
    }

    private String formatPlayCount(long playCount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(playCount);
    }

    private void focusChartSectionTop() {
        if (homeScrollView == null || chartSectionCard == null) {
            return;
        }
        homeScrollView.post(() -> homeScrollView.smoothScrollTo(0, chartSectionCard.getTop()));
    }
}
