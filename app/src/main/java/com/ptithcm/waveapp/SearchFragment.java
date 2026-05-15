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
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.config.ServiceLocator;
import com.ptithcm.waveapp.controller.SearchController;
import com.ptithcm.waveapp.dto.response.ApiResponse;
import com.ptithcm.waveapp.dto.response.SongResponse;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchController searchController;
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private EditText searchEditText;
    private TextView emptyTextView;
    private List<SongResponse> searchResults = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchController = ServiceLocator.getInstance().getSearchController();
        recyclerView = view.findViewById(R.id.recyclerViewResults);
        searchEditText = view.findViewById(R.id.searchEditText);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        setupRecyclerView();
        setupSearchListener();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(searchResults, song -> {
            Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });
        recyclerView.setAdapter(songAdapter);
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            searchResults.clear();
            songAdapter.notifyDataSetChanged();
            emptyTextView.setVisibility(View.GONE);
            return;
        }

        ApiResponse<List<SongResponse>> response = searchController.searchSongs(query);
        if (response.isSuccess()) {
            searchResults.clear();
            searchResults.addAll(response.getData());
            songAdapter.notifyDataSetChanged();
            
            if (searchResults.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
            }
        }
    }
}
