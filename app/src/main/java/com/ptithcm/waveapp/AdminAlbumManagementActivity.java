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
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.util.SearchNormalizer;

import java.util.ArrayList;
import java.util.List;

public class AdminAlbumManagementActivity extends BaseAdminActivity {

    private final List<Album> allAlbums = new ArrayList<>();
    private AdminOverviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_overview_list);
        setupAdminChrome(R.id.tvHeaderTitle, R.id.tvAdminAvatar, R.id.bottomAdminNavigation,
                R.id.nav_admin_albums, "Quản lý album");

        ((TextView) findViewById(R.id.tvSectionHint)).setText("Danh sách album hiện có trong hệ thống.");

        RecyclerView recyclerView = findViewById(R.id.rvAdminList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOverviewAdapter();
        recyclerView.setAdapter(adapter);

        EditText searchInput = findViewById(R.id.etSearchAdmin);
        searchInput.setHint("Tìm album theo tên...");
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAlbums(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadAlbums();
    }

    private void loadAlbums() {
        allAlbums.clear();
        allAlbums.addAll(new AlbumRepository(DatabaseHelper.getInstance(this)).findByActiveTrue());
        filterAlbums("");
    }

    private void filterAlbums(String query) {
        List<AdminOverviewAdapter.AdminOverviewItem> items = new ArrayList<>();

        for (Album album : allAlbums) {
            String name = album.getName() == null ? "" : album.getName();
            String artistName = album.getArtist() != null && album.getArtist().getName() != null
                    ? album.getArtist().getName() : "Chưa rõ nghệ sĩ";

            if (!SearchNormalizer.containsNormalized(name, query)
                    && !SearchNormalizer.containsNormalized(artistName, query)) {
                continue;
            }

            String subtitle = "Nghệ sĩ: " + artistName;
            String meta = album.getReleaseDate() != null
                    ? "Ngày phát hành: " + album.getReleaseDate()
                    : "Chưa có ngày phát hành";
            items.add(new AdminOverviewAdapter.AdminOverviewItem(
                    album.getId(),
                    "",
                    name,
                    subtitle,
                    meta,
                    album.getImage(),
                    R.drawable.ic_logo,
                    false,
                    false
            ));
        }

        adapter.setItems(items);
    }
}
