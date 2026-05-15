package com.ptithcm.waveapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.dto.response.SongResponse;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<SongResponse> songs;
    private final OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(SongResponse song);
    }

    public SongAdapter(List<SongResponse> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        SongResponse song = songs.get(position);
        holder.tvName.setText(song.getName());
        holder.tvArtist.setText(song.getArtistName());
        Glide.with(holder.itemView.getContext())
                .load(song.getImage())
                .placeholder(R.drawable.ic_logo)
                .into(holder.imgSong);

        holder.itemView.setOnClickListener(v -> listener.onSongClick(song));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView tvName, tvArtist;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.imgSong);
            tvName = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtistName);
        }
    }
}
