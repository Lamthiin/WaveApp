package com.ptithcm.waveapp.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageFileHelper {

    public static void loadIntoImageView(Context context, String url, ImageView imageView, int placeholderResId) {
        if (context == null || imageView == null) return;

        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        Glide.with(context)
                .load(url.trim())
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .centerCrop()
                .into(imageView);
    }
}