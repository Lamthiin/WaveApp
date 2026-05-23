package com.ptithcm.waveapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Playlist playlist, int position);
    }

    private List<Playlist> playlists = new ArrayList<>();
    private OnPlaylistClickListener onPlaylistClick;
    private OnDeleteClickListener onDeleteClick;

    public void setOnPlaylistClickListener(OnPlaylistClickListener listener) {
        this.onPlaylistClick = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClick = listener;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists != null ? playlists : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position >= 0 && position < playlists.size()) {
            playlists.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_playlist, parent, false);

        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        Context context = holder.itemView.getContext();

        if (holder.tvName != null) {
            holder.tvName.setText(playlist.getName());
        }

        if (holder.tvMeta != null) {
            holder.tvMeta.setVisibility(View.VISIBLE);
            holder.tvMeta.setText(formatUpdatedAt(playlist.getUpdatedAt()));
        }

        if (holder.imgPlaylist != null) {
            ImageFileHelper.loadIntoImageView(
                    context,
                    playlist.getImage(),
                    holder.imgPlaylist,
                    R.drawable.ic_playlist
            );
        }

        holder.itemView.setOnClickListener(v -> {
            if (onPlaylistClick != null) {
                onPlaylistClick.onPlaylistClick(playlist);
            }
        });

        if (holder.btnMore != null) {
            holder.btnMore.setVisibility(View.GONE);
        }
    }

    private String formatUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt == null) {
            return "Chưa cập nhật";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "Cập nhật: " + updatedAt.format(formatter);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPlaylist;
        TextView tvName;
        TextView tvMeta;
        ImageView btnMore;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPlaylist = itemView.findViewById(R.id.img_playlist_thumbnail);
            if (imgPlaylist == null) {
                imgPlaylist = itemView.findViewById(R.id.imgPlaylist);
            }

            tvName = itemView.findViewById(R.id.tv_playlist_name);
            if (tvName == null) {
                tvName = itemView.findViewById(R.id.tvPlaylistName);
            }

            tvMeta = itemView.findViewById(R.id.tv_playlist_meta);
            if (tvMeta == null) {
                tvMeta = itemView.findViewById(R.id.tvSongCount);
            }

            btnMore = itemView.findViewById(R.id.btn_more);
            if (btnMore == null) {
                btnMore = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}