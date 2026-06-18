package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ptithcm.waveapp.adapter.AlbumAdapter;
import com.ptithcm.waveapp.adapter.ArtistAdapter;
import com.ptithcm.waveapp.adapter.GenreAdapter;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Genre;
import com.ptithcm.waveapp.model.LikedAlbum;
import com.ptithcm.waveapp.model.LikedSong;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.model.UserFollowArtist;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.GenreRepository;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.util.TokenManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {

    private enum SearchTab { SONGS, ARTISTS, ALBUMS, GENRES }
    private SearchTab currentTab = SearchTab.SONGS;

    private EditText searchEditText;
    private TextView tabSongs, tabArtists, tabAlbums, tabGenres;
    private RecyclerView recyclerViewResults;
    private TextView emptyTextView;

    private SongAdapter songAdapter;
    private ArtistAdapter artistAdapter;
    private AlbumAdapter albumAdapter;
    private GenreAdapter genreAdapter;
    private RecyclerView.ItemDecoration gridSpacingDecoration;

    private SongRepository songRepository;
    private ArtistRepository artistRepository;
    private AlbumRepository albumRepository;
    private GenreRepository genreRepository;
    private LikedSongRepository likedSongRepository;
    private LikedAlbumRepository likedAlbumRepository;
    private UserFollowArtistRepository followRepository;
    private PlaylistService playlistService;
    private TokenManager tokenManager;

    private List<Song> allSongs = new ArrayList<>();
    private List<Artist> allArtists = new ArrayList<>();
    private List<Album> allAlbums = new ArrayList<>();
    private List<Genre> allGenres = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initRepositories();
        initViews(view);
        setupTabs();
        setupSearch();

        loadInitialData();

        return view;
    }

    private void initRepositories() {
        ServiceLocator locator = ServiceLocator.getInstance();
        songRepository = locator.songRepository;
        artistRepository = locator.artistRepository;
        albumRepository = locator.albumRepository;
        genreRepository = locator.genreRepository;
        likedSongRepository = locator.likedSongRepository;
        likedAlbumRepository = locator.likedAlbumRepository;
        followRepository = locator.userFollowArtistRepository;
        playlistService = locator.getPlaylistService();
        tokenManager = new TokenManager(requireContext());
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        tabSongs = view.findViewById(R.id.tabSongs);
        tabArtists = view.findViewById(R.id.tabArtists);
        tabAlbums = view.findViewById(R.id.tabAlbums);
        tabGenres = view.findViewById(R.id.tabGenres);
        recyclerViewResults = view.findViewById(R.id.recyclerViewResults);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        songAdapter = new SongAdapter();
        songAdapter.setActionIconMode(SongAdapter.ActionIconMode.MORE);
        songAdapter.setShowIndex(false);
        artistAdapter = new ArtistAdapter();
        artistAdapter.setLayoutMode(ArtistAdapter.LayoutMode.LIST);
        albumAdapter = new AlbumAdapter();
        genreAdapter = new GenreAdapter();
        gridSpacingDecoration = new GridSpacingItemDecoration(2, dpToPx(12));

        setupAdapterListeners();
    }

    private void setupAdapterListeners() {
        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
            intent.putExtra("SONG_ID", song.getId());
            intent.putExtra("SONG_DATA", song);
            intent.putExtra("QUEUE_LIST", new ArrayList<>(allSongs));
            startActivity(intent);
        });

        songAdapter.setOnMoreClickListener((song, position, anchor) -> {
            showSongActionSheet(song, position);
        });

        artistAdapter.setOnArtistClickListener(artist -> {
            Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
            intent.putExtra("ARTIST_ID", artist.getId());
            startActivity(intent);
        });

        artistAdapter.setOnFollowClickListener((artist, position) -> {
            String userId = tokenManager.getUserId();
            if (userId == null) return;
            boolean isFollowing = followRepository.existsByUserIdAndArtistId(userId, artist.getId());
            if (isFollowing) {
                followRepository.deleteByUserIdAndArtistId(userId, artist.getId());
                artistRepository.decrementFollowers(artist.getId());
            } else {
                UserFollowArtist follow = UserFollowArtist.builder()
                        .user(User.builder().id(userId).build())
                        .artist(artist)
                        .followedAt(LocalDateTime.now().toString())
                        .build();
                followRepository.save(follow);
                artistRepository.incrementFollowers(artist.getId());
            }
            artistAdapter.notifyItemChanged(position);
            Toast.makeText(
                    getContext(),
                    isFollowing ? "Đã xóa khỏi yêu thích" : "Đã thêm vào yêu thích",
                    Toast.LENGTH_SHORT
            ).show();
        });

        albumAdapter.setOnAlbumClickListener(album -> {
            Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
            intent.putExtra("ALBUM_ID", album.getId());
            startActivity(intent);
        });

        albumAdapter.setOnLikeClickListener((album, position) -> {
            String userId = tokenManager.getUserId();
            if (userId == null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                LikedAlbumRepository repo = ServiceLocator.getInstance().likedAlbumRepository;
                boolean isLiked = repo.existsByUserIdAndAlbumId(userId, album.getId());
                if (isLiked) {
                    repo.deleteByUserIdAndAlbumId(userId, album.getId());
                } else {
                    com.ptithcm.waveapp.model.User user = new com.ptithcm.waveapp.model.User();
                    user.setId(userId);
                    com.ptithcm.waveapp.model.LikedAlbum la = new com.ptithcm.waveapp.model.LikedAlbum();
                    la.setUser(user);
                    la.setAlbum(album);
                    la.setAddedAt(java.time.LocalDateTime.now().toString());
                    repo.save(la);
                }
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    albumAdapter.notifyItemChanged(position);
                    Toast.makeText(
                            getContext(),
                            isLiked ? "Đã xóa khỏi yêu thích" : "Đã thêm vào yêu thích",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }).start();
        });

        genreAdapter.setOnGenreClickListener(genre -> {
            Intent intent = new Intent(getActivity(), SongsByCategoryActivity.class);
            intent.putExtra("GENRE_ID", genre.getId());
            intent.putExtra("GENRE_NAME", genre.getName());
            intent.putExtra("GENRE_IMAGE_URL", genre.getImageUrl());
            startActivity(intent);
        });
    }

    private void showSongActionSheet(Song song, int position) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_search_song_actions, null, false);

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
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

            LikedSong likedSong = LikedSong.builder()
                    .user(User.builder().id(userId).build())
                    .song(song)
                    .likedAt(LocalDateTime.now().toString())
                    .build();

            likedSongRepository.save(likedSong);
            songRepository.incrementLikeCount(song.getId());

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                songAdapter.notifyItemChanged(position);
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
            List<com.ptithcm.waveapp.model.Playlist> playlists = playlistService.getMyPlaylists(userId);
            if (getActivity() == null) return;

            getActivity().runOnUiThread(() -> {
                if (playlists.isEmpty()) {
                    Toast.makeText(getContext(), "Bạn chưa có playlist nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                View dialogView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.dialog_search_playlist_picker, null, false);

                BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
                dialog.setContentView(dialogView);

                LinearLayout container = dialogView.findViewById(R.id.layoutPlaylistOptions);
                View cancelView = dialogView.findViewById(R.id.tvSearchPlaylistCancel);

                for (com.ptithcm.waveapp.model.Playlist playlist : playlists) {
                    TextView optionView = new TextView(requireContext());
                    optionView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(50)
                    ));
                    optionView.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    optionView.setPadding(dpToPx(24), 0, dpToPx(24), 0);
                    optionView.setText(playlist.getName());
                    optionView.setTextColor(requireContext().getColor(R.color.white));
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
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Đã thêm vào playlist", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupTabs() {
        tabSongs.setOnClickListener(v -> selectTab(SearchTab.SONGS));
        tabArtists.setOnClickListener(v -> selectTab(SearchTab.ARTISTS));
        tabAlbums.setOnClickListener(v -> selectTab(SearchTab.ALBUMS));
        tabGenres.setOnClickListener(v -> selectTab(SearchTab.GENRES));
    }

    private void selectTab(SearchTab tab) {
        if (currentTab == tab) return;
        currentTab = tab;
        searchEditText.setText(""); // Reset keyword
        updateTabUI();
        loadInitialData();
    }

    private void updateTabUI() {
        tabSongs.setBackgroundResource(currentTab == SearchTab.SONGS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabSongs.setTextColor(getResources().getColor(currentTab == SearchTab.SONGS ? R.color.black : R.color.white));

        tabArtists.setBackgroundResource(currentTab == SearchTab.ARTISTS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabArtists.setTextColor(getResources().getColor(currentTab == SearchTab.ARTISTS ? R.color.black : R.color.white));

        tabAlbums.setBackgroundResource(currentTab == SearchTab.ALBUMS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabAlbums.setTextColor(getResources().getColor(currentTab == SearchTab.ALBUMS ? R.color.black : R.color.white));

        tabGenres.setBackgroundResource(currentTab == SearchTab.GENRES ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabGenres.setTextColor(getResources().getColor(currentTab == SearchTab.GENRES ? R.color.black : R.color.white));
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
        });
    }

    private void loadInitialData() {
        new Thread(() -> {
            switch (currentTab) {
                case SONGS:
                    allSongs = songRepository.findAll();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        configureResultsRecycler(false);
                        recyclerViewResults.setLayoutManager(new LinearLayoutManager(getContext()));
                        songAdapter.setSongs(allSongs);
                        recyclerViewResults.setAdapter(songAdapter);
                        toggleEmptyView(allSongs.isEmpty(), "Không có bài hát");
                    });
                    break;
                case ARTISTS:
                    allArtists = artistRepository.findByActiveTrue();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        configureResultsRecycler(false);
                        recyclerViewResults.setLayoutManager(new LinearLayoutManager(getContext()));
                        artistAdapter.setArtists(allArtists);
                        recyclerViewResults.setAdapter(artistAdapter);
                        toggleEmptyView(allArtists.isEmpty(), "Không có nghệ sĩ");
                    });
                    break;
                case ALBUMS:
                    allAlbums = albumRepository.findByActiveTrue();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        configureResultsRecycler(3, 10);
                        recyclerViewResults.setLayoutManager(new GridLayoutManager(getContext(), 3));
                        albumAdapter.setAlbums(allAlbums);
                        recyclerViewResults.setAdapter(albumAdapter);
                        toggleEmptyView(allAlbums.isEmpty(), "Không có album");
                    });
                    break;
                case GENRES:
                    allGenres = genreRepository.findAll();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        configureResultsRecycler(2, 8);
                        recyclerViewResults.setLayoutManager(createGenreGridLayoutManager());
                        genreAdapter.setGenres(allGenres);
                        recyclerViewResults.setAdapter(genreAdapter);
                        toggleEmptyView(allGenres.isEmpty(), "Không có thể loại");
                    });
                    break;
            }
        }).start();
    }

    private void performSearch(String query) {
        String lowerQuery = query.toLowerCase().trim();
        if (lowerQuery.isEmpty()) {
            loadInitialData();
            return;
        }

        switch (currentTab) {
            case SONGS:
                List<Song> filteredSongs = allSongs.stream()
                        .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(lowerQuery)) ||
                                     (s.getArtist() != null && s.getArtist().getName() != null && s.getArtist().getName().toLowerCase().contains(lowerQuery)) ||
                                     (s.getAlbum() != null && s.getAlbum().getName() != null && s.getAlbum().getName().toLowerCase().contains(lowerQuery)))
                        .collect(Collectors.toList());
                songAdapter.setSongs(filteredSongs);
                toggleEmptyView(filteredSongs.isEmpty(), "Không tìm thấy bài hát phù hợp");
                break;
            case ARTISTS:
                List<Artist> filteredArtists = allArtists.stream()
                        .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                artistAdapter.setArtists(filteredArtists);
                toggleEmptyView(filteredArtists.isEmpty(), "Không tìm thấy nghệ sĩ phù hợp");
                break;
            case ALBUMS:
                List<Album> filteredAlbums = allAlbums.stream()
                        .filter(al -> (al.getName() != null && al.getName().toLowerCase().contains(lowerQuery)) ||
                                      (al.getArtist() != null && al.getArtist().getName() != null && al.getArtist().getName().toLowerCase().contains(lowerQuery)))
                        .collect(Collectors.toList());
                albumAdapter.setAlbums(filteredAlbums);
                toggleEmptyView(filteredAlbums.isEmpty(), "Không tìm thấy album phù hợp");
                break;
            case GENRES:
                List<Genre> filteredGenres = allGenres.stream()
                        .filter(g -> g.getName() != null && g.getName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                genreAdapter.setGenres(filteredGenres);
                toggleEmptyView(filteredGenres.isEmpty(), "Không tìm thấy thể loại phù hợp");
                break;
        }
    }

    private void toggleEmptyView(boolean isEmpty, String message) {
        emptyTextView.setText(message);
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void configureResultsRecycler(boolean isGrid) {
        while (recyclerViewResults.getItemDecorationCount() > 0) {
            recyclerViewResults.removeItemDecorationAt(0);
        }
        if (isGrid) {
            recyclerViewResults.addItemDecoration(gridSpacingDecoration);
        }
    }

    private void configureResultsRecycler(int spanCount, int spacingDp) {
        while (recyclerViewResults.getItemDecorationCount() > 0) {
            recyclerViewResults.removeItemDecorationAt(0);
        }
        recyclerViewResults.addItemDecoration(new GridSpacingItemDecoration(spanCount, dpToPx(spacingDp)));
    }

    private GridLayoutManager createGenreGridLayoutManager() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int itemCount = genreAdapter != null ? genreAdapter.getItemCount() : 0;
                boolean isLastOddItem = itemCount % 2 == 1 && position == itemCount - 1;
                return isLastOddItem ? 2 : 1;
            }
        });
        return layoutManager;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;

        GridSpacingItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            outRect.left = column == 0 ? 0 : spacing / 2;
            outRect.right = column == spanCount - 1 ? 0 : spacing / 2;
            outRect.top = position < spanCount ? 0 : spacing / 2;
            outRect.bottom = 0;
        }
    }
}
