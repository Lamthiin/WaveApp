package com.ptithcm.waveapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AdminArtistDetailActivity extends AppCompatActivity {

    private ShapeableImageView ivArtistImage;
    private TextView tvArtistName;
    private TextView tvArtistFollowers;
    private TextView tvArtistBio;
    private LinearLayout layoutArtistSongsContainer;
    private LinearLayout layoutArtistAlbumsContainer;

    private ArtistRepository artistRepository;
    private AlbumRepository albumRepository;
    private SongRepository songRepository;
    private String artistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_artist_detail);

        artistRepository = new ArtistRepository(DatabaseHelper.getInstance(this));
        albumRepository = new AlbumRepository(DatabaseHelper.getInstance(this));
        songRepository = new SongRepository(DatabaseHelper.getInstance(this));

        initViews();
        artistId = getIntent().getStringExtra("ARTIST_ID");

        if (artistId == null || artistId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy nghệ sĩ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadArtistDetail();
    }

    private void initViews() {
        ivArtistImage = findViewById(R.id.ivArtistImage);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvArtistFollowers = findViewById(R.id.tvArtistFollowers);
        tvArtistBio = findViewById(R.id.tvArtistBio);
        layoutArtistSongsContainer = findViewById(R.id.layoutArtistSongsContainer);
        layoutArtistAlbumsContainer = findViewById(R.id.layoutArtistAlbumsContainer);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadArtistDetail() {
        new Thread(() -> {
            Optional<com.ptithcm.waveapp.model.Artist> artistOptional = artistRepository.findById(artistId);
            List<Song> songs = songRepository.findByArtistIdAndActiveTrue(artistId);
            List<Album> albums = albumRepository.findByArtistIdAndActiveTrue(artistId);

            runOnUiThread(() -> {
                if (artistOptional.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy nghệ sĩ", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                com.ptithcm.waveapp.model.Artist artist = artistOptional.get();
                bindArtistInfo(artist);
                bindSongs(songs);
                bindAlbums(albums);
            });
        }).start();
    }

    private void bindArtistInfo(com.ptithcm.waveapp.model.Artist artist) {
        tvArtistName.setText(artist.getName());
        tvArtistFollowers.setText("Follower: " + String.format(Locale.US, "%,d", artist.getFollowersCount()));
        tvArtistBio.setText(artist.getBio() == null || artist.getBio().trim().isEmpty()
                ? "Chưa có mô tả nghệ sĩ."
                : artist.getBio());

        Glide.with(this)
                .load(artist.getImage())
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .centerCrop()
                .into(ivArtistImage);
    }

    private void bindSongs(List<Song> songs) {
        layoutArtistSongsContainer.removeAllViews();

        if (songs.isEmpty()) {
            layoutArtistSongsContainer.addView(createEmptyStateView("Nghệ sĩ này chưa có bài hát."));
            return;
        }

        for (Song song : songs) {
            String albumName = song.getAlbum() != null && song.getAlbum().getName() != null
                    ? song.getAlbum().getName()
                    : "Chưa có album";
            layoutArtistSongsContainer.addView(createOverviewItemView(
                    song.getName(),
                    "Album: " + albumName,
                    "Lượt nghe: " + song.getPlayCount() + " • Yêu thích: " + song.getLikeCount(),
                    song.getImage()
            ));
        }
    }

    private void bindAlbums(List<Album> albums) {
        layoutArtistAlbumsContainer.removeAllViews();

        if (albums.isEmpty()) {
            layoutArtistAlbumsContainer.addView(createEmptyStateView("Nghệ sĩ này chưa có album."));
            return;
        }

        for (Album album : albums) {
            String artistName = album.getArtist() != null && album.getArtist().getName() != null
                    ? album.getArtist().getName()
                    : "Chưa có nghệ sĩ";
            String releaseDate = album.getReleaseDate() != null
                    ? album.getReleaseDate().toString()
                    : "Chưa có ngày phát hành";
            layoutArtistAlbumsContainer.addView(createOverviewItemView(
                    album.getName(),
                    "Nghệ sĩ: " + artistName,
                    "Ngày phát hành: " + releaseDate,
                    album.getImage()
            ));
        }
    }

    private View createOverviewItemView(String title, String subtitle, String meta, String imageUrl) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_admin_overview, null, false);
        ImageView imageView = itemView.findViewById(R.id.ivAdminItemImage);
        TextView titleView = itemView.findViewById(R.id.tvAdminItemTitle);
        TextView subtitleView = itemView.findViewById(R.id.tvAdminItemSubtitle);
        TextView metaView = itemView.findViewById(R.id.tvAdminItemMeta);
        View moreButton = itemView.findViewById(R.id.btnAdminItemMore);

        titleView.setText(title);
        subtitleView.setText(subtitle);
        metaView.setText(meta);
        ImageFileHelper.loadIntoImageView(this, imageUrl, imageView, R.drawable.ic_logo);
        moreButton.setVisibility(View.GONE);
        return itemView;
    }

    private TextView createEmptyStateView(String text) {
        TextView emptyView = new TextView(this);
        emptyView.setText(text);
        emptyView.setTextColor(0xFFB3B3B3);
        emptyView.setTextSize(14);
        return emptyView;
    }
}
