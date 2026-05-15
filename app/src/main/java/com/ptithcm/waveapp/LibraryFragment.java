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
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.config.ServiceLocator;
import com.ptithcm.waveapp.controller.LibraryController;
import com.ptithcm.waveapp.dto.response.ApiResponse;
import com.ptithcm.waveapp.dto.response.SongResponse;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private LibraryController libraryController;
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private TextView emptyTextView;
    private List<SongResponse> likedSongs = new ArrayList<>();
    private TextView tabSongs, tabArtists, tabAlbums, tabCustomPlaylists;
    
    // Mock user ID for demonstration. In a real app, this would come from a SessionManager/Prefs
    private static final String MOCK_USER_ID = "1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        libraryController = ServiceLocator.getInstance().getLibraryController();
        recyclerView = view.findViewById(R.id.playlistRecyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        tabSongs = view.findViewById(R.id.tabSongs);
        tabArtists = view.findViewById(R.id.tabArtists);
        tabAlbums = view.findViewById(R.id.tabAlbums);
        tabCustomPlaylists = view.findViewById(R.id.tabCustomPlaylists);

        setupRecyclerView();
        setupTabs();
        loadLikedSongs();

        return view;
    }

    private void setupTabs() {
        tabSongs.setOnClickListener(v -> {
            updateTabUI(tabSongs);
            loadLikedSongs();
        });
        tabArtists.setOnClickListener(v -> {
            updateTabUI(tabArtists);
            // TODO: loadFollowingArtists
        });
        tabAlbums.setOnClickListener(v -> {
            updateTabUI(tabAlbums);
            // TODO: loadLikedAlbums
        });
        tabCustomPlaylists.setOnClickListener(v -> {
            updateTabUI(tabCustomPlaylists);
            // TODO: loadMyPlaylists
        });
    }

    private void updateTabUI(TextView selectedTab) {
        tabSongs.setBackgroundResource(R.drawable.bg_chip_unselected);
        tabSongs.setTextColor(getResources().getColor(R.color.white));
        tabArtists.setBackgroundResource(R.drawable.bg_chip_unselected);
        tabArtists.setTextColor(getResources().getColor(R.color.white));
        tabAlbums.setBackgroundResource(R.drawable.bg_chip_unselected);
        tabAlbums.setTextColor(getResources().getColor(R.color.white));
        tabCustomPlaylists.setBackgroundResource(R.drawable.bg_chip_unselected);
        tabCustomPlaylists.setTextColor(getResources().getColor(R.color.white));

        selectedTab.setBackgroundResource(R.drawable.bg_chip_selected);
        selectedTab.setTextColor(getResources().getColor(R.color.black));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(likedSongs, song -> {
            Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });
        recyclerView.setAdapter(songAdapter);
    }

    private void loadLikedSongs() {
        ApiResponse<List<SongResponse>> response = libraryController.likedSongs(MOCK_USER_ID);
        if (response.isSuccess()) {
            likedSongs.clear();
            likedSongs.addAll(response.getData());
            songAdapter.notifyDataSetChanged();

            if (likedSongs.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
            }
        }
    }
}
