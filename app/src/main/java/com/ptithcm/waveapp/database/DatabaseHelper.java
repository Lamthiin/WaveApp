package com.ptithcm.waveapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "wave_app.db";
    private static final int    DATABASE_VERSION = 2;

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
    public static final String COL_USER_AVATAR     = "avatar";
    public static final String COL_USER_ROLE       = "role";
    public static final String COL_USER_VERIFIED   = "verified";
    public static final String COL_USER_CREATED_AT = "created_at";

    // artists
    public static final String COL_ARTIST_ID        = "id";
    public static final String COL_ARTIST_NAME      = "name";
    public static final String COL_ARTIST_IMAGE     = "image";
    public static final String COL_ARTIST_BIO       = "bio";
    public static final String COL_ARTIST_FOLLOWERS = "followers_count";

    // genres
    public static final String COL_GENRE_ID          = "id";
    public static final String COL_GENRE_NAME        = "name";
    public static final String COL_GENRE_DESCRIPTION = "description";
    public static final String COL_GENRE_IMAGE_URL   = "image_url";

    // albums
    public static final String COL_ALBUM_ID           = "id";
    public static final String COL_ALBUM_NAME         = "name";
    public static final String COL_ALBUM_ARTIST_ID    = "artist_id";
    public static final String COL_ALBUM_IMAGE        = "image";
    public static final String COL_ALBUM_RELEASE_DATE = "release_date";
    public static final String COL_ALBUM_PLAY_COUNT   = "play_count";

    // songs
    public static final String COL_SONG_ID         = "id";
    public static final String COL_SONG_NAME       = "name";
    public static final String COL_SONG_ARTIST_ID  = "artist_id";
    public static final String COL_SONG_ALBUM_ID   = "album_id";
    public static final String COL_SONG_GENRE_ID   = "genre_id";
    public static final String COL_SONG_DURATION   = "duration";
    public static final String COL_SONG_URL        = "url";
    public static final String COL_SONG_IMAGE      = "image";
    public static final String COL_SONG_LYRICS     = "lyrics";
    public static final String COL_SONG_PLAY_COUNT = "play_count";
    public static final String COL_SONG_LIKE_COUNT = "like_count";

    // playlists
    public static final String COL_PLAYLIST_ID         = "id";
    public static final String COL_PLAYLIST_USER_ID    = "user_id";
    public static final String COL_PLAYLIST_NAME       = "name";
    public static final String COL_PLAYLIST_IMAGE      = "image";
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
                "FOREIGN KEY(" + COL_ALBUM_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + COL_ARTIST_ID + ") ON DELETE CASCADE)");

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
                "FOREIGN KEY(" + COL_SONG_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + COL_ARTIST_ID + ") ON DELETE CASCADE," +
                "FOREIGN KEY(" + COL_SONG_ALBUM_ID + ")  REFERENCES " + TABLE_ALBUMS  + "(" + COL_ALBUM_ID  + ") ON DELETE CASCADE," +
                "FOREIGN KEY(" + COL_SONG_GENRE_ID + ")  REFERENCES " + TABLE_GENRES  + "(" + COL_GENRE_ID  + ") ON DELETE SET NULL)");

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
                "FOREIGN KEY(" + COL_FA_USER_ID   + ") REFERENCES " + TABLE_USERS   + "(" + COL_USER_ID   + ") ON DELETE CASCADE," +
                "FOREIGN KEY(" + COL_FA_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + COL_ARTIST_ID + ") ON DELETE CASCADE)");
    }

    // ═════════════════════════════════════════════════════
    // 🔥 ĐÃ FIX CỨNG: Bỏ thẳng các link URL từ Firebase Storage vào Code
    // ═════════════════════════════════════════════════════
    // ═════════════════════════════════════════════════════
    // 🔥 ĐÃ NÂNG CẤP HOÀN CHỈNH: 5 Thể loại, 10 Nghệ sĩ, 10 Album, 20 Bài hát
    // Tất cả các ID được thiết kế khớp chuẩn logic khóa ngoại (Foreign Key)
    // ═════════════════════════════════════════════════════
    private void insertSampleData(SQLiteDatabase db) {

        // 1. TABLE USERS (4 Users mẫu)
        db.execSQL("INSERT INTO " + TABLE_USERS + " VALUES" +
                "('u001','admin','admin@wave.com','123456','Admin Wave',NULL,'ADMIN',1,'2026-01-01')," +
                "('u002','sonnguyen','son@gmail.com','123456','Nguyen Van Son',NULL,'USER',1,'2026-01-02')," +
                "('u003','minhtu','tu@gmail.com','123456','Tran Minh Tu',NULL,'USER',1,'2026-01-03')," +
                "('u004','lanhanh','hanh@gmail.com','123456','Le Lan Anh',NULL,'USER',1,'2026-01-04')");

        // 2. TABLE GENRES (Đủ 5 Thể loại - match link genres_cover của bạn)
        db.execSQL("INSERT INTO " + TABLE_GENRES + " VALUES" +
                "('ballad','Ballad','Nhạc trữ tình tình cảm','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_cover%2Fdalab.jpg?alt=media&token=d40ba118-fb1f-4c45-b76a-df2058916abb')," +
                "('edm','EDM','Electronic Dance Music sôi động','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_cover%2Fedm.jpg?alt=media&token=adf985c8-4be0-4036-a8ce-7f805826c0da')," +
                "('vpop','V-Pop','Nhạc Pop Việt Nam thịnh hành','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_cover%2Fdalab.jpg?alt=media&token=d40ba118-fb1f-4c45-b76a-df2058916abb')," +
                "('indie','Indie','Nhạc độc lập mộc mạc','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_cover%2Fdalab.jpg?alt=media&token=d40ba118-fb1f-4c45-b76a-df2058916abb')," +
                "('rap','Rap/HipHop','Nhạc Rap underground và mainstream','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_cover%2Fedm.jpg?alt=media&token=adf985c8-4be0-4036-a8ce-7f805826c0da')");

        // 3. TABLE ARTISTS (Đủ 10 Nghệ sĩ - match link artist_avatars)
        db.execSQL("INSERT INTO " + TABLE_ARTISTS + " VALUES" +
                "('a001','Taylor Swift','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FTaylor.jpg?alt=media&token=9be31923-03e0-4178-9021-3f9abe000a61','Ca sĩ nhạc Pop hàng đầu thế giới',152000000)," +
                "('a002','Hoàng Dũng','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FHo%C3%A0ng%20D%C5%A9ng.jpg?alt=media&token=a0dbd6da-562b-4aee-b1ed-5264e7aa81ba','Hoàng tử tình ca Ballad Việt Nam',820000)," +
                "('a003','Bùi Trường Linh','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FB%C3%B9i%20Tr%C6%B0%E1%BB%9Dng%20Linh.jpg?alt=media&token=5d03809b-5176-47ca-8fd1-80b48ca8ae91','Ca sĩ kiêm nhạc sĩ tạo hit tài năng',910000)," +
                "('a004','Vũ.','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FHo%C3%A0ng%20D%C5%A9ng.jpg?alt=media&token=a0dbd6da-562b-4aee-b1ed-5264e7aa81ba','Hoàng tử Indie Việt Nam',1500000)," +
                "('a005','Sơn Tùng M-TP','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FTaylor.jpg?alt=media&token=9be31923-03e0-4178-9021-3f9abe000a61','Ngôi sao nhạc Pop hàng đầu Việt Nam',50000000)," +
                "('a006','HIEUTHUHAI','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FHo%C3%A0ng%20D%C5%A9ng.jpg?alt=media&token=a0dbd6da-562b-4aee-b1ed-5264e7aa81ba','Rapper điển trai, quốc dân',3200000)," +
                "('a007','Đen Vâu','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FB%C3%B9i%20Tr%C6%B0%E1%BB%9Dng%20Linh.jpg?alt=media&token=5d03809b-5176-47ca-8fd1-80b48ca8ae91','Rapper của những bài ca triết lý',4500000)," +
                "('a008','tlinh','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FTaylor.jpg?alt=media&token=9be31923-03e0-4178-9021-3f9abe000a61','Nữ nghệ sĩ GenZ đầy cá tính',1800000)," +
                "('a009','Alan Walker','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FTaylor.jpg?alt=media&token=9be31923-03e0-4178-9021-3f9abe000a61','DJ/Producer nhạc điện tử thế giới',42000000)," +
                "('a010','Grey D','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FHo%C3%A0ng%20D%C5%A9ng.jpg?alt=media&token=a0dbd6da-562b-4aee-b1ed-5264e7aa81ba','Hoàng tử dòng nhạc mộng mơ',1100000)");

        // 4. TABLE ALBUMS (Đủ 10 Album - match link album_covers)
        db.execSQL("INSERT INTO " + TABLE_ALBUMS + " VALUES" +
                "('al001','1889','a001','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115','2014-10-27',89000000)," +
                "('al002','25','a002','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61','2020-12-15',1500000)," +
                "('al003','Từng Ngày Như Mãi Mãi','a003','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122','2023-03-15',15000000)," +
                "('al004','Xoay Tròn','a002','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61','2024-05-15',500000)," +
                "('al005','Bảo Tàng Của Nuối Tiếc','a004','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122','2024-09-25',2300000)," +
                "('al006','Chúng Ta Của Tương Lai','a005','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115','2024-03-08',45000000)," +
                "('al007','Ai Cũng Phải Bắt Đầu Từ Đâu Đó','a006','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61','2023-10-25',12000000)," +
                "('al008','Dong Khung Chanh Quas','a007','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122','2024-04-10',35000000)," +
                "('al009','ái','a008','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115','2023-08-15',18000000)," +
                "('al010','Different World','a009','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61','2018-12-14',500000000)");

        // 5. TABLE SONGS (Chuẩn cú pháp 20 bài hát liên mạch, dán sẵn các link trực tuyến thật của bạn)
        db.execSQL("INSERT INTO " + TABLE_SONGS + " VALUES" +
                // --- Nhóm Bùi Trường Linh ---
                "('song_001','Anh Chưa Từng Hết Yêu','a003','al003','ballad',274," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FAnh%20Ch%C6%B0a%20T%E1%BB%ABng%20H%E1%BA%BFt%20Y%C3%AAu%20_%20buitruonglinh%20%5B-dsIiSaPZZA%5D.mp3?alt=media&token=4931d2fb-d980-406e-8d87-66151f4d8f77'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122'," +
                "'Anh chưa từng hết yêu em...',0,0)," +

                "('song_002','Đường Tôi Chở Em Về','a003','al003','ballad',250," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FAnh%20Ch%C6%B0a%20T%E1%BB%ABng%20H%E1%BA%BFt%20Y%C3%AAu%20_%20buitruonglinh%20%5B-dsIiSaPZZA%5D.mp3?alt=media&token=4931d2fb-d980-406e-8d87-66151f4d8f77'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122'," +
                "'Xe đạp lướt nhanh trên đường...',4200,120)," +

                // --- Nhóm Taylor Swift ---
                "('song_003','Enchanted','a001','al001','edm',231," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FTaylor%20Swift%20-%20Enchanted.mp4?alt=media&token=00e4babd-c5cf-4837-ba98-5d28612ae036'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115'," +
                "'Please dont be in love with someone else...',1200,450)," +

                "('song_004','Blank Space','a001','al001','vpop',231," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FTaylor%20Swift%20-%20Enchanted.mp4?alt=media&token=00e4babd-c5cf-4837-ba98-5d28612ae036'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115'," +
                "'Got a long list of ex-lovers...',25000,9900)," +

                // --- Nhóm Hoàng Dũng ---
                "('song_005','Em Trồng Cây','a002','al004','ballad',252," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%9CONG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=a2cfebda-52b5-4573-9746-7260b786e940'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61'," +
                "'Em trồng cây, em vun bón...',3500,980)," +

                "('song_006','Giữ Anh Cho Ngày Hôm Qua','a002','al004','ballad',245," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%9CONG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=a2cfebda-52b5-4573-9746-7260b786e940'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61'," +
                "'Hãy giữ anh cho ngày hôm qua...',4100,210)," +

                "('song_007','Sâm Phanh','a002','al002','ballad',252," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%9CONG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=a2cfebda-52b5-4573-9746-7260b786e940'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61'," +
                "'Em, không là nàng thơ...',95000,4300)," +

                // --- Nhóm Vũ. ---
                "('song_008','Không Yêu Em Thì Yêu AI','a004','al005','indie',260," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%9CONG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=a2cfebda-52b5-4573-9746-7260b786e940'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122'," +
                "'Kìa ngoài song cửa...',84000,3200)," +

                "('song_009','Bình Yên','a004','al005','indie',255," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%9CONG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=a2cfebda-52b5-4573-9746-7260b786e940'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122'," +
                "'Cuộc đời cuốn xô ta bước qua nhau...',73000,2900)," +

                // --- Nhóm Sơn Tùng M-TP ---
                "('song_010','Chúng Ta Của Tương Lai','a005','al006','vpop',247," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FAnh%20Ch%C6%B0a%20T%E1%BB%ABng%20H%E1%BA%BFt%20Y%C3%AAu%20_%20buitruonglinh%20%5B-dsIiSaPZZA%5D.mp3?alt=media&token=4931d2fb-d980-406e-8d87-66151f4d8f77'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115'," +
                "'Liệu mai sau hai ta có gặp lại...',120000,5400)," +

                "('song_011','Hãy Trao Cho Anh','a005','al006','vpop',246," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FAnh%20Ch%C6%B0a%20T%E1%BB%ABng%20H%E1%BA%BFt%20Y%C3%AAu%20_%20buitruonglinh%20%5B-dsIiSaPZZA%5D.mp3?alt=media&token=4931d2fb-d980-406e-8d87-66151f4d8f77'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115'," +
                "'Hãy trao cho anh thứ anh đang mong chờ...',340000,12000)," +

                // --- Nhóm HIEUTHUHAI ---
                "('song_012','Không Thể Say','a006','al007','rap',198," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%9CONG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=a2cfebda-52b5-4573-9746-7260b786e940'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61'," +
                "'Rót thêm một ly nữa đi...',89000,4500)," +

                "('song_013','Ngủ Một Mình','a006','al007','rap',210," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%9CONG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=a2cfebda-52b5-4573-9746-7260b786e940'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61'," +
                "'Anh không muốn ngủ một mình...',92000,6100)," +

                // --- Nhóm Đen Vâu ---
                "('song_014','Trốn Tìm','a007','al008','rap',245," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FAnh%20Ch%C6%B0a%20T%E1%BB%ABng%20H%E1%BA%BFt%20Y%C3%AAu%20_%20buitruonglinh%20%5B-dsIiSaPZZA%5D.mp3?alt=media&token=4931d2fb-d980-406e-8d87-66151f4d8f77'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122'," +
                "'Anh đi tìm em như thể chơi trốn tìm...',150000,7500)," +

                "('song_015','Mang Tiền Về Cho Mẹ','a007','al008','rap',280," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FAnh%20Ch%C6%B0a%20T%E1%BB%ABng%20H%E1%BA%BFt%20Y%C3%AAu%20_%20buitruonglinh%20%5B-dsIiSaPZZA%5D.mp3?alt=media&token=4931d2fb-d980-406e-8d87-66151f4d8f77'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=2f5ec698-a4db-4103-b251-aab79c205122'," +
                "'Mang tiền về cho mẹ đừng mang ưu phiền...',180000,8200)," +

                // --- Nhóm tlinh ---
                "('song_016','Nếu Lúc Đó','a008','al009','rap',215," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FTaylor%20Swift%20-%20Enchanted.mp4?alt=media&token=00e4babd-c5cf-4837-ba98-5d28612ae036'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115'," +
                "'Nếu lúc đó mình đừng buông tay...',85000,4100)," +

                "('song_017','Gái Độc Thân','a008','al009','vpop',190," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FTaylor%20Swift%20-%20Enchanted.mp4?alt=media&token=00e4babd-c5cf-4837-ba98-5d28612ae036'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=efd06acb-25e6-4c99-a388-3df6b5caf115'," +
                "'Em là gái độc thân tự tin...',64000,2100)," +

                // --- Nhóm Alan Walker ---
                "('song_018','Faded','a009','al010','edm',212," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FTaylor%20Swift%20-%20Enchanted.mp4?alt=media&token=00e4babd-c5cf-4837-ba98-5d28612ae036'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61'," +
                "'Where are you now? Another dream...',990000,43000)," +

                "('song_019','Alone','a009','al010','edm',163," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FTaylor%20Swift%20-%20Enchanted.mp4?alt=media&token=00e4babd-c5cf-4837-ba98-5d28612ae036'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61'," +
                "'I know I am not alone...',650000,31000)," +

                // --- Nhóm Grey D ---
                "('song_020','Có Hẹn Với Thanh Xuân','a010',NULL,'vpop',222," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%9CONG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=a2cfebda-52b5-4573-9746-7260b786e940'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61'," +
                "'Cảm ơn vì đã cùng anh đi qua bão giông...',98000,4200)"); // <--- Kết thúc bài 20 bằng dấu đóng ngoặc ");"

        // 6. CÁC BẢNG LIÊN KẾT ĐÃ ĐƯỢC CHUẨN HÓA THEO MÃ BÀI HÁT MỚI (Từ song_001 đến song_020)
        db.execSQL("INSERT INTO " + TABLE_PLAYLISTS + " VALUES" +
                "('pl001','u002','Nhạc Tuyển Chọn 1',NULL,'2026-02-01')," +
                "('pl002','u002','Top Hits Việt Nam',NULL,'2026-02-10')");

        db.execSQL("INSERT INTO " + TABLE_PLAYLIST_SONGS + " VALUES" +
                "('pl001','song_001',0,'2026-02-01')," +
                "('pl001','song_005',1,'2026-02-01')," +
                "('pl001','song_008',2,'2026-02-02')," +
                "('pl002','song_010',0,'2026-02-10')," +
                "('pl002','song_012',1,'2026-02-10')," +
                "('pl002','song_020',2,'2026-02-11')");

        db.execSQL("INSERT INTO " + TABLE_LIKED_SONGS + " VALUES" +
                "('u002','song_001','2026-02-01')," +
                "('u002','song_005','2026-02-02')," +
                "('u002','song_010','2026-02-05')," +
                "('u003','song_003','2026-03-01')," +
                "('u004','song_014','2026-03-15')");

        db.execSQL("INSERT INTO " + TABLE_LIKED_ALBUMS + "(user_id,album_id,added_at) VALUES" +
                "('u002','al003','2026-02-01')," +
                "('u002','al004','2026-02-10')," +
                "('u003','al005','2026-03-01')");

        db.execSQL("INSERT INTO " + TABLE_FOLLOW_ARTISTS + " VALUES" +
                "('u002','a001','2026-02-01')," +
                "('u002','a002','2026-02-05')," +
                "('u003','a003','2026-03-01')," +
                "('u004','a004','2026-03-15')");
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
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON");
            // Tự động xóa nghệ sĩ không có ảnh mỗi khi mở DB
            db.execSQL("DELETE FROM " + TABLE_ARTISTS + " WHERE " + COL_ARTIST_IMAGE + " IS NULL OR " + COL_ARTIST_IMAGE + " = ''");
        }
    }

    public void deleteArtistsWithoutImage() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ARTISTS, COL_ARTIST_IMAGE + " IS NULL OR " + COL_ARTIST_IMAGE + " = ''", null);
    }

    public void updateGenreImageUrl(String id, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GENRE_IMAGE_URL, url);
        db.update(TABLE_GENRES, values, COL_GENRE_ID + "=?", new String[]{id});
    }

    public void updateArtistImage(String id, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ARTIST_IMAGE, image);
        db.update(TABLE_ARTISTS, values, COL_ARTIST_ID + "=?", new String[]{id});
    }

    public void updateAlbumImage(String id, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ALBUM_IMAGE, image);
        db.update(TABLE_ALBUMS, values, COL_ALBUM_ID + "=?", new String[]{id});
    }

    public void updateSongUrls(String id, String songUrl, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SONG_URL, songUrl);
        values.put(COL_SONG_IMAGE, imageUrl);
        db.update(TABLE_SONGS, values, COL_SONG_ID + "=?", new String[]{id});
    }

    public static String getFirebaseStorageUrl(String url) {
        return url;
    }
}