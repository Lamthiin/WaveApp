package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.GenreRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends BaseAdminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        setupAdminChrome(R.id.tvHeaderTitle, R.id.tvAdminAvatar, R.id.bottomAdminNavigation,
                R.id.nav_admin_dashboard, "Dashboard");
        setupQuickNavigation();
        loadSummary();
    }

    private void setupQuickNavigation() {
        findViewById(R.id.cardUsers).setOnClickListener(v ->
                startActivity(new Intent(this, AdminUserManagementActivity.class)));
        findViewById(R.id.cardArtists).setOnClickListener(v ->
                startActivity(new Intent(this, AdminArtistManagementActivity.class)));
        findViewById(R.id.cardSongs).setOnClickListener(v ->
                startActivity(new Intent(this, AdminSongManagementActivity.class)));
        findViewById(R.id.cardAlbums).setOnClickListener(v ->
                startActivity(new Intent(this, AdminAlbumManagementActivity.class)));
    }

    private void loadSummary() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        UserRepository userRepository = new UserRepository(dbHelper);
        ArtistRepository artistRepository = new ArtistRepository(dbHelper);
        SongRepository songRepository = new SongRepository(dbHelper);
        AlbumRepository albumRepository = new AlbumRepository(dbHelper);
        GenreRepository genreRepository = new GenreRepository(dbHelper);

        List<Artist> topArtists = artistRepository.findTopArtists();
        List<Song> topSongs = songRepository.findTopByPlayCount(5);
        List<Album> topAlbums = albumRepository.findFeaturedAlbums();

        ((TextView) findViewById(R.id.tvAdminGreeting)).setText("Xin chào, " + adminName);
        ((TextView) findViewById(R.id.tvUsersCount)).setText(String.valueOf(userRepository.getAllUsers().size()));
        ((TextView) findViewById(R.id.tvArtistsCount)).setText(String.valueOf(artistRepository.findByActiveTrue().size()));
        ((TextView) findViewById(R.id.tvSongsCount)).setText(String.valueOf(songRepository.findAll().size()));
        ((TextView) findViewById(R.id.tvAlbumsCount)).setText(String.valueOf(albumRepository.findByActiveTrue().size()));
        ((TextView) findViewById(R.id.tvGenresCount)).setText(genreRepository.findAll().size() + " thể loại");

        bindSpotlight(topSongs, topArtists);
        bindTopSongs(topSongs);
        bindTopAlbums(topAlbums);
        bindTopArtists(topArtists);
    }

    private void bindSpotlight(List<Song> topSongs, List<Artist> topArtists) {
        TextView tvDashboardSpotlight = findViewById(R.id.tvDashboardSpotlight);
        TextView tvTopSongName = findViewById(R.id.tvTopSongName);
        TextView tvTopSongMeta = findViewById(R.id.tvTopSongMeta);
        TextView tvTopArtistName = findViewById(R.id.tvTopArtistName);
        TextView tvTopArtistMeta = findViewById(R.id.tvTopArtistMeta);

        if (topSongs.isEmpty()) {
            tvDashboardSpotlight.setText("Chưa có dữ liệu bài hát nổi bật");
            tvTopSongName.setText("Chưa có bài hát");
            tvTopSongMeta.setText("Hãy thêm bài hát để theo dõi lượt nghe");
        } else {
            Song topSong = topSongs.get(0);
            String artistName = topSong.getArtist() != null && topSong.getArtist().getName() != null
                    ? topSong.getArtist().getName()
                    : "Chưa rõ nghệ sĩ";
            tvDashboardSpotlight.setText("Bài hát dẫn đầu: " + topSong.getName());
            tvTopSongName.setText(topSong.getName());
            tvTopSongMeta.setText(artistName + " • " + formatCount(topSong.getPlayCount()) + " lượt nghe");
        }

        if (topArtists.isEmpty()) {
            tvTopArtistName.setText("Chưa có nghệ sĩ");
            tvTopArtistMeta.setText("Chưa có dữ liệu follower");
        } else {
            Artist topArtist = topArtists.get(0);
            tvTopArtistName.setText(topArtist.getName());
            tvTopArtistMeta.setText(formatCount(topArtist.getFollowersCount()) + " follower");
        }
    }

    private void bindTopSongs(List<Song> topSongs) {
        LinearLayout layoutTopSongs = findViewById(R.id.layoutTopSongs);
        layoutTopSongs.removeAllViews();

        if (topSongs.isEmpty()) {
            layoutTopSongs.addView(createEmptyState("Chưa có dữ liệu bài hát để hiển thị."));
            return;
        }

        for (int i = 0; i < topSongs.size(); i++) {
            Song song = topSongs.get(i);
            String artistName = song.getArtist() != null && song.getArtist().getName() != null
                    ? song.getArtist().getName()
                    : "Chưa rõ nghệ sĩ";
            String albumName = song.getAlbum() != null && song.getAlbum().getName() != null
                    ? song.getAlbum().getName()
                    : "Chưa có album";
            layoutTopSongs.addView(createRankedRow(
                    i + 1,
                    song.getImage(),
                    song.getName(),
                    artistName + " • " + albumName,
                    formatCount(song.getPlayCount()) + " lượt nghe",
                    i == topSongs.size() - 1
            ));
        }
    }

    private void bindTopArtists(List<Artist> topArtists) {
        LinearLayout layoutTopArtists = findViewById(R.id.layoutTopArtists);
        layoutTopArtists.removeAllViews();

        if (topArtists.isEmpty()) {
            layoutTopArtists.addView(createEmptyState("Chưa có dữ liệu nghệ sĩ để hiển thị."));
            return;
        }

        int limit = Math.min(5, topArtists.size());
        for (int i = 0; i < limit; i++) {
            Artist artist = topArtists.get(i);
            String bio = artist.getBio() == null || artist.getBio().trim().isEmpty()
                    ? "Chưa có mô tả nghệ sĩ"
                    : artist.getBio();
            layoutTopArtists.addView(createRankedRow(
                    i + 1,
                    artist.getImage(),
                    artist.getName(),
                    bio,
                    formatCount(artist.getFollowersCount()) + " follower",
                    i == limit - 1
            ));
        }
    }

    private void bindTopAlbums(List<Album> topAlbums) {
        LinearLayout layoutTopAlbums = findViewById(R.id.layoutTopAlbums);
        layoutTopAlbums.removeAllViews();

        if (topAlbums.isEmpty()) {
            layoutTopAlbums.addView(createEmptyState("Chưa có dữ liệu album để hiển thị."));
            return;
        }

        int limit = Math.min(5, topAlbums.size());
        for (int i = 0; i < limit; i++) {
            Album album = topAlbums.get(i);
            String artistName = album.getArtist() != null && album.getArtist().getName() != null
                    ? album.getArtist().getName()
                    : "Chưa rõ nghệ sĩ";
            String releaseDate = album.getReleaseDate() != null
                    ? album.getReleaseDate().toString()
                    : "Chưa có ngày phát hành";
            layoutTopAlbums.addView(createRankedRow(
                    i + 1,
                    album.getImage(),
                    album.getName(),
                    artistName + " • " + releaseDate,
                    formatCount(album.getPlayCount()) + " lượt nghe",
                    i == limit - 1
            ));
        }
    }

    private View createRankedRow(int rank, String imageUrl, String title, String subtitle, String meta, boolean isLast) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_admin_overview, null, false);
        TextView tvIndex = row.findViewById(R.id.tvAdminItemIndex);
        ImageView ivImage = row.findViewById(R.id.ivAdminItemImage);
        TextView tvTitle = row.findViewById(R.id.tvAdminItemTitle);
        TextView tvSubtitle = row.findViewById(R.id.tvAdminItemSubtitle);
        TextView tvMeta = row.findViewById(R.id.tvAdminItemMeta);
        View btnMore = row.findViewById(R.id.btnAdminItemMore);

        tvIndex.setVisibility(View.VISIBLE);
        tvIndex.setText(String.format(Locale.US, "%d.", rank));
        tvTitle.setText(title);
        tvTitle.setTextSize(15);
        tvTitle.setSingleLine(true);
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvSubtitle.setText(subtitle);
        tvSubtitle.setSingleLine(true);
        tvSubtitle.setEllipsize(TextUtils.TruncateAt.END);
        tvMeta.setText(meta);
        tvMeta.setSingleLine(true);
        tvMeta.setEllipsize(TextUtils.TruncateAt.END);
        btnMore.setVisibility(View.GONE);
        ImageFileHelper.loadIntoImageView(this, imageUrl, ivImage, R.drawable.ic_logo);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (isLast) {
            params.bottomMargin = 0;
        }
        row.setLayoutParams(params);
        row.setMinimumHeight((int) (116 * getResources().getDisplayMetrics().density));
        return row;
    }

    private TextView createEmptyState(String text) {
        TextView emptyView = new TextView(this);
        emptyView.setText(text);
        emptyView.setTextColor(0xFFB3B3B3);
        emptyView.setTextSize(13);
        return emptyView;
    }

    private String formatCount(long value) {
        return String.format(Locale.US, "%,d", value);
    }
}
