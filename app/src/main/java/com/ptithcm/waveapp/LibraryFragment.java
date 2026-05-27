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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.adapter.AlbumAdapter;
import com.ptithcm.waveapp.adapter.ArtistAdapter;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.LikedAlbum;
import com.ptithcm.waveapp.model.LikedSong;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.model.UserFollowArtist;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryFragment extends Fragment {

    private enum Tab { SONGS, ARTISTS, ALBUMS }
    private Tab currentTab = Tab.SONGS;

    private UserProfileService userProfileService;
    private LikedSongRepository likedSongRepository;
    private LikedAlbumRepository likedAlbumRepository;
    private UserFollowArtistRepository followRepository;
    private TokenManager tokenManager;

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArtistAdapter artistAdapter;
    private AlbumAdapter albumAdapter;
    
    private EditText etSearch;
    private TextView emptyTextView;
    private TextView tabSongs, tabArtists, tabAlbums;

    private List<Song> allSongs = new ArrayList<>();
    private List<Artist> allArtists = new ArrayList<>();
    private List<Album> allAlbums = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        initServices(view);
        initViews(view);
        setupTabs();
        setupSearch();
        
        loadData();

        return view;
    }

    private void initServices(View view) {
        ServiceLocator locator = ServiceLocator.getInstance();
        userProfileService = locator.getUserProfileService();
        likedSongRepository = locator.likedSongRepository;
        likedAlbumRepository = locator.likedAlbumRepository;
        followRepository = locator.userFollowArtistRepository;
        tokenManager = new TokenManager(requireContext());
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewLibrary);
        etSearch = view.findViewById(R.id.etSearchLibrary);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        tabSongs = view.findViewById(R.id.tabSongs);
        tabArtists = view.findViewById(R.id.tabArtists);
        tabAlbums = view.findViewById(R.id.tabAlbums);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        songAdapter = new SongAdapter();
        songAdapter.setActionIconMode(SongAdapter.ActionIconMode.HEART);
        artistAdapter = new ArtistAdapter();
        albumAdapter = new AlbumAdapter();

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
            likedSongRepository.deleteByUserIdAndSongId(userId, song.getId());
            // In Library, clicking like (actually it's a heart to unlike) removes it
            allSongs.remove(song);
            filterData(etSearch.getText().toString());
        });

        artistAdapter.setOnArtistClickListener(artist -> {
            Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
            intent.putExtra("ARTIST_ID", artist.getId());
            startActivity(intent);
        });

        artistAdapter.setOnFollowClickListener((artist, position) -> {
            String userId = tokenManager.getUserId();
            if (userId == null) return;
            followRepository.deleteByUserIdAndArtistId(userId, artist.getId());
            allArtists.remove(artist);
            filterData(etSearch.getText().toString());
        });

        albumAdapter.setOnAlbumClickListener(album -> {
            Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
            intent.putExtra("ALBUM_ID", album.getId());
            startActivity(intent);
        });

        albumAdapter.setOnLikeClickListener((album, position) -> {
            String userId = tokenManager.getUserId();
            if (userId == null) return;
            likedAlbumRepository.deleteByUserIdAndAlbumId(userId, album.getId());
            allAlbums.remove(album);
            filterData(etSearch.getText().toString());
        });
    }

    private void setupTabs() {
        tabSongs.setOnClickListener(v -> selectTab(Tab.SONGS));
        tabArtists.setOnClickListener(v -> selectTab(Tab.ARTISTS));
        tabAlbums.setOnClickListener(v -> selectTab(Tab.ALBUMS));
    }

    private void selectTab(Tab tab) {
        if (currentTab == tab) return;
        currentTab = tab;
        etSearch.setText(""); // Reset keyword
        updateTabUI();
        loadData();
    }

    private void updateTabUI() {
        tabSongs.setBackgroundResource(currentTab == Tab.SONGS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabSongs.setTextColor(getResources().getColor(currentTab == Tab.SONGS ? R.color.black : R.color.white));

        tabArtists.setBackgroundResource(currentTab == Tab.ARTISTS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabArtists.setTextColor(getResources().getColor(currentTab == Tab.ARTISTS ? R.color.black : R.color.white));

        tabAlbums.setBackgroundResource(currentTab == Tab.ALBUMS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabAlbums.setTextColor(getResources().getColor(currentTab == Tab.ALBUMS ? R.color.black : R.color.white));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filterData(s.toString());
            }
        });
    }

    private void loadData() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        new Thread(() -> {
            switch (currentTab) {
                case SONGS:
                    allSongs = userProfileService.getLikedSongs(userId);
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        songAdapter.setSongs(allSongs);
                        recyclerView.setAdapter(songAdapter);
                        updateEmptyState(allSongs.isEmpty(), "Không có bài hát yêu thích");
                    });
                    break;
                case ARTISTS:
                    allArtists = userProfileService.getFollowingArtists(userId);
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        artistAdapter.setArtists(allArtists);
                        recyclerView.setAdapter(artistAdapter);
                        updateEmptyState(allArtists.isEmpty(), "Không có nghệ sĩ yêu thích");
                    });
                    break;
                case ALBUMS:
                    allAlbums = userProfileService.getLikedAlbums(userId);
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));
                        albumAdapter.setAlbums(allAlbums);
                        recyclerView.setAdapter(albumAdapter);
                        updateEmptyState(allAlbums.isEmpty(), "Không có album yêu thích");
                    });
                    break;
            }
        }).start();
    }

    private void filterData(String query) {
        String lowerQuery = query.toLowerCase().trim();
        if (lowerQuery.isEmpty()) {
            switch (currentTab) {
                case SONGS:
                    songAdapter.setSongs(allSongs);
                    updateEmptyState(allSongs.isEmpty(), "Không có bài hát yêu thích");
                    break;
                case ARTISTS:
                    artistAdapter.setArtists(allArtists);
                    updateEmptyState(allArtists.isEmpty(), "Không có nghệ sĩ yêu thích");
                    break;
                case ALBUMS:
                    albumAdapter.setAlbums(allAlbums);
                    updateEmptyState(allAlbums.isEmpty(), "Không có album yêu thích");
                    break;
            }
            return;
        }

        switch (currentTab) {
            case SONGS:
                List<Song> filteredSongs = allSongs.stream()
                        .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(lowerQuery)) ||
                                     (s.getArtist() != null && s.getArtist().getName() != null && s.getArtist().getName().toLowerCase().contains(lowerQuery)))
                        .collect(Collectors.toList());
                songAdapter.setSongs(filteredSongs);
                updateEmptyState(filteredSongs.isEmpty(), "Không tìm thấy bài hát yêu thích phù hợp");
                break;
            case ARTISTS:
                List<Artist> filteredArtists = allArtists.stream()
                        .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                artistAdapter.setArtists(filteredArtists);
                updateEmptyState(filteredArtists.isEmpty(), "Không tìm thấy nghệ sĩ yêu thích phù hợp");
                break;
            case ALBUMS:
                List<Album> filteredAlbums = allAlbums.stream()
                        .filter(al -> (al.getName() != null && al.getName().toLowerCase().contains(lowerQuery)) ||
                                      (al.getArtist() != null && al.getArtist().getName() != null && al.getArtist().getName().toLowerCase().contains(lowerQuery)))
                        .collect(Collectors.toList());
                albumAdapter.setAlbums(filteredAlbums);
                updateEmptyState(filteredAlbums.isEmpty(), "Không tìm thấy album yêu thích phù hợp");
                break;
        }
    }

    private void updateEmptyState(boolean isEmpty, String message) {
        emptyTextView.setText(message);
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}