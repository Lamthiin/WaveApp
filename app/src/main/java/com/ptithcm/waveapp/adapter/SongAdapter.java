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
import com.ptithcm.waveapp.ServiceLocator;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.util.ImageFileHelper;
import com.ptithcm.waveapp.util.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnSongClickListener { void onSongClick(Song song); }
    public interface OnLikeClickListener { void onLikeClick(Song song, int position); }
    public interface OnMoreClickListener { void onMoreClick(Song song, int position); }

    private List<Song> songs;
    private OnSongClickListener onSongClick;
    private OnLikeClickListener onLikeClick;
    private OnMoreClickListener onMoreClick;

    public SongAdapter() {
        this.songs = new ArrayList<>();
    }

    public SongAdapter(List<Song> songs) {
        this.songs = songs != null ? songs : new ArrayList<>();
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.onSongClick = listener;
    }

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.onLikeClick = listener;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        this.onMoreClick = listener;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs != null ? songs : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addSongs(List<Song> moreSongs) {
        if (moreSongs == null) return;
        int start = this.songs.size();
        this.songs.addAll(moreSongs);
        notifyItemRangeInserted(start, moreSongs.size());
    }

    public void clearSongs() {
        this.songs.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        Context context = holder.itemView.getContext();

        if (holder.tvIndex != null) {
            holder.tvIndex.setText(String.valueOf(position + 1));
        }
        holder.tvName.setText(song.getName());
        holder.tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "");

        ImageFileHelper.loadIntoImageView(context, song.getImage(), holder.imgSong, R.drawable.ic_music_note);

        holder.itemView.setOnClickListener(v -> {
            if (onSongClick != null) {
                onSongClick.onSongClick(song);
            }
        });

        if (holder.btnLike != null) {
            String userId = new TokenManager(context).getUserId();
            boolean isLiked = false;
            if (userId != null) {
                isLiked = ServiceLocator.getInstance().likedSongRepository.existsByUserIdAndSongId(userId, song.getId());
            }
            holder.btnLike.setImageResource(R.drawable.ic_more_vert);
            holder.btnLike.setColorFilter(context.getColor(R.color.gray_text));
            holder.btnLike.setOnClickListener(v -> {
                if (onMoreClick != null) {
                    onMoreClick.onMoreClick(song, position);
                } else if (onLikeClick != null) {
                    onLikeClick.onLikeClick(song, position);
                }
            });
        }

        if (holder.btnMore != null && holder.btnMore != holder.btnLike) {
            holder.btnMore.setOnClickListener(v -> {
                if (onMoreClick != null) {
                    onMoreClick.onMoreClick(song, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView tvIndex;
        TextView tvName;
        TextView tvArtist;
        ImageView btnLike;
        ImageView btnMore;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.imgSong);
            tvIndex = itemView.findViewById(R.id.tvSongIndex);
            tvName = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtistName);
            btnLike = itemView.findViewById(R.id.btnLike);
            if (btnLike == null) {
                btnLike = itemView.findViewById(R.id.favoriteButton);
            }
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
