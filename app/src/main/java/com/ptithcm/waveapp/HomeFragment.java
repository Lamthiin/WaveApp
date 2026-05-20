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
import com.ptithcm.waveapp.service.HomeService;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.List;

public class HomeFragment extends Fragment {

    private HomeService homeService;
    private UserProfileService userProfileService;
    private TokenManager tokenManager;

    private LinearLayout albumContainer;
    private LinearLayout artistContainer;
    private LinearLayout genreContainer;
    private LinearLayout chartContainer;
    private ScrollView homeScrollView;
    private RecyclerView rvAllSongs;
    private SongAdapter allSongsAdapter;
    private ImageView imgAvt;
    private ImageView imgLogo;
    private TextView btnFilterAll;
    private TextView btnFilterMusic;

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
        LikedAlbumRepository likedAlbumRepo = new LikedAlbumRepository(db);
        UserFollowArtistRepository followRepo = new UserFollowArtistRepository(db);
        PlaylistRepository playlistRepo = new PlaylistRepository(db);

        homeService = new HomeService(albumRepo, artistRepo, genreRepo, songRepo);
        userProfileService = new UserProfileService(userRepo, likedSongRepo, likedAlbumRepo, followRepo, playlistRepo);
        tokenManager = new TokenManager(requireContext());

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        albumContainer = view.findViewById(R.id.album_container);
        artistContainer = view.findViewById(R.id.artist_container);
        genreContainer = view.findViewById(R.id.genre_container);
        chartContainer = view.findViewById(R.id.chart_container);
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

            String userId = tokenManager.getUserId();
            String avatarUrl = null;
            if (userId != null) {
                try {
                    User user = userProfileService.getProfile(userId);
                    avatarUrl = user != null ? user.getAvatar() : null;
                } catch (Exception ignored) {
                }
            }

            String finalAvatarUrl = avatarUrl;
            requireActivity().runOnUiThread(() -> {
                if (imgAvt != null) {
                    if (finalAvatarUrl != null && !finalAvatarUrl.isEmpty()) {
                        Glide.with(this)
                                .load(finalAvatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar)
                                .into(imgAvt);
                    } else {
                        imgAvt.setImageResource(R.drawable.avatar_default);
                    }
                }

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
                    displaySongs(top50, chartContainer);
                }
            });
        }).start();

        return view;
    }

    private void setupHeaderListeners() {
        if (imgAvt != null) {
            imgAvt.setOnClickListener(v -> startActivity(new Intent(getActivity(), UserProfileActivity.class)));
        }

        if (imgLogo != null) {
            imgLogo.setOnClickListener(v -> Toast.makeText(getContext(), "Wave Music App", Toast.LENGTH_SHORT).show());
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
            rvAllSongs.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAllSongs.setAdapter(allSongsAdapter);

            allSongsAdapter.setOnSongClickListener(song -> {
                Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
                intent.putExtra("SONG_ID", song.getId());
                startActivity(intent);
            });
        }
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
        for (Album album : albums) {
            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_album, albumContainer, false);
            ((TextView) item.findViewById(R.id.tvAlbumName)).setText(album.getName());
            if (album.getArtist() != null) {
                ((TextView) item.findViewById(R.id.tvArtistName)).setText(album.getArtist().getName());
            }

            Glide.with(this)
                    .load(album.getImage())
                    .placeholder(R.drawable.ic_logo)
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
        for (Artist artist : artists) {
            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_artist, artistContainer, false);
            ((TextView) item.findViewById(R.id.tvArtistName)).setText(artist.getName());

            Glide.with(this)
                    .load(artist.getImage())
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar)
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
        if (genres == null) return;

        requireActivity().runOnUiThread(() -> {
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
                View item = LayoutInflater.from(getContext()).inflate(R.layout.item_genre, currentRow, false);

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) item.getLayoutParams();
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

                ((TextView) item.findViewById(R.id.tvGenreName)).setText(genre.getName());

                item.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), SongsByCategoryActivity.class);
                    intent.putExtra("GENRE_ID", genre.getId());
                    intent.putExtra("GENRE_NAME", genre.getName());
                    startActivity(intent);
                });

                if (currentRow != null) {
                    currentRow.addView(item);
                }
            }
        });
    }

    private void displaySongs(List<Song> songs, LinearLayout container) {
        container.removeAllViews();
        int[] colors = {0xFFE94435, 0xFF8E24AA, 0xFF2196F3, 0xFF4CAF50};
        int index = 0;

        for (Song song : songs) {
            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_chart, container, false);

            TextView tvTitle = item.findViewById(R.id.tvChartTitle);
            TextView tvDesc = item.findViewById(R.id.tvChartDesc);
            com.google.android.material.card.MaterialCardView card = item.findViewById(R.id.cardChart);

            tvTitle.setText(song.getName().toUpperCase());
            if (song.getArtist() != null) {
                tvDesc.setText("Boi " + song.getArtist().getName());
            }

            card.setCardBackgroundColor(colors[index % colors.length]);
            index++;

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
                intent.putExtra("SONG_ID", song.getId());
                startActivity(intent);
            });
            container.addView(item);

            if (index >= 10) {
                break;
            }
        }
    }
}
