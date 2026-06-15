package com.ptithcm.waveapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ptithcm.waveapp.adapter.AdminOverviewAdapter;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.util.ImageFileHelper;
import com.ptithcm.waveapp.util.SearchNormalizer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdminAlbumManagementActivity extends BaseAdminActivity {

    private final List<Album> allAlbums = new ArrayList<>();
    private final List<Artist> artistOptions = new ArrayList<>();
    private final List<Song> availableSongOptions = new ArrayList<>();
    private final Set<String> selectedSongIds = new LinkedHashSet<>();
    private AdminOverviewAdapter adapter;
    private AlbumRepository albumRepository;
    private ArtistRepository artistRepository;
    private SongRepository songRepository;
    private EditText searchInput;

    private ActivityResultLauncher<String> albumImagePickerLauncher;
    private ImageView dialogAlbumPreview;
    private MaterialButton btnChooseAlbumImage;
    private MaterialButton btnChooseAlbumSongs;
    private TextView tvSelectedAlbumSongs;
    private String pendingAlbumImageUrl;
    private Uri pendingAlbumImageUri;
    private AutoCompleteTextView actAlbumArtist;
    private Artist selectedArtistOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_overview_list);
        setupAdminChrome(R.id.tvHeaderTitle, R.id.tvAdminAvatar, R.id.bottomAdminNavigation,
                R.id.nav_admin_albums, "Quản lý album");

        findViewById(R.id.hsvStatusFilter).setVisibility(View.GONE);
        findViewById(R.id.tvSectionHint).setVisibility(View.GONE);

        albumRepository = new AlbumRepository(DatabaseHelper.getInstance(this));
        artistRepository = new ArtistRepository(DatabaseHelper.getInstance(this));
        songRepository = new SongRepository(DatabaseHelper.getInstance(this));

        albumImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pendingAlbumImageUri = uri;
                        bindAlbumImagePreview(uri);
                    }
                }
        );

        RecyclerView recyclerView = findViewById(R.id.rvAdminList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOverviewAdapter();
        adapter.setActionMenuResId(R.menu.admin_album_actions_menu);
        adapter.setOnAdminOverviewActionListener(new AdminOverviewAdapter.OnAdminOverviewActionListener() {
            @Override
            public void onViewDetailClick(AdminOverviewAdapter.AdminOverviewItem item) {
                openAlbumDetail(item.id);
            }

            @Override
            public void onEditClick(AdminOverviewAdapter.AdminOverviewItem item) {
                Album album = findAlbumById(item.id);
                if (album != null) {
                    showAlbumFormDialog(album);
                }
            }

            @Override
            public void onDeleteClick(AdminOverviewAdapter.AdminOverviewItem item) {
                Album album = findAlbumById(item.id);
                if (album != null) {
                    confirmDeleteAlbum(album);
                }
            }
        });
        adapter.setOnAdminOverviewItemClickListener(item -> openAlbumDetail(item.id));
        recyclerView.setAdapter(adapter);

        searchInput = findViewById(R.id.etSearchAdmin);
        ImageButton clearSearchButton = findViewById(R.id.btnClearSearchAdmin);
        searchInput.setHint("Tìm album theo tên...");
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearSearchButton.setVisibility(s != null && s.length() > 0 ? View.VISIBLE : View.GONE);
                filterAlbums(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        clearSearchButton.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.clearFocus();
            clearSearchButton.setVisibility(View.GONE);
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAdminAdd);
        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> showAlbumFormDialog(null));

        loadAlbums();
    }

    private void loadAlbums() {
        allAlbums.clear();
        allAlbums.addAll(albumRepository.findByActiveTrue());
        filterAlbums(searchInput != null ? searchInput.getText().toString() : "");
    }

    private void filterAlbums(String query) {
        List<AdminOverviewAdapter.AdminOverviewItem> items = new ArrayList<>();
        int displayIndex = 1;

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
                    displayIndex + ".",
                    name,
                    subtitle,
                    meta,
                    album.getImage(),
                    R.drawable.ic_logo,
                    false,
                    true,
                    false
            ));
            displayIndex++;
        }

        adapter.setItems(items);
    }

    private Album findAlbumById(String albumId) {
        for (Album album : allAlbums) {
            if (album.getId() != null && album.getId().equals(albumId)) {
                return album;
            }
        }
        return null;
    }

    private void openAlbumDetail(String albumId) {
        Intent intent = new Intent(this, AdminAlbumDetailActivity.class);
        intent.putExtra("ALBUM_ID", albumId);
        startActivity(intent);
    }

    private void showAlbumFormDialog(Album album) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_album, null, false);
        EditText etAlbumName = dialogView.findViewById(R.id.etAlbumName);
        EditText etAlbumReleaseDate = dialogView.findViewById(R.id.etAlbumReleaseDate);
        actAlbumArtist = dialogView.findViewById(R.id.actAlbumArtist);
        dialogAlbumPreview = dialogView.findViewById(R.id.ivAlbumPreview);
        btnChooseAlbumImage = dialogView.findViewById(R.id.btnChooseAlbumImage);
        btnChooseAlbumSongs = dialogView.findViewById(R.id.btnChooseAlbumSongs);
        tvSelectedAlbumSongs = dialogView.findViewById(R.id.tvSelectedAlbumSongs);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelAlbumDialog);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveAlbumDialog);

        artistOptions.clear();
        artistOptions.addAll(artistRepository.findByActiveTrue());
        if (artistOptions.isEmpty()) {
            Toast.makeText(this, "Chưa có nghệ sĩ active để tạo album", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> artistNames = new ArrayList<>();
        for (Artist artistOption : artistOptions) {
            artistNames.add(artistOption.getName());
        }
        SearchableArtistAdapter artistAdapter = new SearchableArtistAdapter(this, artistNames);
        actAlbumArtist.setAdapter(artistAdapter);

        pendingAlbumImageUrl = "";
        pendingAlbumImageUri = null;
        selectedArtistOption = null;
        selectedSongIds.clear();
        etAlbumReleaseDate.setFocusable(false);
        etAlbumReleaseDate.setClickable(true);

        List<String> preselectedSongIds = new ArrayList<>();
        if (album != null) {
            etAlbumName.setText(album.getName());
            pendingAlbumImageUrl = album.getImage();
            if (album.getReleaseDate() != null) {
                etAlbumReleaseDate.setText(album.getReleaseDate().toString());
            }
            for (Song song : songRepository.findByAlbumIdAndActiveTrue(album.getId())) {
                if (song.getId() != null) {
                    preselectedSongIds.add(song.getId());
                }
            }
        }

        bindAlbumImagePreview(pendingAlbumImageUrl);
        updateSelectedSongsSummary();

        final List<String> initialSelectedSongIds = preselectedSongIds;
        actAlbumArtist.setOnClickListener(v -> actAlbumArtist.showDropDown());
        actAlbumArtist.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actAlbumArtist.showDropDown();
            }
        });
        actAlbumArtist.setOnItemClickListener((parent, view, position, id) -> {
            String artistName = artistAdapter.getItem(position);
            Artist matchedArtist = findArtistByName(artistName);
            selectedArtistOption = matchedArtist;
            if (matchedArtist == null) {
                clearSelectedArtistState();
                return;
            }

            boolean isEditingSameArtist = album != null
                    && album.getArtist() != null
                    && matchedArtist.getId() != null
                    && matchedArtist.getId().equals(album.getArtist().getId());
            loadSongsForArtist(matchedArtist.getId(), isEditingSameArtist ? initialSelectedSongIds : null);
        });
        actAlbumArtist.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String currentText = s == null ? "" : s.toString().trim();
                if (selectedArtistOption != null && currentText.equals(selectedArtistOption.getName())) {
                    return;
                }

                Artist exactMatchArtist = findArtistByName(currentText);
                if (exactMatchArtist != null) {
                    selectedArtistOption = exactMatchArtist;
                    return;
                }

                selectedArtistOption = null;
                clearSelectedArtistState();
            }
        });

        if (album != null && album.getArtist() != null) {
            selectedArtistOption = findArtistById(album.getArtist().getId());
            if (selectedArtistOption != null) {
                actAlbumArtist.setText(selectedArtistOption.getName(), false);
                loadSongsForArtist(selectedArtistOption.getId(), initialSelectedSongIds);
            }
        } else {
            actAlbumArtist.setText("", false);
        }

        btnChooseAlbumImage.setOnClickListener(v -> albumImagePickerLauncher.launch("image/*"));
        btnChooseAlbumSongs.setOnClickListener(v -> {
            if (selectedArtistOption == null) {
                Toast.makeText(this, "Vui lòng chọn nghệ sĩ trước khi chọn bài hát", Toast.LENGTH_SHORT).show();
                actAlbumArtist.requestFocus();
                actAlbumArtist.showDropDown();
                return;
            }
            showSongPickerDialog();
        });
        etAlbumReleaseDate.setOnClickListener(v -> showReleaseDatePicker(etAlbumReleaseDate));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        String saveLabel = album == null ? "Thêm" : "Lưu";
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setText(saveLabel);
        btnSave.setOnClickListener(v -> {
            String name = etAlbumName.getText().toString().trim();
            if (name.isEmpty()) {
                etAlbumName.setError("Vui lòng nhập tên album");
                return;
            }
            if (selectedArtistOption == null) {
                Toast.makeText(this, "Vui lòng chọn nghệ sĩ cho album", Toast.LENGTH_SHORT).show();
                actAlbumArtist.requestFocus();
                actAlbumArtist.showDropDown();
                return;
            }

            LocalDate releaseDate = null;
            String releaseText = etAlbumReleaseDate.getText().toString().trim();
            if (!releaseText.isEmpty()) {
                try {
                    releaseDate = LocalDate.parse(releaseText);
                } catch (Exception e) {
                    etAlbumReleaseDate.setError("Ngày phát hành theo dạng yyyy-MM-dd");
                    return;
                }
            }

            String artistId = selectedArtistOption.getId();

            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
            btnChooseAlbumImage.setEnabled(false);
            btnSave.setText(album == null ? "Đang thêm..." : "Đang lưu...");

            if (pendingAlbumImageUri != null) {
                ensureFirebaseAuthForUpload(album, name, artistId, releaseDate, dialog, btnSave, btnCancel, saveLabel);
                return;
            }

            persistAlbum(album, name, artistId, pendingAlbumImageUrl, releaseDate, dialog, btnSave, btnCancel);
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private Artist findArtistById(String artistId) {
        if (artistId == null) {
            return null;
        }
        for (Artist artist : artistOptions) {
            if (artistId.equals(artist.getId())) {
                return artist;
            }
        }
        return null;
    }

    private Artist findArtistByName(String artistName) {
        String normalizedName = SearchNormalizer.normalize(artistName);
        if (normalizedName.isEmpty()) {
            return null;
        }
        for (Artist artist : artistOptions) {
            if (SearchNormalizer.normalize(artist.getName()).equals(normalizedName)) {
                return artist;
            }
        }
        return null;
    }

    private void clearSelectedArtistState() {
        availableSongOptions.clear();
        selectedSongIds.clear();
        updateSelectedSongsSummary();
    }

    private void loadSongsForArtist(String artistId, List<String> preselectedSongIdList) {
        availableSongOptions.clear();
        availableSongOptions.addAll(songRepository.findByArtistIdAndActiveTrue(artistId));
        selectedSongIds.clear();
        if (preselectedSongIdList != null) {
            for (String songId : preselectedSongIdList) {
                for (Song song : availableSongOptions) {
                    if (songId.equals(song.getId())) {
                        selectedSongIds.add(songId);
                        break;
                    }
                }
            }
        }
        updateSelectedSongsSummary();
    }

    private void showSongPickerDialog() {
        if (availableSongOptions.isEmpty()) {
            Toast.makeText(this, "Nghệ sĩ này chưa có bài hát để chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_album_song_picker, null, false);
        RecyclerView recyclerView = dialogView.findViewById(R.id.rvAlbumSongPicker);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelSongPicker);
        MaterialButton btnDone = dialogView.findViewById(R.id.btnConfirmSongPicker);

        Set<String> tempSelectedSongIds = new LinkedHashSet<>(selectedSongIds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AlbumSongPickerAdapter pickerAdapter = new AlbumSongPickerAdapter(availableSongOptions, tempSelectedSongIds);
        recyclerView.setAdapter(pickerAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDone.setOnClickListener(v -> {
            selectedSongIds.clear();
            selectedSongIds.addAll(tempSelectedSongIds);
            updateSelectedSongsSummary();
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void updateSelectedSongsSummary() {
        if (tvSelectedAlbumSongs == null) {
            return;
        }
        if (selectedSongIds.isEmpty()) {
            tvSelectedAlbumSongs.setText("Chưa chọn bài hát nào cho album.");
            return;
        }

        List<String> selectedNames = new ArrayList<>();
        for (Song song : availableSongOptions) {
            if (selectedSongIds.contains(song.getId())) {
                selectedNames.add(song.getName());
            }
        }
        tvSelectedAlbumSongs.setText("Đã chọn " + selectedNames.size() + " bài hát: " + String.join(", ", selectedNames));
    }

    private void showReleaseDatePicker(EditText targetInput) {
        final Calendar calendar = Calendar.getInstance();
        String currentValue = targetInput.getText().toString().trim();
        if (!currentValue.isEmpty()) {
            try {
                LocalDate parsedDate = LocalDate.parse(currentValue);
                calendar.set(parsedDate.getYear(), parsedDate.getMonthValue() - 1, parsedDate.getDayOfMonth());
            } catch (Exception ignored) {
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        targetInput.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void bindAlbumImagePreview(Object imageSource) {
        if (dialogAlbumPreview == null) {
            return;
        }
        Glide.with(this)
                .load(imageSource)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .centerCrop()
                .into(dialogAlbumPreview);
    }

    private void ensureFirebaseAuthForUpload(Album album, String name, String artistId, LocalDate releaseDate,
                                             AlertDialog dialog, MaterialButton btnSave, MaterialButton btnCancel,
                                             String saveLabel) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            uploadAlbumImageToStorage(album, name, artistId, releaseDate, dialog, btnSave, btnCancel, saveLabel);
            return;
        }

        firebaseAuth.signInAnonymously()
                .addOnSuccessListener(authResult ->
                        uploadAlbumImageToStorage(album, name, artistId, releaseDate, dialog, btnSave, btnCancel, saveLabel))
                .addOnFailureListener(e -> {
                    restoreAlbumDialogActions(btnSave, btnCancel, saveLabel);
                    Toast.makeText(this, "Đăng nhập Firebase thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadAlbumImageToStorage(Album album, String name, String artistId, LocalDate releaseDate,
                                           AlertDialog dialog, MaterialButton btnSave, MaterialButton btnCancel,
                                           String saveLabel) {
        if (pendingAlbumImageUri == null) {
            persistAlbum(album, name, artistId, pendingAlbumImageUrl, releaseDate, dialog, btnSave, btnCancel);
            return;
        }

        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("album_covers/admin_" + UUID.randomUUID() + ".jpg");

        imageRef.putFile(pendingAlbumImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            pendingAlbumImageUrl = downloadUri.toString();
                            pendingAlbumImageUri = null;
                            persistAlbum(album, name, artistId, pendingAlbumImageUrl, releaseDate, dialog, btnSave, btnCancel);
                        }).addOnFailureListener(e -> {
                            restoreAlbumDialogActions(btnSave, btnCancel, saveLabel);
                            Toast.makeText(this, "Không lấy được link ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    restoreAlbumDialogActions(btnSave, btnCancel, saveLabel);
                    Toast.makeText(this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void persistAlbum(Album album, String name, String artistId, String imageUrl, LocalDate releaseDate,
                              AlertDialog dialog, MaterialButton btnSave, MaterialButton btnCancel) {
        List<String> chosenSongIds = new ArrayList<>(selectedSongIds);
        if (album == null) {
            albumRepository.createAlbum(name, artistId, imageUrl, releaseDate, chosenSongIds);
            Toast.makeText(this, "Đã thêm album mới", Toast.LENGTH_SHORT).show();
        } else {
            boolean updated = albumRepository.updateAlbum(album.getId(), name, artistId, imageUrl, releaseDate, chosenSongIds);
            Toast.makeText(this, updated ? "Đã cập nhật album" : "Cập nhật album thất bại", Toast.LENGTH_SHORT).show();
        }

        loadAlbums();
        dialog.dismiss();
    }

    private void restoreAlbumDialogActions(MaterialButton btnSave, MaterialButton btnCancel, String saveLabel) {
        btnSave.setEnabled(true);
        btnCancel.setEnabled(true);
        if (btnChooseAlbumImage != null) {
            btnChooseAlbumImage.setEnabled(true);
        }
        if (btnChooseAlbumSongs != null) {
            btnChooseAlbumSongs.setEnabled(true);
        }
        btnSave.setText(saveLabel);
    }

    private void confirmDeleteAlbum(Album album) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa album")
                .setMessage("Xóa album " + album.getName() + "? Các bài hát sẽ chỉ bị gỡ khỏi album, không bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    albumRepository.deleteById(album.getId());
                    Toast.makeText(this, "Đã xóa album", Toast.LENGTH_SHORT).show();
                    loadAlbums();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private static class SearchableArtistAdapter extends ArrayAdapter<String> implements Filterable {
        private final List<String> allItems;
        private final List<String> filteredItems;

        SearchableArtistAdapter(AdminAlbumManagementActivity context, List<String> items) {
            super(context, R.layout.item_spinner_dropdown, new ArrayList<>(items));
            this.allItems = new ArrayList<>(items);
            this.filteredItems = new ArrayList<>(items);
            setDropDownViewResource(R.layout.item_spinner_dropdown);
        }

        @Override
        public int getCount() {
            return filteredItems.size();
        }

        @Override
        public String getItem(int position) {
            return filteredItems.get(position);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    List<String> suggestions = new ArrayList<>();
                    String query = constraint == null ? "" : constraint.toString().trim();

                    if (query.isEmpty()) {
                        suggestions.addAll(allItems);
                    } else {
                        for (String item : allItems) {
                            if (SearchNormalizer.containsNormalized(item, query)) {
                                suggestions.add(item);
                            }
                        }
                    }

                    FilterResults results = new FilterResults();
                    results.values = suggestions;
                    results.count = suggestions.size();
                    return results;
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredItems.clear();
                    if (results != null && results.values instanceof List) {
                        filteredItems.addAll((List<String>) results.values);
                    }
                    clear();
                    addAll(filteredItems);
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    return resultValue == null ? "" : resultValue.toString();
                }
            };
        }
    }

    private static class AlbumSongPickerAdapter extends RecyclerView.Adapter<AlbumSongPickerAdapter.ViewHolder> {
        private final List<Song> songs;
        private final Set<String> selectedSongIds;

        AlbumSongPickerAdapter(List<Song> songs, Set<String> selectedSongIds) {
            this.songs = songs;
            this.selectedSongIds = selectedSongIds;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_song_picker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Song song = songs.get(position);
            holder.tvSongName.setText(song.getName());
            ImageFileHelper.loadIntoImageView(holder.itemView.getContext(), song.getImage(), holder.ivSongCover, R.drawable.ic_music_note);

            boolean isChecked = selectedSongIds.contains(song.getId());
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(isChecked);

            View.OnClickListener toggleListener = v -> holder.checkBox.toggle();
            holder.checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
                if (checked) {
                    selectedSongIds.add(song.getId());
                } else {
                    selectedSongIds.remove(song.getId());
                }
            });
            holder.itemView.setOnClickListener(toggleListener);
        }

        @Override
        public int getItemCount() {
            return songs.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView ivSongCover;
            private final TextView tvSongName;
            private final AppCompatCheckBox checkBox;

            ViewHolder(View itemView) {
                super(itemView);
                ivSongCover = itemView.findViewById(R.id.ivSongPickerCover);
                tvSongName = itemView.findViewById(R.id.tvSongPickerName);
                checkBox = itemView.findViewById(R.id.cbSongPicker);
            }
        }
    }
}
