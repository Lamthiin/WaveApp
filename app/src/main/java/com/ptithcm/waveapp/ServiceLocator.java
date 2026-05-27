package com.ptithcm.waveapp;

import android.content.Context;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.repository.AlbumRepository;
import com.ptithcm.waveapp.repository.ArtistRepository;
import com.ptithcm.waveapp.repository.GenreRepository;
import com.ptithcm.waveapp.repository.LikedAlbumRepository;
import com.ptithcm.waveapp.repository.LikedSongRepository;
import com.ptithcm.waveapp.repository.PlaylistRepository;
import com.ptithcm.waveapp.repository.PlaylistSongRepository;
import com.ptithcm.waveapp.repository.SongRepository;
import com.ptithcm.waveapp.repository.UserFollowArtistRepository;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.service.AuthService;
import com.ptithcm.waveapp.service.CategoryService;
import com.ptithcm.waveapp.service.HomeService;
import com.ptithcm.waveapp.service.PlaylistService;
import com.ptithcm.waveapp.service.UserProfileService;
import com.ptithcm.waveapp.util.TokenManager;

/**
 * Dependency Injection container đơn giản.
 * Khởi tạo trong WaveApplication.onCreate() bằng ServiceLocator.init(this)
 */
public class ServiceLocator {
    private static ServiceLocator instance;

    // Repositories
    public final UserRepository             userRepository;
    public final SongRepository             songRepository;
    public final ArtistRepository           artistRepository;
    public final AlbumRepository            albumRepository;
    public final GenreRepository            genreRepository;
    public final PlaylistRepository         playlistRepository;
    public final LikedSongRepository        likedSongRepository;
    public final LikedAlbumRepository       likedAlbumRepository;
    public final PlaylistSongRepository     playlistSongRepository;
    public final UserFollowArtistRepository userFollowArtistRepository;

    // Services
    private final AuthService        authService;
    private final HomeService        homeService;
    private final CategoryService    categoryService;
    private final PlaylistService    playlistService;
    private final UserProfileService userProfileService;

    private ServiceLocator(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        userRepository             = new UserRepository(dbHelper);
        songRepository             = new SongRepository(dbHelper);
        artistRepository           = new ArtistRepository(dbHelper);
        albumRepository            = new AlbumRepository(dbHelper);
        genreRepository            = new GenreRepository(dbHelper);
        playlistRepository         = new PlaylistRepository(dbHelper);
        likedSongRepository        = new LikedSongRepository(dbHelper);
        likedAlbumRepository       = new LikedAlbumRepository(dbHelper);
        playlistSongRepository     = new PlaylistSongRepository(dbHelper);
        userFollowArtistRepository = new UserFollowArtistRepository(dbHelper);

        TokenManager tokenManager  = new TokenManager(context);
        authService        = new AuthService(userRepository, tokenManager);
        homeService        = new HomeService(albumRepository, artistRepository, genreRepository, songRepository);
        categoryService    = new CategoryService(genreRepository, songRepository);
        playlistService    = new PlaylistService(playlistRepository, playlistSongRepository, songRepository, userRepository, likedSongRepository);
        userProfileService = new UserProfileService(userRepository, likedSongRepository, likedAlbumRepository, userFollowArtistRepository, playlistRepository, songRepository, albumRepository);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new ServiceLocator(context);
        }
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServiceLocator chưa được khởi tạo. Gọi ServiceLocator.init(this) trong WaveApplication.onCreate()");
        }
        return instance;
    }

    // Getters
    public AuthService        getAuthService()        { return authService; }
    public HomeService        getHomeService()        { return homeService; }
    public CategoryService    getCategoryService()    { return categoryService; }
    public PlaylistService    getPlaylistService()    { return playlistService; }
    public UserProfileService getUserProfileService() { return userProfileService; }
}