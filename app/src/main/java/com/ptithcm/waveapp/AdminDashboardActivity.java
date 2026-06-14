package com.ptithcm.waveapp;

import android.os.Bundle;
import android.widget.TextView;

import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.UserRepository;

public class AdminDashboardActivity extends BaseAdminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        setupAdminChrome(R.id.tvHeaderTitle, R.id.tvAdminAvatar, R.id.bottomAdminNavigation,
                R.id.nav_admin_dashboard, "Dashboard");
        loadSummary();
    }

    private void loadSummary() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        UserRepository userRepository = new UserRepository(dbHelper);
        ArtistRepository artistRepository = new ArtistRepository(dbHelper);
        SongRepository songRepository = new SongRepository(dbHelper);
        AlbumRepository albumRepository = new AlbumRepository(dbHelper);

        ((TextView) findViewById(R.id.tvAdminGreeting)).setText("Xin chào, " + adminName);
        ((TextView) findViewById(R.id.tvUsersCount)).setText(String.valueOf(userRepository.getAllUsers().size()));
        ((TextView) findViewById(R.id.tvArtistsCount)).setText(String.valueOf(artistRepository.findByActiveTrue().size()));
        ((TextView) findViewById(R.id.tvSongsCount)).setText(String.valueOf(songRepository.findAll().size()));
        ((TextView) findViewById(R.id.tvAlbumsCount)).setText(String.valueOf(albumRepository.findByActiveTrue().size()));
    }
}
