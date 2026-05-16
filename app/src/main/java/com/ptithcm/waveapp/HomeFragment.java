package com.ptithcm.waveapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;                    // FIX 2: dùng Glide thay ImageFileHelper
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.*;
import com.ptithcm.waveapp.repository.*;
import com.ptithcm.waveapp.service.HomeService;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeService        homeService;
    private UserProfileService userProfileService;
    private TokenManager       tokenManager;

    private LinearLayout albumContainer, artistContainer, genreContainer, chartContainer;
    private ScrollView   homeScrollView;
    private RecyclerView rvAllSongs;
    private SongAdapter  allSongsAdapter;
    private ImageView    imgAvt, imgLogo;
    private TextView     btnFilterAll, btnFilterMusic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // FIX 1: Khởi tạo trực tiếp, không dùng ServiceLocator
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        AlbumRepository  albumRepo  = new AlbumRepository(db);
        ArtistRepository artistRepo = new ArtistRepository(db);
        GenreRepository  genreRepo  = new GenreRepository(db);
        SongRepository   songRepo   = new SongRepository(db);
        UserRepository   userRepo   = new UserRepository(db);
        LikedSongRepository       likedSongRepo  = new LikedSongRepository(db);
        LikedAlbumRepository      likedAlbumRepo = new LikedAlbumRepository(db);
        UserFollowArtistRepository followRepo     = new UserFollowArtistRepository(db);
        PlaylistRepository         playlistRepo   = new PlaylistRepository(db);

        homeService = new HomeService(albumRepo, artistRepo, genreRepo, songRepo);
        userProfileService = new UserProfileService(userRepo, likedSongRepo,
                likedAlbumRepo, followRepo, playlistRepo);
        tokenManager = new TokenManager(requireContext());

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        albumContainer  = view.findViewById(R.id.album_container);
        artistContainer = view.findViewById(R.id.artist_container);
        genreContainer  = view.findViewById(R.id.genre_container);
        chartContainer  = view.findViewById(R.id.chart_container);
        homeScrollView  = view.findViewById(R.id.home_scroll_view);
        rvAllSongs      = view.findViewById(R.id.rv_all_songs);
        imgAvt          = view.findViewById(R.id.img_avt);
        imgLogo         = view.findViewById(R.id.img_logo);
        btnFilterAll    = view.findViewById(R.id.btn_filter_all);
        btnFilterMusic  = view.findViewById(R.id.btn_filter_music);

        setupRecyclerView();
        setupHeaderListeners();

        // Chạy DB trên background thread
        new Thread(() -> {
            List<Album>  albums  = homeService.getFeaturedAlbums();
            List<Artist> artists = homeService.getFeaturedArtists();
            List<Genre>  genres  = homeService.getCategories();
            List<Song>   top50   = homeService.getTop50();

            // Load user avatar
            String userId = tokenManager.getUserId();
            String avatarUrl = null;
            if (userId != null) {
                try {
                    User user = userProfileService.getProfile(userId);
                    avatarUrl = user != null ? user.getAvatar() : null;
                } catch (Exception ignored) {}
            }

            final String finalAvatarUrl = avatarUrl;
            requireActivity().runOnUiThread(() -> {
                // FIX 2: Glide load Firebase URL cho avatar
                if (imgAvt != null) {
                    if (finalAvatarUrl != null && !finalAvatarUrl.isEmpty()) {
                        Glide.with(this).load(finalAvatarUrl).circleCrop()
                                .placeholder(R.drawable.ic_avatar).into(imgAvt);
                    } else {
                        imgAvt.setImageResource(R.drawable.ic_avatar);
                    }
                }

                if (albums  != null && albumContainer  != null) displayAlbums(albums);
                if (artists != null && artistContainer != null) displayArtists(artists);
                if (genres  != null && genreContainer  != null) displayGenres(genres);
                if (top50   != null && chartContainer  != null) displaySongs(top50, chartContainer);
            });
        }).start();

        return view;
    }

    private void setupHeaderListeners() {
        if (imgAvt != null) {
            imgAvt.setOnClickListener(v ->
                    // FIX 4: UserProfileActivity phải có trong AndroidManifest.xml
                    startActivity(new Intent(getActivity(), UserProfileActivity.class))
            );
        }

        if (imgLogo != null) {
            imgLogo.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Wave Music App", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnFilterAll   != null) btnFilterAll.setOnClickListener(v   -> updateFilterUI(btnFilterAll));
        if (btnFilterMusic != null) btnFilterMusic.setOnClickListener(v -> updateFilterUI(btnFilterMusic));
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
        if (btnFilterAll   != null) {
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
            if (homeScrollView != null) homeScrollView.setVisibility(View.GONE);
            if (rvAllSongs != null) {
                rvAllSongs.setVisibility(View.VISIBLE);
                loadAllSongs();
            }
        } else {
            if (homeScrollView != null) homeScrollView.setVisibility(View.VISIBLE);
            if (rvAllSongs != null) rvAllSongs.setVisibility(View.GONE);
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

    // ── Albums ────────────────────────────────────────────
    private void displayAlbums(List<Album> albums) {
        albumContainer.removeAllViews();
        for (Album album : albums) {
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_album, albumContainer, false);
            ((TextView) item.findViewById(R.id.tvAlbumName)).setText(album.getName());
            if (album.getArtist() != null)
                ((TextView) item.findViewById(R.id.tvArtistName)).setText(album.getArtist().getName());

            // FIX 2: Glide load Firebase URL
            Glide.with(this).load(album.getImage())
                    .placeholder(R.drawable.ic_logo)
                    .into((ImageView) item.findViewById(R.id.imgAlbum));

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
                intent.putExtra("ALBUM_ID", album.getId()); // chỉ truyền ID, không truyền object
                startActivity(intent);
            });
            albumContainer.addView(item);
        }
    }

    // ── Artists ───────────────────────────────────────────
    private void displayArtists(List<Artist> artists) {
        artistContainer.removeAllViews();
        for (Artist artist : artists) {
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_artist, artistContainer, false);
            ((TextView) item.findViewById(R.id.tvArtistName)).setText(artist.getName());

            // FIX 2: Glide load Firebase URL
            Glide.with(this).load(artist.getImage())
                    .circleCrop().placeholder(R.drawable.ic_avatar)
                    .into((ImageView) item.findViewById(R.id.imgArtist));

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SongsByCategoryActivity.class);
                intent.putExtra("ARTIST_ID", artist.getId()); // FIX 3: chỉ truyền ID
                startActivity(intent);
            });
            artistContainer.addView(item);
        }
    }

    // ── Genres ────────────────────────────────────────────
    private void displayGenres(List<Genre> genres) {
        genreContainer.removeAllViews();
        for (Genre genre : genres) {
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_genre, genreContainer, false);
            ((TextView) item.findViewById(R.id.tvGenreName)).setText(genre.getName());

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SongsByCategoryActivity.class);
                intent.putExtra("GENRE_ID",   genre.getId());
                intent.putExtra("GENRE_NAME", genre.getName());
                startActivity(intent);
            });
            genreContainer.addView(item);
        }
    }

    // ── Songs (Bảng xếp hạng) ────────────────────────────
    private void displaySongs(List<Song> songs, LinearLayout container) {
        container.removeAllViews();
        for (Song song : songs) {
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_song, container, false);
            ((TextView) item.findViewById(R.id.tvSongTitle)).setText(song.getName());
            if (song.getArtist() != null)
                ((TextView) item.findViewById(R.id.tvArtistName)).setText(song.getArtist().getName());

            // FIX 2: Glide load Firebase URL
            Glide.with(this).load(song.getImage())
                    .placeholder(R.drawable.ic_music_note)
                    .into((ImageView) item.findViewById(R.id.imgSong));

            item.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
                intent.putExtra("SONG_ID", song.getId()); // FIX 3: chỉ truyền ID
                startActivity(intent);
            });
            container.addView(item);
        }
    }
}