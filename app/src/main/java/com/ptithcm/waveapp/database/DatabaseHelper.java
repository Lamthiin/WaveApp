package com.ptithcm.waveapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "wave_app.db";
    private static final int    DATABASE_VERSION = 1;

    private static DatabaseHelper instance;
    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    public static final String TABLE_USERS          = "users";
    public static final String TABLE_SONGS          = "songs";
    public static final String TABLE_ARTISTS        = "artists";
    public static final String TABLE_ALBUMS         = "albums";
    public static final String TABLE_GENRES         = "genres";
    public static final String TABLE_PLAYLISTS      = "playlists";
    public static final String TABLE_PLAYLIST_SONGS = "playlist_songs";
    public static final String TABLE_LIKED_SONGS    = "liked_songs";
    public static final String TABLE_LIKED_ALBUMS   = "liked_albums";
    public static final String TABLE_FOLLOW_ARTISTS = "user_follow_artists";

    // users
    public static final String COL_USER_ID         = "id";
    public static final String COL_USER_USERNAME   = "username";
    public static final String COL_USER_EMAIL      = "email";
    public static final String COL_USER_PASSWORD   = "password";
    public static final String COL_USER_NAME       = "name";
    public static final String COL_USER_AVATAR     = "avatar";      // TEXT: path file
    public static final String COL_USER_ROLE       = "role";
    public static final String COL_USER_VERIFIED   = "verified";
    public static final String COL_USER_CREATED_AT = "created_at";

    // artists
    public static final String COL_ARTIST_ID        = "id";
    public static final String COL_ARTIST_NAME      = "name";
    public static final String COL_ARTIST_IMAGE     = "image";      // TEXT: path file
    public static final String COL_ARTIST_BIO       = "bio";
    public static final String COL_ARTIST_FOLLOWERS = "followers_count";

    // genres
    public static final String COL_GENRE_ID          = "id";
    public static final String COL_GENRE_NAME        = "name";
    public static final String COL_GENRE_DESCRIPTION = "description";
    public static final String COL_GENRE_IMAGE_URL   = "image_url"; // TEXT: path file

    // albums
    public static final String COL_ALBUM_ID           = "id";
    public static final String COL_ALBUM_NAME         = "name";
    public static final String COL_ALBUM_ARTIST_ID    = "artist_id";
    public static final String COL_ALBUM_IMAGE        = "image";     // TEXT: path file
    public static final String COL_ALBUM_RELEASE_DATE = "release_date";
    public static final String COL_ALBUM_PLAY_COUNT   = "play_count";

    // songs
    public static final String COL_SONG_ID         = "id";
    public static final String COL_SONG_NAME       = "name";
    public static final String COL_SONG_ARTIST_ID  = "artist_id";
    public static final String COL_SONG_ALBUM_ID   = "album_id";
    public static final String COL_SONG_GENRE_ID   = "genre_id";
    public static final String COL_SONG_DURATION   = "duration";
    public static final String COL_SONG_URL        = "url";          // TEXT: path file nhac
    public static final String COL_SONG_IMAGE      = "image";        // TEXT: path file anh
    public static final String COL_SONG_LYRICS     = "lyrics";
    public static final String COL_SONG_PLAY_COUNT = "play_count";
    public static final String COL_SONG_LIKE_COUNT = "like_count";

    // playlists
    public static final String COL_PLAYLIST_ID         = "id";
    public static final String COL_PLAYLIST_USER_ID    = "user_id";
    public static final String COL_PLAYLIST_NAME       = "name";
    public static final String COL_PLAYLIST_IMAGE      = "image";    // TEXT: path file
    public static final String COL_PLAYLIST_CREATED_AT = "created_at";

    // playlist_songs
    public static final String COL_PS_PLAYLIST_ID = "playlist_id";
    public static final String COL_PS_SONG_ID     = "song_id";
    public static final String COL_PS_POSITION    = "position";
    public static final String COL_PS_ADDED_AT    = "added_at";

    // liked_songs
    public static final String COL_LS_USER_ID  = "user_id";
    public static final String COL_LS_SONG_ID  = "song_id";
    public static final String COL_LS_LIKED_AT = "liked_at";

    // liked_albums
    public static final String COL_LA_ID       = "id";
    public static final String COL_LA_USER_ID  = "user_id";
    public static final String COL_LA_ALBUM_ID = "album_id";
    public static final String COL_LA_ADDED_AT = "added_at";

    // user_follow_artists
    public static final String COL_FA_USER_ID     = "user_id";
    public static final String COL_FA_ARTIST_ID   = "artist_id";
    public static final String COL_FA_FOLLOWED_AT = "followed_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        insertSampleData(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + "(" +
                COL_USER_ID + " TEXT PRIMARY KEY," +
                COL_USER_USERNAME + " TEXT UNIQUE NOT NULL," +
                COL_USER_EMAIL + " TEXT UNIQUE," +
                COL_USER_PASSWORD + " TEXT," +
                COL_USER_NAME + " TEXT," +
                COL_USER_AVATAR + " TEXT," +
                COL_USER_ROLE + " TEXT DEFAULT 'USER'," +
                COL_USER_VERIFIED + " INTEGER DEFAULT 0," +
                COL_USER_CREATED_AT + " TEXT DEFAULT (datetime('now')))");

        db.execSQL("CREATE TABLE " + TABLE_ARTISTS + "(" +
                COL_ARTIST_ID + " TEXT PRIMARY KEY," +
                COL_ARTIST_NAME + " TEXT NOT NULL," +
                COL_ARTIST_IMAGE + " TEXT," +
                COL_ARTIST_BIO + " TEXT," +
                COL_ARTIST_FOLLOWERS + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_GENRES + "(" +
                COL_GENRE_ID + " TEXT PRIMARY KEY," +
                COL_GENRE_NAME + " TEXT NOT NULL UNIQUE," +
                COL_GENRE_DESCRIPTION + " TEXT," +
                COL_GENRE_IMAGE_URL + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_ALBUMS + "(" +
                COL_ALBUM_ID + " TEXT PRIMARY KEY," +
                COL_ALBUM_NAME + " TEXT NOT NULL," +
                COL_ALBUM_ARTIST_ID + " TEXT," +
                COL_ALBUM_IMAGE + " TEXT," +
                COL_ALBUM_RELEASE_DATE + " TEXT," +
                COL_ALBUM_PLAY_COUNT + " INTEGER DEFAULT 0," +
                "FOREIGN KEY(" + COL_ALBUM_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + COL_ARTIST_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_SONGS + "(" +
                COL_SONG_ID + " TEXT PRIMARY KEY," +
                COL_SONG_NAME + " TEXT NOT NULL," +
                COL_SONG_ARTIST_ID + " TEXT," +
                COL_SONG_ALBUM_ID + " TEXT," +
                COL_SONG_GENRE_ID + " TEXT," +
                COL_SONG_DURATION + " INTEGER DEFAULT 0," +
                COL_SONG_URL + " TEXT," +
                COL_SONG_IMAGE + " TEXT," +
                COL_SONG_LYRICS + " TEXT," +
                COL_SONG_PLAY_COUNT + " INTEGER DEFAULT 0," +
                COL_SONG_LIKE_COUNT + " INTEGER DEFAULT 0," +
                "FOREIGN KEY(" + COL_SONG_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + COL_ARTIST_ID + ")," +
                "FOREIGN KEY(" + COL_SONG_ALBUM_ID + ")  REFERENCES " + TABLE_ALBUMS  + "(" + COL_ALBUM_ID  + ")," +
                "FOREIGN KEY(" + COL_SONG_GENRE_ID + ")  REFERENCES " + TABLE_GENRES  + "(" + COL_GENRE_ID  + "))");

        db.execSQL("CREATE TABLE " + TABLE_PLAYLISTS + "(" +
                COL_PLAYLIST_ID + " TEXT PRIMARY KEY," +
                COL_PLAYLIST_USER_ID + " TEXT NOT NULL," +
                COL_PLAYLIST_NAME + " TEXT NOT NULL," +
                COL_PLAYLIST_IMAGE + " TEXT," +
                COL_PLAYLIST_CREATED_AT + " TEXT DEFAULT (datetime('now'))," +
                "FOREIGN KEY(" + COL_PLAYLIST_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_PLAYLIST_SONGS + "(" +
                COL_PS_PLAYLIST_ID + " TEXT NOT NULL," +
                COL_PS_SONG_ID + " TEXT NOT NULL," +
                COL_PS_POSITION + " INTEGER DEFAULT 0," +
                COL_PS_ADDED_AT + " TEXT DEFAULT (datetime('now'))," +
                "PRIMARY KEY(" + COL_PS_PLAYLIST_ID + "," + COL_PS_SONG_ID + ")," +
                "FOREIGN KEY(" + COL_PS_PLAYLIST_ID + ") REFERENCES " + TABLE_PLAYLISTS + "(" + COL_PLAYLIST_ID + ")," +
                "FOREIGN KEY(" + COL_PS_SONG_ID + ")     REFERENCES " + TABLE_SONGS     + "(" + COL_SONG_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_LIKED_SONGS + "(" +
                COL_LS_USER_ID + " TEXT NOT NULL," +
                COL_LS_SONG_ID + " TEXT NOT NULL," +
                COL_LS_LIKED_AT + " TEXT DEFAULT (datetime('now'))," +
                "PRIMARY KEY(" + COL_LS_USER_ID + "," + COL_LS_SONG_ID + ")," +
                "FOREIGN KEY(" + COL_LS_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")," +
                "FOREIGN KEY(" + COL_LS_SONG_ID + ") REFERENCES " + TABLE_SONGS + "(" + COL_SONG_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_LIKED_ALBUMS + "(" +
                COL_LA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_LA_USER_ID + " TEXT NOT NULL," +
                COL_LA_ALBUM_ID + " TEXT NOT NULL," +
                COL_LA_ADDED_AT + " TEXT DEFAULT (datetime('now'))," +
                "UNIQUE(" + COL_LA_USER_ID + "," + COL_LA_ALBUM_ID + ")," +
                "FOREIGN KEY(" + COL_LA_USER_ID  + ") REFERENCES " + TABLE_USERS  + "(" + COL_USER_ID  + ")," +
                "FOREIGN KEY(" + COL_LA_ALBUM_ID + ") REFERENCES " + TABLE_ALBUMS + "(" + COL_ALBUM_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_FOLLOW_ARTISTS + "(" +
                COL_FA_USER_ID + " TEXT NOT NULL," +
                COL_FA_ARTIST_ID + " TEXT NOT NULL," +
                COL_FA_FOLLOWED_AT + " TEXT DEFAULT (datetime('now'))," +
                "PRIMARY KEY(" + COL_FA_USER_ID + "," + COL_FA_ARTIST_ID + ")," +
                "FOREIGN KEY(" + COL_FA_USER_ID   + ") REFERENCES " + TABLE_USERS   + "(" + COL_USER_ID   + ")," +
                "FOREIGN KEY(" + COL_FA_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + COL_ARTIST_ID + "))");
    }

    private void insertSampleData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_USERS + " VALUES" +
                "('u001','admin','admin@wave.com','123456','Admin Wave',NULL,'ADMIN',1,'2024-01-01')," +
                "('u002','sonnguyen','son@gmail.com','123456','Nguyen Van Son',NULL,'USER',1,'2024-01-02')," +
                "('u003','minhtu','tu@gmail.com','123456','Tran Minh Tu',NULL,'USER',1,'2024-01-03')," +
                "('u004','lanhanh','hanh@gmail.com','123456','Le Lan Anh',NULL,'USER',1,'2024-01-04')");

        db.execSQL("INSERT INTO " + TABLE_GENRES + " VALUES" +
                "('edm','EDM','Electronic Dance Music',NULL)," +
                "('kpop','K-Pop','Nhac Pop Han Quoc',NULL)," +
                "('ballad','Ballad','Nhac tru tinh',NULL)," +
                "('rock','Rock','Nhac Rock',NULL)," +
                "('vpop','V-Pop','Nhac Pop Viet Nam',NULL)," +
                "('rnb','R&B','Rhythm and Blues',NULL)");

        db.execSQL("INSERT INTO " + TABLE_ARTISTS + " VALUES" +
                "('a001','Son Tung M-TP',NULL,'Ca si, nhac si Viet Nam',5200000)," +
                "('a002','HIEUTHUHAI',NULL,'Rapper, ca si',2100000)," +
                "('a003','Bui Truong Linh',NULL,'Ca si ballad',820000)," +
                "('a004','Duong Domic',NULL,'Ca si V-Pop',610000)," +
                "('a005','Phung Khanh Linh',NULL,'Ca si kiem nhac si',940000)," +
                "('a006','Low G',NULL,'Rapper underground',430000)," +
                "('a007','Bray',NULL,'Rapper freestyle',390000)," +
                "('a008','Mono',NULL,'Ca si tre tai nang',1500000)," +
                "('a009','tlinh',NULL,'Nu rapper hang dau',870000)," +
                "('a010','Vu.',NULL,'Ca si nhac indie',1200000)");

        db.execSQL("INSERT INTO " + TABLE_ALBUMS + " VALUES" +
                "('al001','Hay Trao Cho Anh','a001',NULL,'2019-07-01',50000000)," +
                "('al002','Muon Roi Ma Sao Con','a001',NULL,'2022-01-01',30000000)," +
                "('al003','Tung Ngay Nhu Mai Mai','a003',NULL,'2023-03-15',15000000)," +
                "('al004','Du Lieu Quy','a004',NULL,'2023-06-20',10000000)," +
                "('al005','Ngu Mot Minh','a008',NULL,'2022-09-09',18000000)," +
                "('al006','Khong The Say','a002',NULL,'2023-11-01',22000000)," +
                "('al007','Chim Sau','a010',NULL,'2023-05-01',12000000)," +
                "('al008','Tron Tim','a008',NULL,'2023-01-01',25000000)");

        db.execSQL("INSERT INTO " + TABLE_SONGS + " VALUES" +
                "('s001','Hay Trao Cho Anh','a001','al001','vpop',246,'songs/s001.mp3',NULL,'Hay trao cho anh...',55000000,2100000)," +
                "('s002','Muon Roi Ma Sao Con','a001','al002','ballad',247,'songs/s002.mp3',NULL,'Muon roi...',40000000,1800000)," +
                "('s003','Chung Ta Cua Hien Tai','a001','al002','ballad',265,'songs/s003.mp3',NULL,'Chung ta...',38000000,1700000)," +
                "('s004','Noi Nay Co Anh','a001','al001','vpop',238,'songs/s004.mp3',NULL,'Noi nay co anh...',45000000,1900000)," +
                "('s005','Lac Troi','a001','al001','vpop',252,'songs/s005.mp3',NULL,'Anh lac troi...',42000000,1600000)," +
                "('s006','Khong The Say','a002','al006','vpop',198,'songs/s006.mp3',NULL,'Khong the say...',22000000,1200000)," +
                "('s007','Ngu Mot Minh','a008','al005','ballad',210,'songs/s007.mp3',NULL,'Ngu mot minh...',20000000,1100000)," +
                "('s008','Tron Tim','a008','al008','vpop',215,'songs/s008.mp3',NULL,'Tron tim...',25000000,1300000)," +
                "('s009','Tung Ngay Nhu Mai Mai','a003','al003','ballad',274,'songs/s009.mp3',NULL,'Tung ngay...',15000000,800000)," +
                "('s010','Toi Thay Hoa Vang','a003','al003','ballad',260,'songs/s010.mp3',NULL,'Toi thay hoa vang...',12000000,700000)," +
                "('s011','Du Lieu Quy','a004','al004','vpop',215,'songs/s011.mp3',NULL,'Du lieu quy...',10000000,500000)," +
                "('s012','Yeu Thi Yeu Thoi','a004','al004','vpop',222,'songs/s012.mp3',NULL,'Yeu thi yeu thoi...',8000000,400000)," +
                "('s013','Buoc Qua Mua Co Don','a005',NULL,'ballad',255,'songs/s013.mp3',NULL,'Buoc qua mua co don...',9000000,450000)," +
                "('s014','Em Xinh','a005',NULL,'vpop',195,'songs/s014.mp3',NULL,'Em xinh...',7000000,350000)," +
                "('s015','Ong Ba Anh','a006',NULL,'vpop',230,'songs/s015.mp3',NULL,'Ong ba anh...',30000000,1400000)," +
                "('s016','Bac Kim Thang','a007',NULL,'vpop',185,'songs/s016.mp3',NULL,'Bac kim thang...',18000000,900000)," +
                "('s017','Chim Sau','a010','al007','rnb',263,'songs/s017.mp3',NULL,'Chim sau...',13000000,650000)," +
                "('s018','May Cua Ngay Da Qua','a010','al007','ballad',248,'songs/s018.mp3',NULL,'May cua ngay...',11000000,580000)," +
                "('s019','See Tinh','a009',NULL,'kpop',176,'songs/s019.mp3',NULL,'See tinh...',35000000,1600000)," +
                "('s020','Lam Gi Thi Lam','a009',NULL,'rnb',201,'songs/s020.mp3',NULL,'Lam gi thi lam...',16000000,780000)");

        db.execSQL("INSERT INTO " + TABLE_PLAYLISTS + " VALUES" +
                "('pl001','u002','Nhac Tam Trang',NULL,'2024-02-01')," +
                "('pl002','u002','Top Hits 2024',NULL,'2024-02-10')," +
                "('pl003','u003','Ballad Dem Khuya',NULL,'2024-03-01')," +
                "('pl004','u004','Rap Viet Dinh',NULL,'2024-03-15')");

        db.execSQL("INSERT INTO " + TABLE_PLAYLIST_SONGS + " VALUES" +
                "('pl001','s002',0,'2024-02-01'),('pl001','s003',1,'2024-02-01')," +
                "('pl001','s009',2,'2024-02-02'),('pl001','s010',3,'2024-02-02')," +
                "('pl002','s001',0,'2024-02-10'),('pl002','s006',1,'2024-02-10')," +
                "('pl002','s008',2,'2024-02-11'),('pl002','s019',3,'2024-02-11')," +
                "('pl003','s002',0,'2024-03-01'),('pl003','s007',1,'2024-03-01')," +
                "('pl003','s009',2,'2024-03-02'),('pl003','s013',3,'2024-03-02')," +
                "('pl004','s006',0,'2024-03-15'),('pl004','s015',1,'2024-03-15')," +
                "('pl004','s016',2,'2024-03-16'),('pl004','s020',3,'2024-03-16')");

        db.execSQL("INSERT INTO " + TABLE_LIKED_SONGS + " VALUES" +
                "('u002','s001','2024-02-01'),('u002','s004','2024-02-02')," +
                "('u002','s006','2024-02-05'),('u003','s002','2024-03-01')," +
                "('u003','s007','2024-03-02'),('u004','s006','2024-03-15')," +
                "('u004','s008','2024-03-16'),('u004','s016','2024-03-17')");

        db.execSQL("INSERT INTO " + TABLE_LIKED_ALBUMS + "(user_id,album_id,added_at) VALUES" +
                "('u002','al001','2024-02-01'),('u002','al005','2024-02-10')," +
                "('u003','al003','2024-03-01'),('u004','al006','2024-03-15')");

        db.execSQL("INSERT INTO " + TABLE_FOLLOW_ARTISTS + " VALUES" +
                "('u002','a001','2024-02-01'),('u002','a002','2024-02-05')," +
                "('u003','a003','2024-03-01'),('u003','a005','2024-03-03')," +
                "('u004','a002','2024-03-15'),('u004','a006','2024-03-16')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLLOW_ARTISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKED_ALBUMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKED_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GENRES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) db.execSQL("PRAGMA foreign_keys = ON");
    }
}