package com.ptithcm.waveapp.database;

import android.content.ContentValues;
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
    public static final String COL_USER_AVATAR     = "avatar";
    public static final String COL_USER_ROLE       = "role";
    public static final String COL_USER_CREATED_AT = "created_at";
    public static final String COL_USER_UPDATED_AT = "updated_at";

    // artists
    public static final String COL_ARTIST_ID        = "id";
    public static final String COL_ARTIST_NAME      = "name";
    public static final String COL_ARTIST_IMAGE     = "image";
    public static final String COL_ARTIST_BIO       = "bio";
    public static final String COL_ARTIST_FOLLOWERS = "followers_count";
    public static final String COL_ARTIST_ACTIVE    = "active";

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
    public static final String COL_PLAYLIST_UPDATED_AT = "updated_at";

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
                COL_USER_CREATED_AT + " TEXT DEFAULT (datetime('now'))," +
                COL_USER_UPDATED_AT + " TEXT DEFAULT (datetime('now')))");

        db.execSQL("CREATE TABLE " + TABLE_ARTISTS + "(" +
                COL_ARTIST_ID + " TEXT PRIMARY KEY," +
                COL_ARTIST_NAME + " TEXT NOT NULL," +
                COL_ARTIST_IMAGE + " TEXT," +
                COL_ARTIST_BIO + " TEXT," +
                COL_ARTIST_FOLLOWERS + " INTEGER DEFAULT 0," +
                COL_ARTIST_ACTIVE + " INTEGER DEFAULT 1)");

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
                COL_PLAYLIST_UPDATED_AT + " TEXT DEFAULT (datetime('now'))," +
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

    private void insertSampleData(SQLiteDatabase db) {

        // 1. TABLE USERS
        db.execSQL("INSERT INTO " + TABLE_USERS + " VALUES" +
                "('u001','dev','dev@wave.com','$2a$12$dAHDlXmMPT9SXbITlkNEM.XT4bgIRhCJ804p6SR3f90otTwV/9AIm','Dev',NULL,'ADMIN','2026-05-02T00:00:00.0','2026-05-02T00:00:00.0')," +
                "('u002','admin','admin@wave.com','$2a$12$m1d70IyAOOA4VlZ.MaHFJu6ZTQu5Izbu.xntueTChqGRYgFMO/qB6','Admin',NULL,'ADMIN','2026-05-01T00:00:00.0','2026-05-01T00:00:20.0')," +
                "('u003','usera','a@gmail.com','$2a$12$Pp/lKLhmqMgYWWxfpvpRReztpj4aUCEN2KnTLUOzr3IJKXopTv5hG','Nguyễn Văn A',NULL,'USER','2026-05-02T00:00:00.0','2026-05-02T00:00:00.0')," +
                "('u004','userb','b@gmail.com','$2a$12$43vYkiVmDCJ0lDEVCbQHzujGG2RXcem7jb5wyvOoB4E3fDcyImaG2','Trần Văn B',NULL,'USER','2026-05-02T00:00:00.0','2026-05-02T00:00:00.0')," +
                "('u005','userc','c@gmail.com','$2a$12$5jki0VST/gEnozz/REEPJuFSsBkdiEAa/kvHtn/SlChxTG1CeeauC','Lê Thị C',NULL,'USER','2026-05-02T00:00:00.0','2026-05-02T00:00:00.0')");

        // 2. TABLE GENRES
        db.execSQL("INSERT INTO " + TABLE_GENRES + " VALUES" +
                "('ballad','Ballad','Nhạc trữ tình tình cảm','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_image%2Fballad.jpg?alt=media&token=e1ca0ed7-64a0-4fec-af52-0c7365ff4d66')," +
                "('edm','EDM','Electronic Dance Music sôi động','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_image%2Fedm.jpg?alt=media&token=aa01228f-9f79-46dc-8f38-c8cafa430347')," +
                "('vpop','V-Pop','Nhạc Pop Việt Nam thịnh hành','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_image%2Fvpop.jpg?alt=media&token=eccd8f87-01b9-4df3-a9be-51326c4a5d32')," +
                "('indie','Indie','Nhạc độc lập mộc mạc','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_image%2Findie.jpg?alt=media&token=4e0c9b9e-487c-4f4b-a450-4ed43d0714ac')," +
                "('rap','Rap/HipHop','Nhạc Rap underground và mainstream','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/genres_image%2Frap.jpg?alt=media&token=b3a7304a-38ec-4603-8b2b-2a94bf191a5b')");

        // 3. TABLE ARTISTS
        db.execSQL("INSERT INTO " + TABLE_ARTISTS + " VALUES" +
                "('a001','Taylor Swift','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FTaylor.jpg?alt=media&token=b158701f-ee29-47ad-b487-a1de6cf9627b','Ca sĩ nhạc Pop hàng đầu thế giới',152000000,1)," +
                "('a002','Hoàng Dũng','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FHo%C3%A0ng%20D%C5%A9ng.jpg?alt=media&token=40970181-abc3-4623-a7d6-57ef895571af','Hoàng tử tình ca Ballad Việt Nam',820000,1)," +
                "('a003','Bùi Trường Linh','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FB%C3%B9i%20Tr%C6%B0%E1%BB%9Dng%20Linh.jpg?alt=media&token=2b8ce7fa-f968-4441-8357-6ada9d0913f6','Ca sĩ kiêm nhạc sĩ tạo hit tài năng',910000,1)," +
                "('a004','Vũ','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FV%C5%A9.jpg?alt=media&token=28fc6b58-6c8e-4cd5-ad40-d641a05cfa8c','Hoàng tử Indie Việt Nam',1500000,1)," +
                "('a005','Sơn Tùng M-TP','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FS%C6%A1n%20T%C3%B9ng%20MTP.jpg?alt=media&token=9a163356-cee6-4299-9c18-2be0a5ff3020','Ngôi sao nhạc Pop hàng đầu Việt Nam',50000000,1)," +
                "('a006','HIEUTHUHAI','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2Fartistscover_1765036795443-HieuThuHai.jpg?alt=media&token=18a6f310-d11d-4e5f-8017-1bca6a2f8b7b','Rapper điển trai, quốc dân',3200000,1)," +
                "('a007','Đen Vâu','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2F%C4%90en%20V%C3%A2u.jpg?alt=media&token=3deef73a-a90e-4dce-8b4f-a64ec5aa9f21','Rapper của những bài ca triết lý',4500000,1)," +
                "('a008','tlinh','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FTlinh.jpg?alt=media&token=8e79b4a1-586a-47d4-b5af-2eba4d53a305','Nữ nghệ sĩ GenZ đầy cá tính',1800000,1)," +
                "('a009','Alan Walker','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FAlan%20Walker.jpg?alt=media&token=bde2aedc-4e5e-45d0-b980-d617ce426e3e','DJ/Producer nhạc điện tử thế giới',42000000,1)," +
                "('a010','Grey D','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/artist_avatars%2FGrey%20D.jpg?alt=media&token=997cfe71-587a-475f-a154-db5ff1f8b10a','Hoàng tử dòng nhạc mộng mơ',1100000,1)");

        // 4. TABLE ALBUMS
        db.execSQL("INSERT INTO " + TABLE_ALBUMS + " VALUES" +
                "('al001','Lover','a001','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FTaylor%20Swift.jpg?alt=media&token=4a168906-772d-4c58-80b9-15e5318ae7bb','2014-10-27',89000000)," +
                "('al002','Xoay Vòng','a002','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=a0bff26c-b6ed-4b40-9a6b-eb6dbffbc6d4','2020-12-15',1500000)," +
                "('al003','Từng Ngày Như Mãi Mãi','a003','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=ab5961fe-8cee-4a05-a605-16aac0230d73','2023-03-15',15000000)," +
                "('al004','Xoay Tròn','a002','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=422e06e2-1820-4709-8ccf-5dd728004a61','2024-05-15',500000)," +
                "('al005','Bảo Tàng Của Nuối Tiếc','a004','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FB%E1%BA%A3o%20t%C3%A0ng%20c%E1%BB%A7a%20nu%E1%BB%91i%20ti%E1%BA%BFc.jpg?alt=media&token=5f170ed7-f13b-449d-a2e5-29ba01fd927e','2024-09-25',2300000)," +
                "('al006','Chúng Ta Của Tương Lai','a005','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FCh%C3%BAng%20ta%20c%E1%BB%A7a%20t%C6%B0%C6%A1ng%20lai.jpg?alt=media&token=47e6995a-56e2-4f91-afda-e341a8bf3ba6','2024-03-08',45000000)," +
                "('al007','Ai Cũng Phải Bắt Đầu Từ Đâu Đó','a006','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FAi%20c%C5%A9ng%20ph%E1%BA%A3i%20b%E1%BA%AFt%20%C4%91%E1%BA%A7u%20t%E1%BB%AB%20%C4%91%C3%A2u%20%C4%91%C3%B3.jpg?alt=media&token=27bcd59c-7891-4222-b498-03095cf30c37','2023-10-25',12000000)," +
                "('al008','Dongvui Harmony','a007','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FDongvui%20hamony.jpg?alt=media&token=f01bd11f-1d78-47d4-ab76-4c80be7e30f0','2024-04-10',35000000)," +
                "('al009','ái','a008','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2F%C3%81I.jpg?alt=media&token=792c0375-0c2d-47d3-a527-5651516308cb','2023-08-15',18000000)," +
                "('al010','Different World','a009','https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/album_covers%2FDiferent%20Word.jpg?alt=media&token=4e6a2715-b95a-4f14-b635-ef94303a0f43','2018-12-14',500000000)");

        // 5. TABLE SONGS
        db.execSQL("INSERT INTO " + TABLE_SONGS + " VALUES" +
                // --- Nhóm Bùi Trường Linh ---
                "('song_001','Từng Ngày Như Mãi Mãi','a003','al003','ballad',274," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FT%E1%BB%ABng%20Ng%C3%A0y%20Nh%C6%B0%20M%C3%A3i%20M%C3%A3i%20_%20buitruonglinh%20%5Bp3FnuJnm8iQ%5D.mp3?alt=media&token=7fb7eacc-89a0-423e-a576-f1a5e2ee745f'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=4f0046a4-5a42-4519-98d1-236be3b2cb48'," +
                "'Anh chưa từng hết yêu em...',0,0)," +

                "('song_002','Em Ơi Là Em','a003','al003','ballad',250," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FEm%20%C6%A0i%20L%C3%A0%20Em%20_%20buitruonglinh%20(ft.%20Ki%E1%BB%81u%20Chi%2C%20BMZ)%20%5BKOlX-v0q-8A%5D.mp3?alt=media&token=f3959c2c-75b3-4851-8293-85a82f43dcd7'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FT%E1%BB%ABng%20ng%C3%A0y%20nh%C6%B0%20m%C3%A3i%20m%C3%A3i.jpg?alt=media&token=4f0046a4-5a42-4519-98d1-236be3b2cb48'," +
                "'Vì em nói bao lần em không thích đi lòng vòng...',4200,120)," +

                // --- Nhóm Taylor Swift ---
                "('song_003','Enchanted','a001','al001','edm',231," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FTaylor%20Swift%20-%20Enchanted.mp4?alt=media&token=b3add129-43e7-40ca-8260-a7e9bc1541b2'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2Fenchanted.jpg?alt=media&token=2c9423f6-d584-4f11-bdbd-3fb246c489c9'," +
                "'Please dont be in love with someone else...',1200,450)," +

                "('song_004','Blank Space','a001','al001','vpop',231," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FTaylor%20Swift%20-%20Enchanted.mp4?alt=media&token=b3add129-43e7-40ca-8260-a7e9bc1541b2'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FBlank%20Space.jpg?alt=media&token=36de5a59-54b2-4cc5-8f2b-42c3a4698b36'," +
                "'Got a long list of ex-lovers...',25000,9900)," +

                // --- Nhóm Hoàng Dũng ---
                "('song_005','Em Trồng Cây','a002','al004','ballad',252," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20EM%20TR%E1%BB%92NG%20C%C3%82Y%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5B_LUnDykB1HQ%5D.mp3?alt=media&token=ef83a2e2-3eb6-42e7-9e2e-df3c4b00875e'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=09d2d3c7-f414-4922-8c00-84256bed7c87'," +
                "'Em trồng cây, em vun bón...',3500,980)," +

                "('song_006','Giữ Anh Cho Ngày Hôm Qua','a002','al004','ballad',245," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20GI%E1%BB%AE%20ANH%20CHO%20NG%C3%80Y%20H%C3%94M%20QUA%20(feat.%20RHYMASTIC)%20_%20OFFICIAL%20MUSIC%20VIDEO.mp4?alt=media&token=57b86eeb-21e6-4778-94bc-9f3b60b9a982'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=09d2d3c7-f414-4922-8c00-84256bed7c87'," +
                "'Hãy giữ anh cho ngày hôm qua...',4100,210)," +

                "('song_007','Sâm Phanh','a002','al002','ballad',252," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHO%C3%80NG%20D%C5%A8NG%20-%20S%C3%82M-PANH%20_%20ALBUM%20XOAY%20TR%C3%92N%20%5BnexEFjR69zE%5D.mp3?alt=media&token=b1b43327-c200-4c89-8621-62f0f2ff6e27'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FXoay%20Tr%C3%B2n.jpg?alt=media&token=09d2d3c7-f414-4922-8c00-84256bed7c87'," +
                "'Hóa ra là... Hóa ra ta ưu tư thôi mà...',95000,4300)," +

                // --- Nhóm Vũ. ---
                "('song_008','Không Yêu Em Thì Yêu AI','a004','al005','indie',260," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FKh%C3%B4ng%20Y%C3%AAu%20Em%20Thi%CC%80%20Y%C3%AAu%20Ai_%20_%20Vu%CC%83.%20ft.%20Low%20G%20(t%C6%B0%CC%80%20Album%20_Ba%CC%89o%20Ta%CC%80ng%20Cu%CC%89a%20Nu%C3%B4%CC%81i%20Ti%C3%AA%CC%81c_)%20%5Bo-2yt0ZZZ6o%5D.mp3?alt=media&token=005a4a70-145d-456c-a5ef-0252ac3360d4'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FKh%C3%B4ng%20y%C3%AAu%20em%20th%C3%AC%20y%C3%AAu%20ai.jpg?alt=media&token=01c650f8-7297-4c76-8a44-1712c369902f'," +
                "'Kìa ngoài song cửa...',84000,3200)," +

                "('song_009','Bình Yên','a004','al005','indie',255," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2Fbi%CC%80nh%20y%C3%AAn%20_%20Vu%CC%83.%20ft.%20Binz%20(Official%20MV)%20t%C6%B0%CC%80%20Album%20_Ba%CC%89o%20Ta%CC%80ng%20Cu%CC%89a%20Nu%C3%B4%CC%81i%20Ti%C3%AA%CC%81c_%20%5Bf9P7_qWrf38%5D.mp3?alt=media&token=87285088-a545-44bd-8760-16d60f06c835'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FB%C3%ACnh%20y%C3%AAn.jpg?alt=media&token=c9b35efc-9fb4-4716-81a1-3aa6af0ccfe8'," +
                "'Cuộc đời cuốn xô ta bước qua nhau...',73000,2900)," +

                // --- Nhóm Sơn Tùng M-TP ---
                "('song_010','Chúng Ta Của Tương Lai','a005','al006','vpop',247," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FS%C6%A0N%20T%C3%99NG%20M-TP%20_%20CH%C3%9ANG%20TA%20C%E1%BB%A6A%20T%C6%AF%C6%A0NG%20LAI%20_%20OFFICIAL%20MUSIC%20VIDEO.mp4?alt=media&token=608a55d3-5fa0-4eed-a610-cc9cb5ba021f'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FCh%C3%BAng%20ta%20c%E1%BB%A7a%20t%C6%B0%C6%A1ng%20lai.jpg?alt=media&token=8c3f88b2-7b83-4a58-80e5-596467a75ddf'," +
                "'Liệu mai sau hai ta có gặp lại...',120000,5400)," +

                "('song_011','Đừng Làm Trái Tim Anh Đau','a005','al006','vpop',246," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2F%C4%90%E1%BB%ABng%20L%C3%A0m%20Tr%C3%A1i%20Tim%20Anh%20%C4%90au%20%5B7u4g483WTzw%5D.mp3?alt=media&token=9d281028-7208-4ad4-997c-ddde0d66da9f'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2F%C4%90%E1%BB%ABng%20l%C3%A0m%20tr%C3%A1i%20tim%20anh%20%C4%91au.jpg?alt=media&token=44b72325-276e-4f27-a148-4a2a865d675b'," +
                "'Hãy trao cho anh thứ anh đang mong chờ...',340000,12000)," +

                // --- Nhóm HIEUTHUHAI ---
                "('song_012','Không Thể Say','a006','al007','rap',198," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FHIEUTHUHAI%20-%20Kh%C3%B4ng%20Th%E1%BB%83%20Say%20(prod.%20by%20Kewtiie)%20l%20Official%20Video.mp4?alt=media&token=ffd666d3-e24c-4c17-ac8c-939042a0921a'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FKh%C3%B4ng%20th%E1%BB%83%20say.jpg?alt=media&token=09bcc0dd-9eda-40a3-9831-dab29ec3e530'," +
                "'Rót thêm một ly nữa đi...',89000,4500)," +

                "('song_013','5050','a006','al007','rap',210," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FREX%20-%205050%20Remix%20(feat.%20HURRYKNG%2C%20MANBO%20%26%20HIEUTHUHAI)%20_%20Lyric%20Video%20%5BALv_RxKH4E8%5D.mp3?alt=media&token=4f3ff8b4-fff7-4f18-ae7e-65c11e0cd6d9'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2F5050.jpg?alt=media&token=93167063-eb45-4c83-bc04-09e9f5df7f0c'," +
                "'Em chỉ cần vài ba tháng thôi...',92000,6100)," +

                // --- Nhóm Đen Vâu ---
                "('song_014','Hai Triệu Năm','a007','al008','rap',245," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2F%C4%90en%20-%20hai%20tri%E1%BB%87u%20n%C4%83m%20ft.%20Bi%C3%AAn%20(Madihu%20mix)%20behind%20the%20scenes%20%5BhZq4uewe-_Y%5D.mp3?alt=media&token=5a5c24ad-be16-4491-9b9b-9b6d3d12815c'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FHai%20tri%E1%BB%87u%20n%C4%83m.jpg?alt=media&token=bbd7cec3-bd59-44ba-bd3d-e6216973b12e'," +
                "'Anh đi tìm em như thể chơi trốn tìm...',150000,7500)," +

                "('song_015','Cho Tôi Lang Thang','a007','al008','rap',280," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FNg%E1%BB%8Dt%20vc.%20%C4%90en%20-%20Cho%20T%E1%BB%AB%20Lang%20Thang.mp4?alt=media&token=f805a4ca-90a3-4338-b085-7cf047d60973'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FCho%20T%C3%B4i%20Lang%20Thang.jpg?alt=media&token=a144510f-eefa-4b57-ae3c-34d05c154d84'," +
                "'Mang tiền về cho mẹ đừng mang ưu phiền...',180000,8200)," +

                // --- Nhóm tlinh ---
                "('song_016','Nếu Lúc Đó','a008','al009','rap',215," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2Ftlinh%20-%20n%E1%BA%BFu%20l%C3%BAc%20%C4%91%C3%B3%20(ft.%202pillz)%20_%20OFFICIAL%20MUSIC%20VIDEO%20%5BfyMgBQioTLo%5D.mp3?alt=media&token=8887318a-404b-4f10-9d52-770c8559c9c0'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FN%E1%BA%BFu%20l%C3%BAc%20%C4%91%C3%B3%20-%20Tlinh.jpg?alt=media&token=6d1b30bc-6367-4a3e-9aaf-66963fe21d10'," +
                "'Nếu lúc đó mình đừng buông tay...',85000,4100)," +

                "('song_017','Người Điên','a008','al009','vpop',190," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2Ftlinh%20-%20ng%C6%B0%E1%BB%9Di%20%C4%91i%C3%AAn%20_%20OFFICIAL%20VISUALIZER%20%5BdoN4GrGoVpA%5D.mp3?alt=media&token=1a077b11-4b23-412d-9f75-24f55fe120f8'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FNg%C6%B0%E1%BB%9Di%20%C4%91i%C3%AAn.jpg?alt=media&token=6faae5d3-a25b-444d-b7e3-d19b72936e9f'," +
                "'Em chỉ là người điên vô cùng ở tận thế...',64000,2100)," +

                // --- Nhóm Alan Walker ---
                "('song_018','Getaway','a009','al010','edm',212," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FAlan%20Walker%2C%20Emyrson%20Flora%20-%20Getaway%20(Official%20Music%20Video)%20%5BjjQSeEQYE8k%5D.mp3?alt=media&token=e035a2a6-3d8f-4916-a6d1-81e03c4f4adc'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2Fgetaway.jpg?alt=media&token=f91cabdb-45a3-4cfa-813f-f5d85c31740d'," +
                "'Where are you now? Another dream...',990000,43000)," +

                "('song_019','Monters','a009','al010','edm',163," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FAlan%20Walker%2C%20Emyrson%20Flora%20-%20Monster%20(Official%20Music%20Video)%20%5Bvv0YJOY5txc%5D.mp3?alt=media&token=50fc6069-a72c-466a-a24c-93229793efec'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FMonters.jpg?alt=media&token=f89df0ff-beb3-485b-98cc-3b9445df8a22'," +
                "'I know I am not alone...',650000,31000)," +

                // --- Nhóm Grey D ---
                "('song_020','Hóa Ra','a010',NULL,'vpop',222," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_files%2FGREY%20D%20-%20ho%C3%A1%20ra%E2%80%A6.mp4?alt=media&token=d38c5e1a-403a-4808-90d6-da0136c69f68'," +
                "'https://firebasestorage.googleapis.com/v0/b/waveapp-8afdf.firebasestorage.app/o/songs_covers%2FH%C3%B3a%20ra.jpg?alt=media&token=fc4284b2-c06a-4840-b9f2-42ce0855c44d'," +
                "'Cảm ơn vì đã cùng anh đi qua bão giông...',98000,4200)");

        // 6. CÁC BẢNG LIÊN KẾT
        db.execSQL("INSERT INTO " + TABLE_PLAYLISTS +
                "(" + COL_PLAYLIST_ID + "," +
                COL_PLAYLIST_USER_ID + "," +
                COL_PLAYLIST_NAME + "," +
                COL_PLAYLIST_IMAGE + "," +
                COL_PLAYLIST_CREATED_AT + "," +
                COL_PLAYLIST_UPDATED_AT + ") VALUES" +
                "('pl001','u001','Nhạc Tuyển Chọn 1',NULL,'2026-02-01','2026-02-01')," +
                "('pl002','u001','Top Hits Việt Nam',NULL,'2026-02-10','2026-02-10')");

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
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ARTISTS + " ADD COLUMN " +
                    COL_ARTIST_ACTIVE + " INTEGER DEFAULT 1");
            return;
        }
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


    public Artist insertArtistDirect(String name, String image, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        String artistId = generateNextArtistId(db);

        ContentValues values = new ContentValues();
        values.put(COL_ARTIST_ID, artistId);
        values.put(COL_ARTIST_NAME, name);
        values.put(COL_ARTIST_IMAGE, image);
        values.put(COL_ARTIST_BIO, bio);
        values.put(COL_ARTIST_FOLLOWERS, 0);
        values.put(COL_ARTIST_ACTIVE, 1);

        long rowId = db.insertWithOnConflict(TABLE_ARTISTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowId == -1) {
            return null;
        }

        return Artist.builder()
                .id(artistId)
                .name(name)
                .image(image)
                .bio(bio)
                .followersCount(0)
                .active(true)
                .build();
    }

    private String generateNextArtistId(SQLiteDatabase db) {
        Cursor c = db.rawQuery(
                "SELECT " + COL_ARTIST_ID +
                        " FROM " + TABLE_ARTISTS +
                        " WHERE " + COL_ARTIST_ID + " GLOB 'a[0-9]*'" +
                        " ORDER BY CAST(SUBSTR(" + COL_ARTIST_ID + ", 2) AS INTEGER) DESC LIMIT 1",
                null
        );
        try {
            int nextNumber = 1;
            if (c != null && c.moveToFirst()) {
                String lastId = c.getString(0);
                if (lastId != null && lastId.length() > 1) {
                    nextNumber = Integer.parseInt(lastId.substring(1)) + 1;
                }
            }
            return String.format(Locale.US, "a%03d", nextNumber);
        } catch (NumberFormatException e) {
            return "a001";
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
