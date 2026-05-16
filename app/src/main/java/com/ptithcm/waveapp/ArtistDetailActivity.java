package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ptithcm.waveapp.util.TokenManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArtistDetailActivity extends AppCompatActivity {

    private ImageView imgArtist;
    private TextView tvArtistName, tvMonthlyListeners;
    private AppCompatButton btnFollow;
    private ImageButton btnBack, btnShuffle, btnPlay;
    private RecyclerView rvPopularSongs;

    private SongAdapter songAdapter;
    private Artist currentArtist;
    private List<Song> songList = new ArrayList<>();

    private SongRepository songRepo;
    private ArtistRepository artistRepo;
    private UserFollowArtistRepository followRepo;
    private LikedSongRepository likedSongRepo;
    private TokenManager tokenManager;

    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        initRepositories();
        initViews();
        handleIntent();
        setupRecyclerView();
        setupListeners();
        loadArtistData();
    }

    private void initRepositories() {
        ServiceLocator locator = ServiceLocator.getInstance();
        songRepo = locator.songRepository;
        artistRepo = locator.artistRepository;
        followRepo = locator.userFollowArtistRepository;
        likedSongRepo = locator.likedSongRepository;
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

    private void handleIntent() {
        currentArtist = (Artist) getIntent().getSerializableExtra("ARTIST_DATA");
        if (currentArtist == null) {
            finish();
        }
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter();
        rvPopularSongs.setLayoutManager(new LinearLayoutManager(this));
        rvPopularSongs.setAdapter(songAdapter);

        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });

        songAdapter.setOnLikeClickListener(this::toggleLikeSong);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFollow.setOnClickListener(v -> toggleFollow());

        btnPlay.setOnClickListener(v -> {
            if (!songList.isEmpty()) {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                intent.putExtra("SONG_DATA", songList.get(0));
                startActivity(intent);
            }
        });

        btnShuffle.setOnClickListener(v -> {
            if (!songList.isEmpty()) {
                // In a real app, you'd shuffle the list and play the first one
                Toast.makeText(this, "Shuffle play", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadArtistData() {
        if (currentArtist == null) return;

        tvArtistName.setText(currentArtist.getName());
        tvMonthlyListeners.setText(getString(R.string.followers_count, currentArtist.getFollowersCount()));

        Glide.with(this)
                .load(currentArtist.getImage())
                .placeholder(R.drawable.sontung)
                .into(imgArtist);

        checkFollowStatus();
        loadPopularSongs();
    }

    private void checkFollowStatus() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        isFollowing = followRepo.existsByUserIdAndArtistId(userId, currentArtist.getId());
        updateFollowButton();
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
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login to follow", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFollowing) {
            followRepo.deleteByUserIdAndArtistId(userId, currentArtist.getId());
            artistRepo.decrementFollowers(currentArtist.getId());
            isFollowing = false;
        } else {
            String now = LocalDateTime.now().toString();
            UserFollowArtist follow = UserFollowArtist.builder()
                    .user(User.builder().id(userId).build())
                    .artist(currentArtist)
                    .followedAt(now)
                    .build();
            followRepo.save(follow);
            artistRepo.incrementFollowers(currentArtist.getId());
            isFollowing = true;
        }
        updateFollowButton();
        
        // Refresh follower count
        artistRepo.findById(currentArtist.getId()).ifPresent(updated -> {
            currentArtist = updated;
            tvMonthlyListeners.setText(getString(R.string.followers_count, currentArtist.getFollowersCount()));
        });
    }

    private void loadPopularSongs() {
        songList = songRepo.findByArtistIdAndActiveTrue(currentArtist.getId());
        songAdapter.setSongs(songList);
    }

    private void toggleLikeSong(Song song, int position) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        boolean currentlyLiked = likedSongRepo.existsByUserIdAndSongId(userId, song.getId());
        if (currentlyLiked) {
            likedSongRepo.deleteByUserIdAndSongId(userId, song.getId());
            songRepo.decrementLikeCount(song.getId());
        } else {
            String now = LocalDateTime.now().toString();
            LikedSong likedSong = LikedSong.builder()
                    .user(User.builder().id(userId).build())
                    .song(song)
                    .likedAt(now)
                    .build();
            likedSongRepo.save(likedSong);
            songRepo.incrementLikeCount(song.getId());
        }
        songAdapter.notifyItemChanged(position);
    }
}
