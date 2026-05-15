package com.ptithcm.waveapp.config;

import android.content.Context;
import com.ptithcm.waveapp.controller.*;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.repository.impl.*;
import com.ptithcm.waveapp.service.*;
import com.ptithcm.waveapp.security.JwtTokenProvider;

/**
 * Đóng vai trò như một Dependency Injection container đơn giản cho Android.
 * Giúp khởi tạo và quản lý các Singleton của Service và Controller.
 */
public class ServiceLocator {
    private static ServiceLocator instance;

    // Repositories
    private final SqlUserRepository userRepository;
    private final SqlSongRepository songRepository;
    private final SqlArtistRepository artistRepository;
    private final SqlAlbumRepository albumRepository;
    private final SqlGenreRepository genreRepository;
    private final SqlPlaylistRepository playlistRepository;
    private final SqlLikedSongRepository likedSongRepository;
    private final SqlLikedAlbumRepository likedAlbumRepository;
    private final SqlPlaylistSongRepository playlistSongRepository;
    private final SqlUserFollowArtistRepository userFollowArtistRepository;

    private final HomeService homeService; // <--- THÊM DÒNG NÀY: Khai báo homeService
    // Providers
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();

    // Services
    private final AuthService authService;
    private final CategoryService categoryService;
    private final PlaylistService playlistService;
    private final UserProfileService userProfileService;

    // Controllers
    private final AuthController authController;
    private final HomeController homeController;
    private final SearchController searchController;
    private final LibraryController libraryController;
    private final CategoryController categoryController;
    private final PlaylistController playlistController;
    private final UserProfileController userProfileController;
    private final AlbumController albumController;

    private ServiceLocator(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        userRepository = new SqlUserRepository(dbHelper);
        artistRepository = new SqlArtistRepository(dbHelper);
        genreRepository = new SqlGenreRepository(dbHelper);
        albumRepository = new SqlAlbumRepository(dbHelper, artistRepository);
        songRepository = new SqlSongRepository(dbHelper, artistRepository, albumRepository, genreRepository);
        playlistRepository = new SqlPlaylistRepository(dbHelper);
        likedSongRepository = new SqlLikedSongRepository(dbHelper);
        likedAlbumRepository = new SqlLikedAlbumRepository(dbHelper);
        playlistSongRepository = new SqlPlaylistSongRepository(dbHelper);
        userFollowArtistRepository = new SqlUserFollowArtistRepository(dbHelper);

        // Initialize Services
        authService = new AuthService(userRepository, jwtTokenProvider);
        homeService = new HomeService(albumRepository, artistRepository, genreRepository, songRepository);
        categoryService = new CategoryService(genreRepository, songRepository);
        playlistService = new PlaylistService(playlistRepository, playlistSongRepository, songRepository, userRepository, likedSongRepository);
        userProfileService = new UserProfileService(userRepository, likedSongRepository, likedAlbumRepository, userFollowArtistRepository, playlistRepository);

        // Initialize Controllers
        authController = new AuthController(authService);
        homeController = new HomeController(homeService);
        searchController = new SearchController(songRepository, artistRepository, albumRepository, genreRepository);
        libraryController = new LibraryController(likedSongRepository, likedAlbumRepository, userFollowArtistRepository, playlistRepository, songRepository, albumRepository, artistRepository, userRepository);
        categoryController = new CategoryController(categoryService);
        playlistController = new PlaylistController(playlistService);
        userProfileController = new UserProfileController(userProfileService);
        albumController = new AlbumController(albumRepository, songRepository, likedAlbumRepository, likedSongRepository);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new ServiceLocator(context);
        }
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServiceLocator must be initialized with context first");
        }
        return instance;
    }

    public AuthController getAuthController() { return authController; }
    public HomeController getHomeController() { return homeController; }
    public SearchController getSearchController() { return searchController; }
    public LibraryController getLibraryController() { return libraryController; }
    public CategoryController getCategoryController() { return categoryController; }
    public PlaylistController getPlaylistController() { return playlistController; }
    public UserProfileController getUserProfileController() { return userProfileController; }
    public AlbumController getAlbumController() { return albumController; }
}
