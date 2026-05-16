package com.ptithcm.waveapp.util;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.database.DatabaseHelper;

public class ImageFileHelper {
    // Trong file ImageFileHelper.java
    public static void loadIntoImageView(Context context, String url, ImageView imageView, int placeholderResId) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        Glide.with(context)
                .load(url) // Load trực tiếp URL từ Firebase bạn lấy dưới DB
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .into(imageView);
    }
}
