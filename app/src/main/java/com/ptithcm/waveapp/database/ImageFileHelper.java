package com.ptithcm.waveapp.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import java.io.*;

/**
 * Quản lý file ảnh trong Internal Storage của app
 *
 * LUỒNG:
 *   User chọn ảnh → Uri
 *   → saveImageFromUri() → lưu file vào /data/.../files/images/...
 *   → trả về path (String) → lưu TEXT vào SQLite
 *   → hiển thị: loadIntoImageView(context, path, imageView, placeholder)
 */
public class ImageFileHelper {

    // Thư mục con trong Internal Storage
    public static final String DIR_ALBUMS    = "images/albums";
    public static final String DIR_ARTISTS   = "images/artists";
    public static final String DIR_SONGS     = "images/songs";
    public static final String DIR_AVATARS   = "images/avatars";
    public static final String DIR_PLAYLISTS = "images/playlists";
    public static final String DIR_GENRES    = "images/genres";

    /**
     * Lưu ảnh từ Gallery (Uri) vào Internal Storage
     * @return path tương đối lưu vào SQLite (ví dụ: "images/albums/al001.jpg")
     */
    public static String saveImageFromUri(Context ctx, Uri uri, String subDir, String id) {
        try {
            File dir = new File(ctx.getFilesDir(), subDir);
            if (!dir.exists()) dir.mkdirs();

            File out = new File(dir, id + ".jpg");
            try (InputStream in  = ctx.getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) fos.write(buf, 0, len);
            }
            return subDir + "/" + id + ".jpg";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lưu ảnh từ Bitmap (Camera) vào Internal Storage
     * @return path tương đối lưu vào SQLite
     */
    public static String saveImageFromBitmap(Context ctx, Bitmap bm, String subDir, String id) {
        try {
            File dir = new File(ctx.getFilesDir(), subDir);
            if (!dir.exists()) dir.mkdirs();

            File out = new File(dir, id + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                bm.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            }
            return subDir + "/" + id + ".jpg";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Đọc path từ SQLite → Bitmap để hiển thị
     */
    public static Bitmap loadBitmap(Context ctx, String path) {
        if (path == null || path.isEmpty()) return null;
        File file = new File(ctx.getFilesDir(), path);
        if (!file.exists()) return null;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    /**
     * Load ảnh lên ImageView, hiện placeholder nếu không có ảnh
     */
    public static void loadIntoImageView(Context ctx, String path,
                                         ImageView iv, int placeholder) {
        Bitmap bm = loadBitmap(ctx, path);
        if (bm != null) iv.setImageBitmap(bm);
        else            iv.setImageResource(placeholder);
    }

    /** Xóa file ảnh khi xóa record */
    public static void deleteImage(Context ctx, String path) {
        if (path == null) return;
        new File(ctx.getFilesDir(), path).delete();
    }

    // ── Tên path chuẩn theo loại ─────────────────────────
    public static String albumPath(String albumId)      { return DIR_ALBUMS    + "/" + albumId    + ".jpg"; }
    public static String artistPath(String artistId)    { return DIR_ARTISTS   + "/" + artistId   + ".jpg"; }
    public static String songPath(String songId)        { return DIR_SONGS     + "/" + songId     + ".jpg"; }
    public static String avatarPath(String userId)      { return DIR_AVATARS   + "/" + userId     + ".jpg"; }
    public static String playlistPath(String playlistId){ return DIR_PLAYLISTS + "/" + playlistId + ".jpg"; }
    public static String genrePath(String genreId)      { return DIR_GENRES    + "/" + genreId    + ".jpg"; }
}