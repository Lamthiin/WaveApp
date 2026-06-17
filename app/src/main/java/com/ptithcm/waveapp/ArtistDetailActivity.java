package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.adapter.SongAdapter;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.model.LikedSong;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.model.UserFollowArtist;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.util.TokenManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

public class ArtistDetailActivity extends BaseMiniPlayerActivity {

    private ImageView imgArtist;
    private TextView tvArtistName;
    private TextView tvMonthlyListeners;
    private AppCompatButton btnFollow;
    private ImageButton btnBack;
    private ImageButton btnShuffle;
    private ImageButton btnPlay;
    private RecyclerView rvPopularSongs;

    private SongAdapter songAdapter;
    private Artist currentArtist;
    private String artistId;
    private final List<Song> songList = new ArrayList<>();
    private final Random random = new Random();

    private SongRepository songRepo;
    private ArtistRepository artistRepo;
    private UserFollowArtistRepository followRepo;
    private LikedSongRepository likedSongRepo;
    private PlaylistService playlistService;
    private TokenManager tokenManager;

    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        initRepositories();
        initViews();
        setupRecyclerView();
        setupListeners();
        handleIntent();
        loadArtistData();
    }

    private void initRepositories() {
        ServiceLocator locator = ServiceLocator.getInstance();
        songRepo = locator.songRepository;
        artistRepo = locator.artistRepository;
        followRepo = locator.userFollowArtistRepository;
        likedSongRepo = locator.likedSongRepository;
        playlistService = locator.getPlaylistService();
        tokenManager = new TokenManager(this);
    }

    private void initViews() {
        imgArtist = findViewById(R.id.imageView3);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvMonthlyListeners = findViewById(R.id.tvMonthlyListeners);
        btnFollow = findViewById(R.id.btnFollow);
        btnBack = findViewById(R.id.btnBack);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnPlay = findViewById(R.id.btnPlay);
        rvPopularSongs = findViewById(R.id.rvPopularSongs);
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter();
        rvPopularSongs.setLayoutManager(new LinearLayoutManager(this));
        rvPopularSongs.setAdapter(songAdapter);

        songAdapter.setOnSongClickListener(this::openMusicPlayer);
        songAdapter.setOnLikeClickListener(this::toggleLikeSong);
        songAdapter.setOnMoreClickListener((song, position, anchor) -> showSongOptions(song, position));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFollow.setOnClickListener(v -> toggleFollow());
        btnPlay.setOnClickListener(v -> playTopSong());
        btnShuffle.setOnClickListener(v -> shufflePlay());
    }

    private void handleIntent() {
        currentArtist = (Artist) getIntent().getSerializableExtra("ARTIST_DATA");
        if (currentArtist != null) {
            artistId = currentArtist.getId();
            return;
        }

        artistId = getIntent().getStringExtra("ARTIST_ID");
        if (artistId == null || artistId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy nghệ sĩ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadArtistData() {
        if (artistId == null || artistId.trim().isEmpty()) return;

        new Thread(() -> {
            Optional<Artist> artistOptional = artistRepo.findById(artistId);
            if (artistOptional.isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không tìm thấy nghệ sĩ", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            Artist artist = artistOptional.get();
            List<Song> songs = new ArrayList<>(songRepo.findByArtistIdAndActiveTrue(artistId));
            Collections.sort(songs, Comparator.comparingLong(Song::getPlayCount).reversed());

            String userId = tokenManager.getUserId();
            boolean following = userId != null && followRepo.existsByUserIdAndArtistId(userId, artistId);

            runOnUiThread(() -> {
                currentArtist = artist;
                isFollowing = following;
                songList.clear();
                songList.addAll(songs);
                bindArtistData();
                updateFollowButton();
                songAdapter.setSongs(songList);
            });
        }).start();
    }

    private void bindArtistData() {
        if (currentArtist == null) return;

        tvArtistName.setText(currentArtist.getName());
        String formattedFollowers = String.format(Locale.US, "%,d", currentArtist.getFollowersCount());
        tvMonthlyListeners.setText(formattedFollowers + " người theo dõi");

        Glide.with(this)
                .load(currentArtist.getImage())
                .placeholder(R.drawable.sontung)
                .error(R.drawable.sontung)
                .into(imgArtist);
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText(R.string.following);
            btnFollow.setBackgroundResource(R.drawable.bg_following_button);
        } else {
            btnFollow.setText(R.string.follow);
            btnFollow.setBackgroundResource(R.drawable.bg_follow_button);
        }
    }

    private void toggleFollow() {
        if (currentArtist == null) return;

        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Đăng nhập để theo dõi nghệ sĩ", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (isFollowing) {
                followRepo.deleteByUserIdAndArtistId(userId, currentArtist.getId());
                artistRepo.decrementFollowers(currentArtist.getId());
            } else {
                User user = new User();
                user.setId(userId);

                UserFollowArtist follow = new UserFollowArtist();
                follow.setUser(user);
                follow.setArtist(currentArtist);
                follow.setFollowedAt(LocalDateTime.now().toString());

                followRepo.save(follow);
                artistRepo.incrementFollowers(currentArtist.getId());
            }

            Optional<Artist> updatedArtist = artistRepo.findById(currentArtist.getId());
            runOnUiThread(() -> {
                isFollowing = !isFollowing;
                if (updatedArtist.isPresent()) {
                    currentArtist = updatedArtist.get();
                    bindArtistData();
                }
                updateFollowButton();
            });
        }).start();
    }

    private void playTopSong() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "Nghệ sĩ này chưa có bài hát", Toast.LENGTH_SHORT).show();
            return;
        }
        openMusicPlayer(songList.get(0));
    }

    private void shufflePlay() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "Nghệ sĩ này chưa có bài hát", Toast.LENGTH_SHORT).show();
            return;
        }
        Song randomSong = songList.get(random.nextInt(songList.size()));
        openMusicPlayer(randomSong);
    }

    private void openMusicPlayer(Song song) {
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        intent.putExtra("SONG_DATA", song);
        intent.putExtra("QUEUE_LIST", new ArrayList<>(songList));
        startActivity(intent);
    }

    private void toggleLikeSong(Song song, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Đăng nhập để thích bài hát", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            boolean currentlyLiked = likedSongRepo.existsByUserIdAndSongId(userId, song.getId());
            if (currentlyLiked) {
                likedSongRepo.deleteByUserIdAndSongId(userId, song.getId());
                songRepo.decrementLikeCount(song.getId());
                song.setLikeCount(Math.max(0, song.getLikeCount() - 1));
            } else {
                User user = new User();
                user.setId(userId);

                LikedSong likedSong = new LikedSong();
                likedSong.setUser(user);
                likedSong.setSong(song);
                likedSong.setLikedAt(LocalDateTime.now().toString());

                likedSongRepo.save(likedSong);
                songRepo.incrementLikeCount(song.getId());
                song.setLikeCount(song.getLikeCount() + 1);
            }

            runOnUiThread(() -> songAdapter.notifyItemChanged(position));
        }).start();
    }

    private void showSongOptions(Song song, int position) {
        String[] options = {"Thêm vào yêu thích", "Thêm vào playlist", "Xóa khỏi danh sách yêu thích"};
        new AlertDialog.Builder(this)
                .setTitle(song.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        addSongToFavorites(song, position);
                    } else {
                        if (which == 1) {
                            showAddToPlaylistDialog(song);
                        } else {
                            removeSongFromFavorites(song, position);
                        }
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void addSongToFavorites(Song song, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            boolean currentlyLiked = likedSongRepo.existsByUserIdAndSongId(userId, song.getId());
            if (currentlyLiked) {
                runOnUiThread(() -> Toast.makeText(this, "Bài hát đã có trong yêu thích", Toast.LENGTH_SHORT).show());
                return;
            }

            User user = new User();
            user.setId(userId);

            LikedSong likedSong = new LikedSong();
            likedSong.setUser(user);
            likedSong.setSong(song);
            likedSong.setLikedAt(LocalDateTime.now().toString());

            likedSongRepo.save(likedSong);
            songRepo.incrementLikeCount(song.getId());
            song.setLikeCount(song.getLikeCount() + 1);

            runOnUiThread(() -> {
                songAdapter.notifyItemChanged(position);
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void removeSongFromFavorites(Song song, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xóa yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            boolean currentlyLiked = likedSongRepo.existsByUserIdAndSongId(userId, song.getId());
            if (!currentlyLiked) {
                runOnUiThread(() -> Toast.makeText(this, "Bài hát chưa có trong yêu thích", Toast.LENGTH_SHORT).show());
                return;
            }

            likedSongRepo.deleteByUserIdAndSongId(userId, song.getId());
            songRepo.decrementLikeCount(song.getId());
            song.setLikeCount(Math.max(0, song.getLikeCount() - 1));

            runOnUiThread(() -> {
                songAdapter.notifyItemChanged(position);
                Toast.makeText(this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showAddToPlaylistDialog(Song song) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào playlist", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            List<com.ptithcm.waveapp.model.Playlist> playlists = playlistService.getMyPlaylists(userId);
            runOnUiThread(() -> {
                if (playlists.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa có playlist nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] playlistNames = new String[playlists.size()];
                for (int i = 0; i < playlists.size(); i++) {
                    playlistNames[i] = playlists.get(i).getName();
                }

                new AlertDialog.Builder(this)
                        .setTitle("Chọn playlist")
                        .setItems(playlistNames, (dialog, which) -> addSongToPlaylist(playlists.get(which).getId(), song))
                        .setNegativeButton("Đóng", null)
                        .show();
            });
        }).start();
    }

    private void addSongToPlaylist(String playlistId, Song song) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        new Thread(() -> {
            try {
                playlistService.addSong(playlistId, song.getId(), userId);
                runOnUiThread(() -> Toast.makeText(this, "Đã thêm vào playlist", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
