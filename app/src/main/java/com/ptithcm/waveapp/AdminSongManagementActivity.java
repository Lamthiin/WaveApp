package com.ptithcm.waveapp;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ptithcm.waveapp.adapter.AdminOverviewAdapter;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Genre;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.GenreRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.util.SearchNormalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AdminSongManagementActivity extends BaseAdminActivity {

    private final List<Song> allSongs = new ArrayList<>();
    private AdminOverviewAdapter adapter;
    private SongRepository songRepository;
    private ArtistRepository artistRepository;
    private GenreRepository genreRepository;
    private AlbumRepository albumRepository;
    private EditText searchInput;

    private ActivityResultLauncher<String> songCoverPickerLauncher;
    private ActivityResultLauncher<String> songAudioPickerLauncher;
    private ImageView dialogSongCoverPreview;
    private MaterialButton btnChooseSongCover;
    private MaterialButton btnChooseSongAudio;
    private TextView tvSongAudioStatus;
    private String pendingSongImageUrl;
    private String pendingSongUrl;
    private int pendingSongDuration;
    private Uri pendingSongCoverUri;
    private Uri pendingSongAudioUri;
    private Spinner spinnerArtist;
    private Spinner spinnerGenre;
    private Spinner spinnerAlbum;
    private List<Artist> artistOptions = new ArrayList<>();
    private List<Genre> genreOptions = new ArrayList<>();
    private List<Album> albumOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_overview_list);
        setupAdminChrome(R.id.tvHeaderTitle, R.id.tvAdminAvatar, R.id.bottomAdminNavigation,
                R.id.nav_admin_songs, "Quản lý bài hát");

        findViewById(R.id.hsvStatusFilter).setVisibility(View.GONE);
        findViewById(R.id.tvSectionHint).setVisibility(View.GONE);

        songRepository = new SongRepository(DatabaseHelper.getInstance(this));
        artistRepository = new ArtistRepository(DatabaseHelper.getInstance(this));
        genreRepository = new GenreRepository(DatabaseHelper.getInstance(this));
        albumRepository = new AlbumRepository(DatabaseHelper.getInstance(this));

        songCoverPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pendingSongCoverUri = uri;
                        bindSongCoverPreview(uri);
                    }
                }
        );
        songAudioPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pendingSongAudioUri = uri;
                        pendingSongDuration = extractAudioDurationSeconds(uri);
                        updateSongAudioStatus();
                    }
                }
        );

        RecyclerView recyclerView = findViewById(R.id.rvAdminList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOverviewAdapter();
        adapter.setActionMenuResId(R.menu.admin_song_actions_menu);
        adapter.setOnAdminOverviewActionListener(new AdminOverviewAdapter.OnAdminOverviewActionListener() {
            @Override
            public void onViewDetailClick(AdminOverviewAdapter.AdminOverviewItem item) {
            }

            @Override
            public void onEditClick(AdminOverviewAdapter.AdminOverviewItem item) {
                Song song = findSongById(item.id);
                if (song != null) {
                    showSongFormDialog(song);
                }
            }

            @Override
            public void onDeleteClick(AdminOverviewAdapter.AdminOverviewItem item) {
                Song song = findSongById(item.id);
                if (song != null) {
                    confirmDeleteSong(song);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        searchInput = findViewById(R.id.etSearchAdmin);
        ImageButton clearSearchButton = findViewById(R.id.btnClearSearchAdmin);
        searchInput.setHint("Tìm bài hát theo tên...");
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearSearchButton.setVisibility(s != null && s.length() > 0 ? View.VISIBLE : View.GONE);
                filterSongs(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        clearSearchButton.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.clearFocus();
            clearSearchButton.setVisibility(View.GONE);
        });

        com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd = findViewById(R.id.fabAdminAdd);
        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> showSongFormDialog(null));

        loadSongs();
    }

    private void loadSongs() {
        allSongs.clear();
        allSongs.addAll(songRepository.findAll());
        filterSongs(searchInput != null ? searchInput.getText().toString() : "");
    }

    private void filterSongs(String query) {
        List<AdminOverviewAdapter.AdminOverviewItem> items = new ArrayList<>();
        int displayIndex = 1;

        for (Song song : allSongs) {
            String name = song.getName() == null ? "" : song.getName();
            String artistName = song.getArtist() != null && song.getArtist().getName() != null
                    ? song.getArtist().getName() : "Chưa rõ nghệ sĩ";
            String genreName = song.getGenre() != null && song.getGenre().getName() != null
                    ? song.getGenre().getName() : "Chưa có thể loại";

            if (!SearchNormalizer.containsNormalized(name, query)
                    && !SearchNormalizer.containsNormalized(artistName, query)
                    && !SearchNormalizer.containsNormalized(genreName, query)) {
                continue;
            }

            String subtitle = artistName + " • " + genreName;
            String meta = "Thời lượng: " + formatDuration(song.getDuration())
                    + " • Lượt nghe: " + song.getPlayCount()
                    + " • Lượt yêu thích: " + song.getLikeCount();
            items.add(new AdminOverviewAdapter.AdminOverviewItem(
                    song.getId(),
                    displayIndex + ".",
                    name,
                    subtitle,
                    meta,
                    song.getImage(),
                    R.drawable.ic_music_note,
                    false,
                    true,
                    false
            ));
            displayIndex++;
        }

        adapter.setItems(items);
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return String.format(Locale.US, "%d:%02d", minutes, remainder);
    }

    private Song findSongById(String songId) {
        for (Song song : allSongs) {
            if (song.getId() != null && song.getId().equals(songId)) {
                return song;
            }
        }
        return null;
    }

    private void showSongFormDialog(Song song) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_song, null, false);
        EditText etSongName = dialogView.findViewById(R.id.etSongName);
        EditText etSongLyrics = dialogView.findViewById(R.id.etSongLyrics);
        spinnerArtist = dialogView.findViewById(R.id.spinnerSongArtist);
        spinnerGenre = dialogView.findViewById(R.id.spinnerSongGenre);
        spinnerAlbum = dialogView.findViewById(R.id.spinnerSongAlbum);
        dialogSongCoverPreview = dialogView.findViewById(R.id.ivSongCoverPreview);
        btnChooseSongCover = dialogView.findViewById(R.id.btnChooseSongCover);
        btnChooseSongAudio = dialogView.findViewById(R.id.btnChooseSongAudio);
        tvSongAudioStatus = dialogView.findViewById(R.id.tvSongAudioStatus);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelSongDialog);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveSongDialog);

        artistOptions = artistRepository.findByActiveTrue();
        genreOptions = genreRepository.findAll();
        albumOptions = albumRepository.findByActiveTrue();

        List<String> artistNames = new ArrayList<>();
        for (Artist artist : artistOptions) artistNames.add(artist.getName());
        ArrayAdapter<String> artistAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, artistNames);
        artistAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerArtist.setAdapter(artistAdapter);

        List<String> genreNames = new ArrayList<>();
        for (Genre genre : genreOptions) genreNames.add(genre.getName());
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, genreNames);
        genreAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerGenre.setAdapter(genreAdapter);

        List<String> albumNames = new ArrayList<>();
        albumNames.add("Không chọn album");
        for (Album album : albumOptions) albumNames.add(album.getName());
        ArrayAdapter<String> albumAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, albumNames);
        albumAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerAlbum.setAdapter(albumAdapter);

        pendingSongImageUrl = "";
        pendingSongUrl = "";
        pendingSongDuration = 0;
        pendingSongCoverUri = null;
        pendingSongAudioUri = null;

        if (song != null) {
            etSongName.setText(song.getName());
            etSongLyrics.setText(song.getLyrics());
            pendingSongImageUrl = song.getImage();
            pendingSongUrl = song.getUrl();
            pendingSongDuration = song.getDuration();

            if (song.getArtist() != null) {
                int idx = findArtistIndex(song.getArtist().getId());
                if (idx >= 0) spinnerArtist.setSelection(idx);
            }
            if (song.getGenre() != null) {
                int idx = findGenreIndex(song.getGenre().getId());
                if (idx >= 0) spinnerGenre.setSelection(idx);
            }
            if (song.getAlbum() != null) {
                int idx = findAlbumIndex(song.getAlbum().getId());
                if (idx >= 0) spinnerAlbum.setSelection(idx + 1);
            }
        }

        bindSongCoverPreview(pendingSongImageUrl);
        updateSongAudioStatus();

        btnChooseSongCover.setOnClickListener(v -> songCoverPickerLauncher.launch("image/*"));
        btnChooseSongAudio.setOnClickListener(v -> songAudioPickerLauncher.launch("audio/*"));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        String saveLabel = song == null ? "Thêm" : "Lưu";
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setText(saveLabel);
        btnSave.setOnClickListener(v -> {
            String name = etSongName.getText().toString().trim();
            if (name.isEmpty()) {
                etSongName.setError("Vui lòng nhập tên bài hát");
                return;
            }
            if (artistOptions.isEmpty()) {
                Toast.makeText(this, "Chưa có nghệ sĩ nào, vui lòng thêm nghệ sĩ trước", Toast.LENGTH_SHORT).show();
                return;
            }
            if (genreOptions.isEmpty()) {
                Toast.makeText(this, "Chưa có thể loại nào, vui lòng thêm thể loại trước", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean hasAudio = pendingSongAudioUri != null
                    || (pendingSongUrl != null && !pendingSongUrl.trim().isEmpty());
            if (!hasAudio) {
                Toast.makeText(this, "Vui lòng chọn file nhạc để upload", Toast.LENGTH_SHORT).show();
                return;
            }

            String artistId = artistOptions.get(spinnerArtist.getSelectedItemPosition()).getId();
            String genreId = genreOptions.get(spinnerGenre.getSelectedItemPosition()).getId();
            int albumPos = spinnerAlbum.getSelectedItemPosition();
            String albumId = albumPos == 0 ? null : albumOptions.get(albumPos - 1).getId();

            String lyrics = etSongLyrics.getText().toString().trim();

            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
            btnChooseSongCover.setEnabled(false);
            btnChooseSongAudio.setEnabled(false);
            btnSave.setText("Đang lưu...");
            dialog.setCancelable(false);

            saveSong(song, name, artistId, albumId, genreId, lyrics, dialog, btnSave, btnCancel, saveLabel);
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private int findArtistIndex(String artistId) {
        if (artistId == null) return -1;
        for (int i = 0; i < artistOptions.size(); i++) {
            if (artistId.equals(artistOptions.get(i).getId())) return i;
        }
        return -1;
    }

    private int findGenreIndex(String genreId) {
        if (genreId == null) return -1;
        for (int i = 0; i < genreOptions.size(); i++) {
            if (genreId.equals(genreOptions.get(i).getId())) return i;
        }
        return -1;
    }

    private int findAlbumIndex(String albumId) {
        if (albumId == null) return -1;
        for (int i = 0; i < albumOptions.size(); i++) {
            if (albumId.equals(albumOptions.get(i).getId())) return i;
        }
        return -1;
    }

    private void bindSongCoverPreview(Object imageSource) {
        if (dialogSongCoverPreview == null) return;

        Glide.with(this)
                .load(imageSource)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .centerCrop()
                .into(dialogSongCoverPreview);
    }

    private void updateSongAudioStatus() {
        if (tvSongAudioStatus == null) return;
        if (pendingSongAudioUri != null) {
            tvSongAudioStatus.setText("Đã chọn file nhạc (chưa lưu) • Thời lượng: " + formatDuration(pendingSongDuration));
        } else if (pendingSongUrl != null && !pendingSongUrl.trim().isEmpty()) {
            tvSongAudioStatus.setText("Đã có file nhạc • Thời lượng: " + formatDuration(pendingSongDuration));
        } else {
            tvSongAudioStatus.setText("Chưa chọn file nhạc");
        }
    }

    private void saveSong(Song song, String name, String artistId, String albumId, String genreId, String lyrics,
                           AlertDialog dialog, MaterialButton btnSave, MaterialButton btnCancel, String saveLabel) {
        if (pendingSongCoverUri != null) {
            Uri coverUri = pendingSongCoverUri;
            uploadFile("songs_covers/" + getFileName(coverUri), coverUri,
                    url -> {
                        pendingSongImageUrl = url;
                        pendingSongCoverUri = null;
                        saveSong(song, name, artistId, albumId, genreId, lyrics, dialog, btnSave, btnCancel, saveLabel);
                    },
                    e -> onSaveFailed("Upload ảnh thất bại: " + e.getMessage(), dialog, btnSave, btnCancel, saveLabel));
            return;
        }
        if (pendingSongAudioUri != null) {
            Uri audioUri = pendingSongAudioUri;
            uploadFile("songs_files/" + getFileName(audioUri), audioUri,
                    url -> {
                        pendingSongUrl = url;
                        pendingSongAudioUri = null;
                        saveSong(song, name, artistId, albumId, genreId, lyrics, dialog, btnSave, btnCancel, saveLabel);
                    },
                    e -> onSaveFailed("Upload file nhạc thất bại: " + e.getMessage(), dialog, btnSave, btnCancel, saveLabel));
            return;
        }

        if (song == null) {
            songRepository.createSong(name, artistId, albumId, genreId, pendingSongDuration, pendingSongUrl, pendingSongImageUrl, lyrics);
            Toast.makeText(this, "Đã thêm bài hát mới", Toast.LENGTH_SHORT).show();
        } else {
            boolean updated = songRepository.updateSong(song.getId(), name, artistId, albumId, genreId, pendingSongDuration, pendingSongUrl, pendingSongImageUrl, lyrics);
            Toast.makeText(this, updated ? "Đã cập nhật bài hát" : "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }

        loadSongs();
        dialog.dismiss();
    }

    private void onSaveFailed(String message, AlertDialog dialog, MaterialButton btnSave, MaterialButton btnCancel, String saveLabel) {
        btnSave.setEnabled(true);
        btnCancel.setEnabled(true);
        btnChooseSongCover.setEnabled(true);
        btnChooseSongAudio.setEnabled(true);
        btnSave.setText(saveLabel);
        dialog.setCancelable(true);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void uploadFile(String path, Uri uri, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(path);
        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> onSuccess.onSuccess(downloadUri.toString()))
                        .addOnFailureListener(onFailure))
                .addOnFailureListener(onFailure);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "admin_" + UUID.randomUUID();
    }

    private int extractAudioDurationSeconds(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            String durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return durationMs != null ? (int) (Long.parseLong(durationMs) / 1000) : 0;
        } catch (Exception e) {
            return 0;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private void confirmDeleteSong(Song song) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài hát")
                .setMessage("Bạn có chắc chắn muốn xóa bài hát \"" + song.getName() + "\" không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    songRepository.deleteById(song.getId());
                    Toast.makeText(this, "Đã xóa bài hát", Toast.LENGTH_SHORT).show();
                    loadSongs();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
