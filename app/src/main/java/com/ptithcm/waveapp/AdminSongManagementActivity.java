package com.ptithcm.waveapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.adapter.AdminOverviewAdapter;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.util.SearchNormalizer;

import java.util.ArrayList;
import java.util.List;

public class AdminSongManagementActivity extends BaseAdminActivity {

    private final List<Song> allSongs = new ArrayList<>();
    private AdminOverviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_overview_list);
        setupAdminChrome(R.id.tvHeaderTitle, R.id.tvAdminAvatar, R.id.bottomAdminNavigation,
                R.id.nav_admin_songs, "Quản lý bài hát");

        ((TextView) findViewById(R.id.tvSectionHint)).setText("Danh sách bài hát để admin theo dõi nhanh.");

        RecyclerView recyclerView = findViewById(R.id.rvAdminList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOverviewAdapter();
        recyclerView.setAdapter(adapter);

        EditText searchInput = findViewById(R.id.etSearchAdmin);
        searchInput.setHint("Tìm bài hát theo tên...");
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadSongs();
    }

    private void loadSongs() {
        allSongs.clear();
        allSongs.addAll(new SongRepository(DatabaseHelper.getInstance(this)).findAll());
        filterSongs("");
    }

    private void filterSongs(String query) {
        List<AdminOverviewAdapter.AdminOverviewItem> items = new ArrayList<>();

        for (Song song : allSongs) {
            String name = song.getName() == null ? "" : song.getName();
            String artistName = song.getArtist() != null && song.getArtist().getName() != null
                    ? song.getArtist().getName() : "Chưa rõ nghệ sĩ";
            String albumName = song.getAlbum() != null && song.getAlbum().getName() != null
                    ? song.getAlbum().getName() : "Chưa có album";

            if (!SearchNormalizer.containsNormalized(name, query)
                    && !SearchNormalizer.containsNormalized(artistName, query)
                    && !SearchNormalizer.containsNormalized(albumName, query)) {
                continue;
            }

            String subtitle = artistName + " • " + albumName;
            String meta = "Lượt nghe: " + song.getPlayCount() + " • Yêu thích: " + song.getLikeCount();
            items.add(new AdminOverviewAdapter.AdminOverviewItem(
                    song.getId(),
                    "",
                    name,
                    subtitle,
                    meta,
                    song.getImage(),
                    R.drawable.ic_logo,
                    false
            ));
        }

        adapter.setItems(items);
    }
}
