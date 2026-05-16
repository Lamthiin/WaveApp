package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.adapter.AlbumAdapter;
import com.ptithcm.waveapp.adapter.ArtistAdapter;
import com.ptithcm.waveapp.adapter.PlaylistAdapter;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private UserProfileService userProfileService;
    private TokenManager tokenManager;

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArtistAdapter artistAdapter;
    private AlbumAdapter albumAdapter;
    private PlaylistAdapter playlistAdapter;

    private TextView emptyTextView;
    private TextView tabSongs, tabArtists, tabAlbums, tabCustomPlaylists;

    private final List<Song> currentList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        userProfileService = ServiceLocator.getInstance().getUserProfileService();
        tokenManager       = new TokenManager(requireContext());

        recyclerView       = view.findViewById(R.id.playlistRecyclerView);
        emptyTextView      = view.findViewById(R.id.emptyTextView);
        tabSongs           = view.findViewById(R.id.tabSongs);
        tabArtists         = view.findViewById(R.id.tabArtists);
        tabAlbums          = view.findViewById(R.id.tabAlbums);
        tabCustomPlaylists = view.findViewById(R.id.tabCustomPlaylists);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        songAdapter = new SongAdapter();
        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });

        songAdapter.setOnLikeClickListener((song, position) -> {
            toggleLikeSong(song, position);
        });

        artistAdapter = new ArtistAdapter();
        artistAdapter.setOnArtistClickListener(artist -> {
            Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
            intent.putExtra("ARTIST_DATA", artist);
            startActivity(intent);
        });

        artistAdapter.setOnFollowClickListener((artist, position) -> {
            toggleFollowArtist(artist, position);
        });

        albumAdapter = new AlbumAdapter();
        albumAdapter.setOnAlbumClickListener(album -> {
            Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
            intent.putExtra("ALBUM_DATA", album);
            startActivity(intent);
        });

        albumAdapter.setOnLikeClickListener((album, position) -> {
            toggleLikeAlbum(album, position);
        });

        playlistAdapter = new PlaylistAdapter();
        playlistAdapter.setOnPlaylistClickListener(playlist -> {
            Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
            intent.putExtra("PLAYLIST_DATA", playlist);
            startActivity(intent);
        });

        recyclerView.setAdapter(songAdapter);

        setupTabs();
        loadLikedSongs(); // tab mặc định khi mở

        return view;
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    private void setupTabs() {
        tabSongs.setOnClickListener(v -> {
            updateTabUI(tabSongs);
            recyclerView.setAdapter(songAdapter);
            loadLikedSongs();
        });
        tabArtists.setOnClickListener(v -> {
            updateTabUI(tabArtists);
            recyclerView.setAdapter(artistAdapter);
            loadFollowingArtists();
        });
        tabAlbums.setOnClickListener(v -> {
            updateTabUI(tabAlbums);
            recyclerView.setAdapter(albumAdapter);
            loadLikedAlbums();
        });
        tabCustomPlaylists.setOnClickListener(v -> {
            updateTabUI(tabCustomPlaylists);
            recyclerView.setAdapter(playlistAdapter);
            loadMyPlaylists();
        });
    }

    private void updateTabUI(TextView selectedTab) {
        for (TextView tab : new TextView[]{tabSongs, tabArtists, tabAlbums, tabCustomPlaylists}) {
            tab.setBackgroundResource(R.drawable.bg_chip_unselected);
            tab.setTextColor(getResources().getColor(R.color.white));
        }
        selectedTab.setBackgroundResource(R.drawable.bg_chip_selected);
        selectedTab.setTextColor(getResources().getColor(R.color.black));
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    // ── Load data ─────────────────────────────────────────────────────────────

    private void loadLikedSongs() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        List<Song> songs = userProfileService.getLikedSongs(userId);
        songAdapter.setSongs(songs);
        updateEmptyState(songs.isEmpty());
    }

    private void loadFollowingArtists() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        List<Artist> artists = userProfileService.getFollowingArtists(userId);
        artistAdapter.setArtists(artists);
        updateEmptyState(artists.isEmpty());
    }

    private void loadLikedAlbums() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        List<Album> albums = userProfileService.getLikedAlbums(userId);
        albumAdapter.setAlbums(albums);
        updateEmptyState(albums.isEmpty());
    }

    private void loadMyPlaylists() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        List<Playlist> playlists = userProfileService.getMyPlaylists(userId);
        playlistAdapter.setPlaylists(playlists);
        updateEmptyState(playlists.isEmpty());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCurrentTab();
    }

    private void refreshCurrentTab() {
        if (recyclerView.getAdapter() == songAdapter) {
            loadLikedSongs();
        } else if (recyclerView.getAdapter() == artistAdapter) {
            loadFollowingArtists();
        } else if (recyclerView.getAdapter() == albumAdapter) {
            loadLikedAlbums();
        } else if (recyclerView.getAdapter() == playlistAdapter) {
            loadMyPlaylists();
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void toggleLikeSong(Song song, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        ServiceLocator locator = ServiceLocator.getInstance();
        boolean currentlyLiked = locator.likedSongRepository.existsByUserIdAndSongId(userId, song.getId());
        if (currentlyLiked) {
            locator.likedSongRepository.deleteByUserIdAndSongId(userId, song.getId());
            locator.songRepository.decrementLikeCount(song.getId());
        } else {
            String now = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                now = java.time.LocalDateTime.now().toString();
            }
            com.ptithcm.waveapp.model.LikedSong likedSong = com.ptithcm.waveapp.model.LikedSong.builder()
                    .user(com.ptithcm.waveapp.model.User.builder().id(userId).build())
                    .song(song)
                    .likedAt(now)
                    .build();
            locator.likedSongRepository.save(likedSong);
            locator.songRepository.incrementLikeCount(song.getId());
        }
        songAdapter.notifyItemChanged(position);
    }

    private void toggleLikeAlbum(Album album, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        ServiceLocator locator = ServiceLocator.getInstance();
        boolean currentlyLiked = locator.likedAlbumRepository.existsByUserIdAndAlbumId(userId, album.getId());
        if (currentlyLiked) {
            locator.likedAlbumRepository.deleteByUserIdAndAlbumId(userId, album.getId());
        } else {
            String now = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                now = java.time.LocalDateTime.now().toString();
            }
            com.ptithcm.waveapp.model.LikedAlbum likedAlbum = com.ptithcm.waveapp.model.LikedAlbum.builder()
                    .user(com.ptithcm.waveapp.model.User.builder().id(userId).build())
                    .album(album)
                    .addedAt(now)
                    .build();
            locator.likedAlbumRepository.save(likedAlbum);
        }
        albumAdapter.notifyItemChanged(position);
    }

    private void toggleFollowArtist(Artist artist, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        ServiceLocator locator = ServiceLocator.getInstance();
        boolean currentlyFollowing = locator.userFollowArtistRepository.existsByUserIdAndArtistId(userId, artist.getId());
        if (currentlyFollowing) {
            locator.userFollowArtistRepository.deleteByUserIdAndArtistId(userId, artist.getId());
            locator.artistRepository.decrementFollowers(artist.getId());
        } else {
            String now = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                now = java.time.LocalDateTime.now().toString();
            }
            com.ptithcm.waveapp.model.UserFollowArtist follow = com.ptithcm.waveapp.model.UserFollowArtist.builder()
                    .user(com.ptithcm.waveapp.model.User.builder().id(userId).build())
                    .artist(artist)
                    .followedAt(now)
                    .build();
            locator.userFollowArtistRepository.save(follow);
            locator.artistRepository.incrementFollowers(artist.getId());
        }
        artistAdapter.notifyItemChanged(position);
    }
}