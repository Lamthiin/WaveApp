package com.ptithcm.waveapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.ptithcm.waveapp.adapter.AdminOverviewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.util.SearchNormalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AdminArtistManagementActivity extends BaseAdminActivity {

    private static final int PAGE_SIZE = 10;
    private static final String TAB_ACTIVE = "ACTIVE";
    private static final String TAB_HIDDEN = "HIDDEN";

    private final List<Artist> activeArtists = new ArrayList<>();
    private final List<Artist> hiddenArtists = new ArrayList<>();
    private final List<AdminOverviewAdapter.AdminOverviewItem> filteredArtistItems = new ArrayList<>();
    private AdminOverviewAdapter adapter;
    private ArtistRepository artistRepository;
    private EditText searchInput;
    private TextView filterActive;
    private TextView filterHidden;
    private ActivityResultLauncher<String> artistImagePickerLauncher;
    private ImageView dialogArtistPreview;
    private MaterialButton dialogChooseArtistImageButton;
    private String pendingArtistImageUrl;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private int visibleItemCount;
    private boolean isLoadingMore;
    private String currentArtistTab = TAB_ACTIVE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_overview_list);
        setupAdminChrome(R.id.tvHeaderTitle, R.id.tvAdminAvatar, R.id.bottomAdminNavigation,
                R.id.nav_admin_artists, "Quản lý nghệ sĩ");

        findViewById(R.id.tvSectionHint).setVisibility(View.GONE);
        artistRepository = new ArtistRepository(DatabaseHelper.getInstance(this));
        artistImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadArtistImage(uri);
                    }
                }
        );

        recyclerView = findViewById(R.id.rvAdminList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOverviewAdapter();
        adapter.setOnAdminOverviewActionListener(new AdminOverviewAdapter.OnAdminOverviewActionListener() {
            @Override
            public void onViewDetailClick(AdminOverviewAdapter.AdminOverviewItem item) {
                openArtistDetail(item.id);
            }

            @Override
            public void onEditClick(AdminOverviewAdapter.AdminOverviewItem item) {
                Artist artist = findArtistById(item.id);
                if (artist != null) {
                    showArtistFormDialog(artist);
                }
            }

            @Override
            public void onHideClick(AdminOverviewAdapter.AdminOverviewItem item) {
                Artist artist = findArtistById(item.id);
                if (artist != null) {
                    confirmHideArtist(artist);
                }
            }

            @Override
            public void onRestoreClick(AdminOverviewAdapter.AdminOverviewItem item) {
                Artist artist = findArtistById(item.id);
                if (artist != null) {
                    confirmRestoreArtist(artist);
                }
            }
        });
        adapter.setOnAdminOverviewItemClickListener(item -> {
            openArtistDetail(item.id);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isLoadingMore) {
                    return;
                }

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }

                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition >= adapter.getItemCount() - 2) {
                    loadMoreArtists();
                }
            }
        });

        searchInput = findViewById(R.id.etSearchAdmin);
        filterActive = findViewById(R.id.filterActive);
        filterHidden = findViewById(R.id.filterHidden);
        searchInput.setHint("Tìm nghệ sĩ theo tên...");
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterArtists(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        setupTabs();

        fabAdd = findViewById(R.id.fabAdminAdd);
        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> showArtistFormDialog(null));

        loadArtists();
    }

    private void loadArtists() {
        activeArtists.clear();
        hiddenArtists.clear();
        activeArtists.addAll(artistRepository.findByActiveTrue());
        hiddenArtists.addAll(artistRepository.findByHidden());
        filterArtists(searchInput != null ? searchInput.getText().toString() : "");
    }

    private void setupTabs() {
        filterActive.setOnClickListener(v -> {
            currentArtistTab = TAB_ACTIVE;
            updateTabUI();
            filterArtists(searchInput.getText().toString());
        });

        filterHidden.setOnClickListener(v -> {
            currentArtistTab = TAB_HIDDEN;
            updateTabUI();
            filterArtists(searchInput.getText().toString());
        });

        updateTabUI();
    }

    private void updateTabUI() {
        updateFilterUI(
                TAB_ACTIVE.equals(currentArtistTab) ? filterActive : filterHidden,
                TAB_ACTIVE.equals(currentArtistTab) ? filterHidden : filterActive
        );
        if (fabAdd != null) {
            fabAdd.setVisibility(TAB_ACTIVE.equals(currentArtistTab) ? View.VISIBLE : View.GONE);
        }
    }

    private void updateFilterUI(TextView active, TextView inactive) {
        active.setBackgroundColor(Color.parseColor("#1DB954"));
        active.setTextColor(Color.parseColor("#FFFFFF"));
        inactive.setBackgroundColor(Color.parseColor("#282828"));
        inactive.setTextColor(Color.parseColor("#B3B3B3"));
    }

    private void filterArtists(String query) {
        filteredArtistItems.clear();
        int displayIndex = 1;
        List<Artist> sourceArtists = TAB_ACTIVE.equals(currentArtistTab) ? activeArtists : hiddenArtists;

        for (Artist artist : sourceArtists) {
            String name = artist.getName() == null ? "" : artist.getName();
            String bio = artist.getBio() == null ? "" : artist.getBio();
            if (!SearchNormalizer.containsNormalized(name, query)
                    && !SearchNormalizer.containsNormalized(bio, query)) {
                continue;
            }

            String subtitle = String.format(
                    Locale.US,
                    "Follower: %,d",
                    artist.getFollowersCount()
            );
            String meta = bio.isEmpty() ? "Chưa có mô tả nghệ sĩ." : bio;
            filteredArtistItems.add(new AdminOverviewAdapter.AdminOverviewItem(
                    artist.getId(),
                    displayIndex + ".",
                    name,
                    subtitle,
                    meta,
                    artist.getImage(),
                    R.drawable.ic_logo,
                    TAB_HIDDEN.equals(currentArtistTab)
            ));
            displayIndex++;
        }

        visibleItemCount = 0;
        isLoadingMore = false;
        adapter.setItems(new ArrayList<>());
        loadMoreArtists();
    }

    private void loadMoreArtists() {
        if (visibleItemCount >= filteredArtistItems.size()) {
            isLoadingMore = false;
            return;
        }

        isLoadingMore = true;
        int nextCount = Math.min(visibleItemCount + PAGE_SIZE, filteredArtistItems.size());
        List<AdminOverviewAdapter.AdminOverviewItem> nextItems =
                new ArrayList<>(filteredArtistItems.subList(visibleItemCount, nextCount));
        adapter.addItems(nextItems);
        visibleItemCount = nextCount;
        isLoadingMore = false;
    }

    private void openArtistDetail(String artistId) {
        Artist artist = findArtistById(artistId);
        if (artist == null) {
            Toast.makeText(this, "Không tìm thấy nghệ sĩ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AdminArtistDetailActivity.class);
        intent.putExtra("ARTIST_ID", artist.getId());
        startActivity(intent);
    }

    private Artist findArtistById(String artistId) {
        List<Artist> sourceArtists = TAB_ACTIVE.equals(currentArtistTab) ? activeArtists : hiddenArtists;
        for (Artist artist : sourceArtists) {
            if (artist.getId() != null && artist.getId().equals(artistId)) {
                return artist;
            }
        }
        return null;
    }

    private void showArtistFormDialog(Artist artist) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_artist, null, false);
        EditText etArtistName = dialogView.findViewById(R.id.etArtistName);
        EditText etArtistBio = dialogView.findViewById(R.id.etArtistBio);
        dialogArtistPreview = dialogView.findViewById(R.id.ivArtistPreview);
        dialogChooseArtistImageButton = dialogView.findViewById(R.id.btnChooseArtistImage);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelArtistDialog);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveArtistDialog);

        pendingArtistImageUrl = "";
        if (artist != null) {
            etArtistName.setText(artist.getName());
            etArtistBio.setText(artist.getBio());
            pendingArtistImageUrl = artist.getImage();
        }
        bindArtistImagePreview(pendingArtistImageUrl);
        dialogChooseArtistImageButton.setOnClickListener(v -> artistImagePickerLauncher.launch("image/*"));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(artist == null ? "Thêm nghệ sĩ" : "Chỉnh sửa nghệ sĩ")
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setText(artist == null ? "Thêm" : "Lưu");
        btnSave.setOnClickListener(v -> {
            String name = etArtistName.getText().toString().trim();
            String bio = etArtistBio.getText().toString().trim();

            if (name.isEmpty()) {
                etArtistName.setError("Vui lòng nhập tên nghệ sĩ");
                return;
            }

            if (artist == null) {
                artistRepository.createArtist(name, pendingArtistImageUrl, bio);
                currentArtistTab = TAB_ACTIVE;
                updateTabUI();
                Toast.makeText(this, "Đã thêm nghệ sĩ mới", Toast.LENGTH_SHORT).show();
            } else {
                boolean updated = artistRepository.updateArtist(artist.getId(), name, pendingArtistImageUrl, bio);
                Toast.makeText(this, updated ? "Đã cập nhật nghệ sĩ" : "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }

            loadArtists();
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void bindArtistImagePreview(String imageUrl) {
        if (dialogArtistPreview == null) {
            return;
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .centerCrop()
                .into(dialogArtistPreview);
    }

    private void uploadArtistImage(Uri selectedUri) {
        if (dialogChooseArtistImageButton == null) {
            return;
        }

        dialogChooseArtistImageButton.setEnabled(false);
        dialogChooseArtistImageButton.setText("Đang tải ảnh...");

        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("artist_avatars/admin_" + UUID.randomUUID() + ".jpg");

        imageRef.putFile(selectedUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    pendingArtistImageUrl = downloadUri.toString();
                                    bindArtistImagePreview(pendingArtistImageUrl);
                                    dialogChooseArtistImageButton.setEnabled(true);
                                    dialogChooseArtistImageButton.setText("Chọn ảnh từ máy");
                                    Toast.makeText(this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    dialogChooseArtistImageButton.setEnabled(true);
                                    dialogChooseArtistImageButton.setText("Chọn ảnh từ máy");
                                    Toast.makeText(this, "Không lấy được link ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    dialogChooseArtistImageButton.setEnabled(true);
                    dialogChooseArtistImageButton.setText("Chọn ảnh từ máy");
                    Toast.makeText(this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmHideArtist(Artist artist) {
        new AlertDialog.Builder(this)
                .setTitle("Ẩn nghệ sĩ")
                .setMessage("Ẩn nghệ sĩ " + artist.getName() + " khỏi danh sách quản lý? Dữ liệu bài hát và album vẫn sẽ được giữ lại.")
                .setPositiveButton("Ẩn", (dialog, which) -> {
                    boolean hidden = artistRepository.hideArtist(artist.getId());
                    Toast.makeText(this, hidden ? "Đã ẩn nghệ sĩ" : "Ẩn nghệ sĩ thất bại", Toast.LENGTH_SHORT).show();
                    if (hidden) {
                        loadArtists();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmRestoreArtist(Artist artist) {
        new AlertDialog.Builder(this)
                .setTitle("Khôi phục nghệ sĩ")
                .setMessage("Khôi phục nghệ sĩ " + artist.getName() + " về danh sách active?")
                .setPositiveButton("Khôi phục", (dialog, which) -> {
                    boolean restored = artistRepository.restoreArtist(artist.getId());
                    Toast.makeText(this, restored ? "Đã khôi phục nghệ sĩ" : "Khôi phục nghệ sĩ thất bại", Toast.LENGTH_SHORT).show();
                    if (restored) {
                        loadArtists();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
