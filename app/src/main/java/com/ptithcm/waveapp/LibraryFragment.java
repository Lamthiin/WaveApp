package com.ptithcm.waveapp;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ptithcm.waveapp.adapter.AlbumAdapter;
import com.ptithcm.waveapp.adapter.ArtistAdapter;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;
import com.ptithcm.waveapp.auth.LoginActivity;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryFragment extends Fragment {
    private static final int ALBUM_GRID_SPAN_COUNT = 3;

    private enum Tab { SONGS, ARTISTS, ALBUMS }
    private enum SortMode { DEFAULT, NAME_ASC, NAME_DESC }
    private Tab currentTab = Tab.SONGS;
    private SortMode currentSortMode = SortMode.DEFAULT;

    private UserProfileService userProfileService;
    private LikedSongRepository likedSongRepository;
    private LikedAlbumRepository likedAlbumRepository;
    private UserFollowArtistRepository followRepository;
    private TokenManager tokenManager;

    private RecyclerView recyclerView;
    private RecyclerView.ItemDecoration albumGridSpacingDecoration;
    private SongAdapter songAdapter;
    private ArtistAdapter artistAdapter;
    private AlbumAdapter albumAdapter;
    private ShapeableImageView imgLibraryProfile;
    private View layoutSearchLibrary;
    private View layoutLibrarySort;
    private ImageButton btnLibrarySort;
    private ImageButton btnLibraryLayout;
    private ImageButton btnLibrarySearch;
    private ImageButton btnLibraryAdd;
    
    private EditText etSearch;
    private TextView emptyTextView;
    private TextView tabSongs, tabArtists, tabAlbums;
    private TextView tvLibrarySortLabel;

    private List<Song> allSongs = new ArrayList<>();
    private List<Artist> allArtists = new ArrayList<>();
    private List<Album> allAlbums = new ArrayList<>();
    private boolean artistGridMode = false;
    private boolean albumGridMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        initServices(view);
        initViews(view);
        setupTabs();
        setupSearch();
        setupHeaderActions();
        
        loadData();

        return view;
    }

    private void initServices(View view) {
        ServiceLocator locator = ServiceLocator.getInstance();
        userProfileService = locator.getUserProfileService();
        likedSongRepository = locator.likedSongRepository;
        likedAlbumRepository = locator.likedAlbumRepository;
        followRepository = locator.userFollowArtistRepository;
        tokenManager = new TokenManager(requireContext());
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewLibrary);
        layoutSearchLibrary = view.findViewById(R.id.layoutSearchLibrary);
        layoutLibrarySort = view.findViewById(R.id.layoutLibrarySort);
        etSearch = view.findViewById(R.id.etSearchLibrary);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        tabSongs = view.findViewById(R.id.tabSongs);
        tabArtists = view.findViewById(R.id.tabArtists);
        tabAlbums = view.findViewById(R.id.tabAlbums);
        imgLibraryProfile = view.findViewById(R.id.imgLibraryProfile);
        btnLibrarySort = view.findViewById(R.id.btnLibrarySort);
        btnLibraryLayout = view.findViewById(R.id.btnLibraryLayout);
        btnLibrarySearch = view.findViewById(R.id.btnLibrarySearch);
        btnLibraryAdd = view.findViewById(R.id.btnLibraryAdd);
        tvLibrarySortLabel = view.findViewById(R.id.tvLibrarySortLabel);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        albumGridSpacingDecoration = new GridSpacingItemDecoration(ALBUM_GRID_SPAN_COUNT, dpToPx(12));

        songAdapter = new SongAdapter();
        songAdapter.setActionIconMode(SongAdapter.ActionIconMode.MORE);
        songAdapter.setShowIndex(false);
        artistAdapter = new ArtistAdapter();
        artistAdapter.setLayoutMode(ArtistAdapter.LayoutMode.LIST);
        albumAdapter = new AlbumAdapter();

        setupAdapterListeners();
        reloadProfileAvatar();
        updateLayoutButtonState();
        updateSortLabel();
    }

    private void setupAdapterListeners() {
        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
            intent.putExtra("SONG_ID", song.getId());
            intent.putExtra("SONG_DATA", song);
            intent.putExtra("QUEUE_LIST", new ArrayList<>(allSongs));
            startActivity(intent);
        });

        songAdapter.setOnMoreClickListener((song, position, anchor) -> showSongActions(song, anchor));

        artistAdapter.setOnArtistClickListener(artist -> {
            Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
            intent.putExtra("ARTIST_ID", artist.getId());
            startActivity(intent);
        });

        artistAdapter.setOnFollowClickListener((artist, position) -> {
            String userId = tokenManager.getUserId();
            if (userId == null) return;
            followRepository.deleteByUserIdAndArtistId(userId, artist.getId());
            allArtists.remove(artist);
            filterData(etSearch.getText().toString());
        });

        albumAdapter.setOnAlbumClickListener(album -> {
            Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
            intent.putExtra("ALBUM_ID", album.getId());
            startActivity(intent);
        });

        albumAdapter.setOnLikeClickListener((album, position) -> {
            String userId = tokenManager.getUserId();
            if (userId == null) return;
            likedAlbumRepository.deleteByUserIdAndAlbumId(userId, album.getId());
            allAlbums.remove(album);
            filterData(etSearch.getText().toString());
        });
    }

    private void setupTabs() {
        tabSongs.setOnClickListener(v -> selectTab(Tab.SONGS));
        tabArtists.setOnClickListener(v -> selectTab(Tab.ARTISTS));
        tabAlbums.setOnClickListener(v -> selectTab(Tab.ALBUMS));
    }

    private void setupHeaderActions() {
        if (imgLibraryProfile != null) {
            imgLibraryProfile.setOnClickListener(v -> showProfileDialog());
        }

        if (btnLibrarySearch != null) {
            btnLibrarySearch.setOnClickListener(v -> toggleSearchBar());
        }

        if (btnLibraryAdd != null) {
            btnLibraryAdd.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), MyPlaylistsActivity.class)));
        }

        if (btnLibrarySort != null) {
            btnLibrarySort.setOnClickListener(this::showSortMenu);
        }

        if (layoutLibrarySort != null) {
            layoutLibrarySort.setOnClickListener(this::showSortMenu);
        }

        if (btnLibraryLayout != null) {
            btnLibraryLayout.setOnClickListener(v -> toggleLayoutMode());
        }
    }

    private void selectTab(Tab tab) {
        if (currentTab == tab) return;
        currentTab = tab;
        currentSortMode = SortMode.DEFAULT;
        etSearch.setText(""); // Reset keyword
        updateTabUI();
        updateLayoutButtonState();
        updateSortLabel();
        loadData();
    }

    private void updateTabUI() {
        tabSongs.setBackgroundResource(currentTab == Tab.SONGS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabSongs.setTextColor(getResources().getColor(currentTab == Tab.SONGS ? R.color.black : R.color.white));

        tabArtists.setBackgroundResource(currentTab == Tab.ARTISTS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabArtists.setTextColor(getResources().getColor(currentTab == Tab.ARTISTS ? R.color.black : R.color.white));

        tabAlbums.setBackgroundResource(currentTab == Tab.ALBUMS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabAlbums.setTextColor(getResources().getColor(currentTab == Tab.ALBUMS ? R.color.black : R.color.white));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filterData(s.toString());
            }
        });
    }

    private void showSortMenu(View anchor) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_library_sort, null, false);

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setDimAmount(0.55f);
        }

        TextView tvSortDefault = dialogView.findViewById(R.id.tvSortDefault);
        TextView tvSortNameAsc = dialogView.findViewById(R.id.tvSortNameAsc);
        TextView tvSortNameDesc = dialogView.findViewById(R.id.tvSortNameDesc);
        View optionSortDefault = dialogView.findViewById(R.id.optionSortDefault);
        View optionSortNameAsc = dialogView.findViewById(R.id.optionSortNameAsc);
        View optionSortNameDesc = dialogView.findViewById(R.id.optionSortNameDesc);
        View cancelView = dialogView.findViewById(R.id.tvLibrarySortCancel);
        View checkDefault = dialogView.findViewById(R.id.ivSortDefaultCheck);
        View checkNameAsc = dialogView.findViewById(R.id.ivSortNameAscCheck);
        View checkNameDesc = dialogView.findViewById(R.id.ivSortNameDescCheck);

        int selectedColor = requireContext().getColor(R.color.spotify_green);
        int normalColor = requireContext().getColor(R.color.white);

        tvSortDefault.setTextColor(currentSortMode == SortMode.DEFAULT ? selectedColor : normalColor);
        tvSortNameAsc.setTextColor(currentSortMode == SortMode.NAME_ASC ? selectedColor : normalColor);
        tvSortNameDesc.setTextColor(currentSortMode == SortMode.NAME_DESC ? selectedColor : normalColor);
        checkDefault.setVisibility(currentSortMode == SortMode.DEFAULT ? View.VISIBLE : View.GONE);
        checkNameAsc.setVisibility(currentSortMode == SortMode.NAME_ASC ? View.VISIBLE : View.GONE);
        checkNameDesc.setVisibility(currentSortMode == SortMode.NAME_DESC ? View.VISIBLE : View.GONE);

        optionSortDefault.setOnClickListener(v -> {
            currentSortMode = SortMode.DEFAULT;
            updateSortLabel();
            applyCurrentState();
            dialog.dismiss();
        });

        optionSortNameAsc.setOnClickListener(v -> {
            currentSortMode = SortMode.NAME_ASC;
            updateSortLabel();
            applyCurrentState();
            dialog.dismiss();
        });

        optionSortNameDesc.setOnClickListener(v -> {
            currentSortMode = SortMode.NAME_DESC;
            updateSortLabel();
            applyCurrentState();
            dialog.dismiss();
        });

        cancelView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void toggleSearchBar() {
        if (layoutSearchLibrary == null) return;

        boolean shouldShow = layoutSearchLibrary.getVisibility() != View.VISIBLE;
        layoutSearchLibrary.setVisibility(shouldShow ? View.VISIBLE : View.GONE);

        if (etSearch == null) return;

        if (shouldShow) {
            etSearch.requestFocus();
        } else {
            etSearch.setText("");
            etSearch.clearFocus();
        }
    }

    private void toggleLayoutMode() {
        if (currentTab == Tab.SONGS || currentTab == Tab.ARTISTS) {
            return;
        }

        if (currentTab == Tab.ALBUMS) {
            albumGridMode = !albumGridMode;
        }

        updateLayoutButtonState();
        applyCurrentState();
    }

    private void updateLayoutButtonState() {
        if (btnLibraryLayout == null) return;

        if (currentTab == Tab.SONGS || currentTab == Tab.ARTISTS) {
            btnLibraryLayout.setVisibility(View.GONE);
            return;
        }

        btnLibraryLayout.setVisibility(View.VISIBLE);
        boolean showingGrid = albumGridMode;
        btnLibraryLayout.setImageResource(showingGrid ? R.drawable.ic_list_view : R.drawable.ic_grid_view);
    }

    private void updateSortLabel() {
        if (tvLibrarySortLabel == null) return;

        if (currentSortMode == SortMode.NAME_ASC) {
            tvLibrarySortLabel.setText("Tên A-Z");
        } else if (currentSortMode == SortMode.NAME_DESC) {
            tvLibrarySortLabel.setText("Tên Z-A");
        } else {
            tvLibrarySortLabel.setText("Gần đây");
        }
    }

    private void reloadProfileAvatar() {
        if (imgLibraryProfile == null) return;

        String avatar = tokenManager.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(avatar)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .into(imgLibraryProfile);
        } else {
            imgLibraryProfile.setImageResource(R.drawable.avatar_default);
        }
    }

    private void showProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_library_profile, null, false);

        ShapeableImageView imgAvatar = dialogView.findViewById(R.id.imgLibraryProfileAvatar);
        TextView tvName = dialogView.findViewById(R.id.tvLibraryProfileName);
        TextView tvEmail = dialogView.findViewById(R.id.tvLibraryProfileEmail);
        TextView tvRole = dialogView.findViewById(R.id.tvLibraryProfileRole);

        String avatar = tokenManager.getAvatar();
        String name = tokenManager.getName();
        String email = tokenManager.getEmail();
        String role = tokenManager.getRole();

        tvName.setText(name != null && !name.isEmpty() ? name : "Wave User");
        tvEmail.setText(email != null && !email.isEmpty() ? email : "Chưa có email");
        tvRole.setText(role != null && !role.isEmpty() ? role : "USER");

        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(avatar)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.avatar_default);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnOpenLibraryProfile).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(getActivity(), UserProfileActivity.class));
        });

        dialogView.findViewById(R.id.btnLogoutLibraryProfile).setOnClickListener(v -> {
            dialog.dismiss();
            performLogout();
        });

        dialog.show();
    }

    private void performLogout() {
        tokenManager.logout();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showSongActions(Song song, View anchor) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_library_song_actions, null, false);

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setDimAmount(0.55f);
        }

        View optionRemoveFavorite = dialogView.findViewById(R.id.optionRemoveFavorite);
        View optionViewArtist = dialogView.findViewById(R.id.optionViewArtist);
        View optionViewAlbum = dialogView.findViewById(R.id.optionViewAlbum);
        View cancelView = dialogView.findViewById(R.id.tvLibrarySongActionsCancel);

        optionViewArtist.setVisibility(song.getArtist() != null ? View.VISIBLE : View.GONE);
        optionViewAlbum.setVisibility(song.getAlbum() != null ? View.VISIBLE : View.GONE);

        optionRemoveFavorite.setOnClickListener(v -> {
            dialog.dismiss();
            removeLikedSong(song);
        });

        optionViewArtist.setOnClickListener(v -> {
            dialog.dismiss();
            openArtistDetail(song);
        });

        optionViewAlbum.setOnClickListener(v -> {
            dialog.dismiss();
            openAlbumDetail(song);
        });

        cancelView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void removeLikedSong(Song song) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        likedSongRepository.deleteByUserIdAndSongId(userId, song.getId());
        allSongs.remove(song);
        filterData(etSearch.getText().toString());
        Toast.makeText(requireContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
    }

    private void openArtistDetail(Song song) {
        if (song.getArtist() == null) return;

        Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
        intent.putExtra("ARTIST_ID", song.getArtist().getId());
        startActivity(intent);
    }

    private void openAlbumDetail(Song song) {
        if (song.getAlbum() == null) return;

        Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
        intent.putExtra("ALBUM_ID", song.getAlbum().getId());
        startActivity(intent);
    }

    private void loadData() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        new Thread(() -> {
            switch (currentTab) {
                case SONGS:
                    allSongs = userProfileService.getLikedSongs(userId);
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(this::applyCurrentState);
                    break;
                case ARTISTS:
                    allArtists = userProfileService.getFollowingArtists(userId);
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(this::applyCurrentState);
                    break;
                case ALBUMS:
                    allAlbums = userProfileService.getLikedAlbums(userId);
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(this::applyCurrentState);
                    break;
            }
        }).start();
    }

    private void filterData(String query) {
        String lowerQuery = query.toLowerCase().trim();
        applyCurrentState();
    }

    private void applyCurrentState() {
        if (getContext() == null) return;

        String lowerQuery = etSearch != null ? etSearch.getText().toString().toLowerCase().trim() : "";

        switch (currentTab) {
            case SONGS:
                configureRecyclerDecoration(false);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(songAdapter);
                List<Song> filteredSongs = allSongs.stream()
                        .filter(s -> lowerQuery.isEmpty()
                                || (s.getName() != null && s.getName().toLowerCase().contains(lowerQuery))
                                || (s.getArtist() != null && s.getArtist().getName() != null
                                && s.getArtist().getName().toLowerCase().contains(lowerQuery)))
                        .collect(Collectors.toList());
                sortSongs(filteredSongs);
                songAdapter.setSongs(filteredSongs);
                updateEmptyState(filteredSongs.isEmpty(),
                        lowerQuery.isEmpty() ? "Không có bài hát yêu thích" : "Không tìm thấy bài hát yêu thích phù hợp");
                break;
            case ARTISTS:
                configureRecyclerDecoration(false);
                artistAdapter.setLayoutMode(artistGridMode ? ArtistAdapter.LayoutMode.GRID : ArtistAdapter.LayoutMode.LIST);
                recyclerView.setLayoutManager(artistGridMode
                        ? new GridLayoutManager(getContext(), 2)
                        : new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(artistAdapter);
                List<Artist> filteredArtists = allArtists.stream()
                        .filter(a -> lowerQuery.isEmpty()
                                || (a.getName() != null && a.getName().toLowerCase().contains(lowerQuery)))
                        .collect(Collectors.toList());
                sortArtists(filteredArtists);
                artistAdapter.setArtists(filteredArtists);
                updateEmptyState(filteredArtists.isEmpty(),
                        lowerQuery.isEmpty() ? "Không có nghệ sĩ yêu thích" : "Không tìm thấy nghệ sĩ yêu thích phù hợp");
                break;
            case ALBUMS:
                configureRecyclerDecoration(albumGridMode);
                albumAdapter.setLayoutMode(albumGridMode ? AlbumAdapter.LayoutMode.GRID : AlbumAdapter.LayoutMode.LIST);
                recyclerView.setLayoutManager(albumGridMode
                        ? new GridLayoutManager(getContext(), ALBUM_GRID_SPAN_COUNT)
                        : new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(albumAdapter);
                List<Album> filteredAlbums = allAlbums.stream()
                        .filter(al -> lowerQuery.isEmpty()
                                || (al.getName() != null && al.getName().toLowerCase().contains(lowerQuery))
                                || (al.getArtist() != null && al.getArtist().getName() != null
                                && al.getArtist().getName().toLowerCase().contains(lowerQuery)))
                        .collect(Collectors.toList());
                sortAlbums(filteredAlbums);
                albumAdapter.setAlbums(filteredAlbums);
                updateEmptyState(filteredAlbums.isEmpty(),
                        lowerQuery.isEmpty() ? "Không có album yêu thích" : "Không tìm thấy album yêu thích phù hợp");
                break;
        }
    }

    private void sortSongs(List<Song> songs) {
        if (currentSortMode == SortMode.NAME_ASC) {
            songs.sort(Comparator.comparing(song -> safeLower(song.getName())));
        } else if (currentSortMode == SortMode.NAME_DESC) {
            songs.sort((a, b) -> safeLower(b.getName()).compareTo(safeLower(a.getName())));
        }
    }

    private void sortArtists(List<Artist> artists) {
        if (currentSortMode == SortMode.NAME_ASC) {
            artists.sort(Comparator.comparing(artist -> safeLower(artist.getName())));
        } else if (currentSortMode == SortMode.NAME_DESC) {
            artists.sort((a, b) -> safeLower(b.getName()).compareTo(safeLower(a.getName())));
        }
    }

    private void sortAlbums(List<Album> albums) {
        if (currentSortMode == SortMode.NAME_ASC) {
            albums.sort(Comparator.comparing(album -> safeLower(album.getName())));
        } else if (currentSortMode == SortMode.NAME_DESC) {
            albums.sort((a, b) -> safeLower(b.getName()).compareTo(safeLower(a.getName())));
        }
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void updateEmptyState(boolean isEmpty, String message) {
        emptyTextView.setText(message);
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void configureRecyclerDecoration(boolean isAlbumGrid) {
        while (recyclerView.getItemDecorationCount() > 0) {
            recyclerView.removeItemDecorationAt(0);
        }

        if (isAlbumGrid) {
            recyclerView.addItemDecoration(albumGridSpacingDecoration);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;

        GridSpacingItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            outRect.left = column == 0 ? 0 : spacing / 2;
            outRect.right = column == spanCount - 1 ? 0 : spacing / 2;
            outRect.top = position < spanCount ? 0 : spacing / 2;
            outRect.bottom = 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadProfileAvatar();
        loadData();
    }
}
