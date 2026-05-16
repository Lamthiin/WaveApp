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
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;
import com.ptithcm.waveapp.util.TokenManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private SongRepository songRepository;
    private ArtistRepository artistRepository;
    private AlbumRepository albumRepository;
    private LikedSongRepository likedSongRepository;
    private LikedAlbumRepository likedAlbumRepository;
    private UserFollowArtistRepository followRepository;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initRepositories();
        initViews(view);
        setupTabs();
        setupSearch();

        return view;
    }

    private void initRepositories() {
        ServiceLocator locator = ServiceLocator.getInstance();
        songRepository = locator.songRepository;
        artistRepository = locator.artistRepository;
        albumRepository = locator.albumRepository;
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

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(getContext()));
        
        songAdapter = new SongAdapter();
        artistAdapter = new ArtistAdapter();
        albumAdapter = new AlbumAdapter();

        setupAdapterListeners();
    }

    private void setupAdapterListeners() {
        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
            intent.putExtra("SONG_ID", song.getId());
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
            if (userId == null) return;
            if (likedAlbumRepository.existsByUserIdAndAlbumId(userId, album.getId())) {
                likedAlbumRepository.deleteByUserIdAndAlbumId(userId, album.getId());
            } else {
                LikedAlbum likedAlbum = LikedAlbum.builder()
                        .user(User.builder().id(userId).build())
                        .album(album)
                        .addedAt(LocalDateTime.now().toString())
                        .build();
                likedAlbumRepository.save(likedAlbum);
            }
            albumAdapter.notifyItemChanged(position);
        });
    }

    private void setupTabs() {
        tabSongs.setOnClickListener(v -> selectTab(SearchTab.SONGS));
        tabArtists.setOnClickListener(v -> selectTab(SearchTab.ARTISTS));
        tabAlbums.setOnClickListener(v -> selectTab(SearchTab.ALBUMS));
        tabGenres.setOnClickListener(v -> selectTab(SearchTab.GENRES));
    }

    private void selectTab(SearchTab tab) {
        currentTab = tab;
        updateTabUI();
        performSearch(searchEditText.getText().toString());
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

    private void performSearch(String query) {
        if (query.isEmpty()) {
            recyclerViewResults.setAdapter(null);
            emptyTextView.setVisibility(View.GONE);
            return;
        }

        switch (currentTab) {
            case SONGS:
                List<Song> songs = songRepository.searchByName(query);
                songAdapter.setSongs(songs);
                recyclerViewResults.setAdapter(songAdapter);
                toggleEmptyView(songs.isEmpty());
                break;
            case ARTISTS:
                List<Artist> artists = artistRepository.searchByName(query);
                artistAdapter.setArtists(artists);
                recyclerViewResults.setAdapter(artistAdapter);
                toggleEmptyView(artists.isEmpty());
                break;
            case ALBUMS:
                List<Album> albums = albumRepository.searchByName(query);
                albumAdapter.setAlbums(albums);
                recyclerViewResults.setAdapter(albumAdapter);
                toggleEmptyView(albums.isEmpty());
                break;
            case GENRES:
                // Thể loại thường ít, có thể search local hoặc từ repo
                // Hiện tại hiển thị trống hoặc danh sách gợi ý
                toggleEmptyView(true);
                break;
        }
    }

    private void toggleEmptyView(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}