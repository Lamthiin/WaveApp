package com.ptithcm.waveapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AdminAlbumDetailActivity extends AppCompatActivity {

    private ShapeableImageView ivAlbumImage;
    private TextView tvAlbumName;
    private TextView tvAlbumReleaseDate;
    private TextView tvAlbumPlayCount;
    private LinearLayout layoutAlbumSongsContainer;

    private AlbumRepository albumRepository;
    private SongRepository songRepository;
    private String albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_album_detail);

        albumRepository = new AlbumRepository(DatabaseHelper.getInstance(this));
        songRepository = new SongRepository(DatabaseHelper.getInstance(this));

        initViews();
        albumId = getIntent().getStringExtra("ALBUM_ID");
        if (albumId == null || albumId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy album", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadAlbumDetail();
    }

    private void initViews() {
        ivAlbumImage = findViewById(R.id.ivAlbumImage);
        tvAlbumName = findViewById(R.id.tvAlbumName);
        tvAlbumReleaseDate = findViewById(R.id.tvAlbumReleaseDate);
        tvAlbumPlayCount = findViewById(R.id.tvAlbumPlayCount);
        layoutAlbumSongsContainer = findViewById(R.id.layoutAlbumSongsContainer);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAlbumDetail() {
        new Thread(() -> {
            Optional<Album> albumOptional = albumRepository.findById(albumId);
            List<Song> songs = songRepository.findByAlbumIdAndActiveTrue(albumId);

            runOnUiThread(() -> {
                if (albumOptional.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy album", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                bindAlbumInfo(albumOptional.get());
                bindSongs(songs);
            });
        }).start();
    }

    private void bindAlbumInfo(Album album) {
        String releaseDate = album.getReleaseDate() != null
                ? album.getReleaseDate().toString()
                : "Chưa có ngày phát hành";

        tvAlbumName.setText(album.getName());
        tvAlbumReleaseDate.setText("Ngày phát hành: " + releaseDate);
        tvAlbumPlayCount.setText(String.format(Locale.US, "%,d lượt nghe", album.getPlayCount()));

        Glide.with(this)
                .load(album.getImage())
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .fitCenter()
                .into(ivAlbumImage);
    }

    private void bindSongs(List<Song> songs) {
        layoutAlbumSongsContainer.removeAllViews();
        if (songs.isEmpty()) {
            layoutAlbumSongsContainer.addView(createEmptyStateView("Album này chưa có bài hát."));
            return;
        }

        for (Song song : songs) {
            String genreName = song.getGenre() != null && song.getGenre().getName() != null
                    ? song.getGenre().getName()
                    : "Chưa có thể loại";
            layoutAlbumSongsContainer.addView(createOverviewItemView(
                    song,
                    song.getName(),
                    "Thể loại: " + genreName,
                    "Thời lượng: " + formatDuration(song.getDuration()) + " • Lượt nghe: " + String.format(Locale.US, "%,d", song.getPlayCount()),
                    song.getImage()
            ));
        }
    }

    private View createOverviewItemView(Song song, String title, String subtitle, String meta, String imageUrl) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_admin_album_detail_song, layoutAlbumSongsContainer, false);
        ImageView imageView = itemView.findViewById(R.id.ivSongImage);
        TextView titleView = itemView.findViewById(R.id.tvSongName);
        TextView subtitleView = itemView.findViewById(R.id.tvSongSubtitle);
        TextView metaView = itemView.findViewById(R.id.tvSongMeta);
        ImageButton moreButton = itemView.findViewById(R.id.btnSongMore);

        titleView.setText(title);
        subtitleView.setText(subtitle);
        metaView.setText(meta);
        ImageFileHelper.loadIntoImageView(this, imageUrl, imageView, R.drawable.ic_music_note);
        moreButton.setOnClickListener(v -> showSongItemMenu(v, song));
        return itemView;
    }

    private void showSongItemMenu(View anchor, Song song) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.admin_album_song_item_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_remove_from_album) {
                confirmRemoveSongFromAlbum(song);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void confirmRemoveSongFromAlbum(Song song) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài hát khỏi album")
                .setMessage("Gỡ bài hát " + song.getName() + " ra khỏi album này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> removeSongFromAlbum(song))
                .show();
    }

    private void removeSongFromAlbum(Song song) {
        new Thread(() -> {
            boolean removed = songRepository.removeSongFromAlbum(song.getId(), albumId);
            runOnUiThread(() -> {
                if (removed) {
                    Toast.makeText(this, "Đã xóa bài hát khỏi album", Toast.LENGTH_SHORT).show();
                    loadAlbumDetail();
                } else {
                    Toast.makeText(this, "Không thể xóa bài hát khỏi album", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private TextView createEmptyStateView(String text) {
        TextView emptyView = new TextView(this);
        emptyView.setText(text);
        emptyView.setTextColor(0xFFB3B3B3);
        emptyView.setTextSize(14);
        return emptyView;
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return String.format(Locale.US, "%d:%02d", minutes, remainder);
    }
}
