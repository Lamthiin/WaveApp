package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private SongRepository songRepository;
    private ArtistRepository artistRepository;
    private AlbumRepository albumRepository;
    private GenreRepository genreRepository;
    private LikedSongRepository likedSongRepository;
    private LikedAlbumRepository likedAlbumRepository;
    private UserFollowArtistRepository followRepository;
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
        songAdapter.setActionIconMode(SongAdapter.ActionIconMode.HEART);
        artistAdapter = new ArtistAdapter();
        albumAdapter = new AlbumAdapter();
        genreAdapter = new GenreAdapter();

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

        songAdapter.setOnLikeClickListener((song, position) -> {
            String userId = tokenManager.getUserId();
            if (userId == null) return;
            if (likedSongRepository.existsByUserIdAndSongId(userId, song.getId())) {
                likedSongRepository.deleteByUserIdAndSongId(userId, song.getId());
                songRepository.decrementLikeCount(song.getId());
            } else {
                LikedSong likedSong = LikedSong.builder()
                        .user(User.builder().id(userId).build())
                        .song(song)
                        .likedAt(LocalDateTime.now().toString())
                        .build();
                likedSongRepository.save(likedSong);
                songRepository.incrementLikeCount(song.getId());
            }
            songAdapter.notifyItemChanged(position);
        });

        artistAdapter.setOnArtistClickListener(artist -> {
            Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
            intent.putExtra("ARTIST_ID", artist.getId());
            startActivity(intent);
        });

        artistAdapter.setOnFollowClickListener((artist, position) -> {
            String userId = tokenManager.getUserId();
            if (userId == null) return;
            if (followRepository.existsByUserIdAndArtistId(userId, artist.getId())) {
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
                if (repo.existsByUserIdAndAlbumId(userId, album.getId())) {
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
                getActivity().runOnUiThread(() -> albumAdapter.notifyItemChanged(position));
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
                        recyclerViewResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
                        artistAdapter.setArtists(allArtists);
                        recyclerViewResults.setAdapter(artistAdapter);
                        toggleEmptyView(allArtists.isEmpty(), "Không có nghệ sĩ");
                    });
                    break;
                case ALBUMS:
                    allAlbums = albumRepository.findByActiveTrue();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        recyclerViewResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
                        albumAdapter.setAlbums(allAlbums);
                        recyclerViewResults.setAdapter(albumAdapter);
                        toggleEmptyView(allAlbums.isEmpty(), "Không có album");
                    });
                    break;
                case GENRES:
                    allGenres = genreRepository.findAll();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        recyclerViewResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
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
}