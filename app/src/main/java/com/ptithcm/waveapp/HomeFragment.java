package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.config.ServiceLocator;
import com.ptithcm.waveapp.controller.HomeController;
import com.ptithcm.waveapp.dto.response.AlbumResponse;
import com.ptithcm.waveapp.dto.response.ApiResponse;
import com.ptithcm.waveapp.dto.response.HomeResponse;

import java.util.List;

public class HomeFragment extends Fragment {

    private HomeController homeController;
    private LinearLayout albumContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        homeController = ServiceLocator.getInstance().getHomeController();
        albumContainer = view.findViewById(R.id.album_container); // Cần thêm ID này vào XML

        loadHomeData();
        
        return view;
    }

    private void loadHomeData() {
        ApiResponse<HomeResponse> response = homeController.getHome();
        if (response.isSuccess()) {
            displayAlbums(response.getData().getFeaturedAlbums());
        }
    }

    private void displayAlbums(List<AlbumResponse> albums) {
        if (albumContainer == null) return;
        albumContainer.removeAllViews();

        for (AlbumResponse album : albums) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_album, albumContainer, false);
            
            ImageView imgAlbum = itemView.findViewById(R.id.imgAlbum);
            TextView tvName = itemView.findViewById(R.id.tvAlbumName);
            TextView tvArtist = itemView.findViewById(R.id.tvArtistName);

            tvName.setText(album.getName());
            tvArtist.setText(album.getArtistName());
            
            Glide.with(this).load(album.getImage()).placeholder(R.drawable.ic_logo).into(imgAlbum);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
                intent.putExtra("ALBUM_ID", album.getId());
                startActivity(intent);
            });

            albumContainer.addView(itemView);
        }
    }
}
