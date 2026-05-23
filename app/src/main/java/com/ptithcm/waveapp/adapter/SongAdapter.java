package com.ptithcm.waveapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public enum ActionIconMode {
        ADD,
        DELETE
    }

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public interface OnLikeClickListener {
        void onLikeClick(Song song, int position);
    }

    public interface OnMoreClickListener {
        void onMoreClick(Song song, int position);
    }

    private List<Song> songs;
    private ActionIconMode actionIconMode = ActionIconMode.ADD;

    private OnSongClickListener onSongClick;
    private OnLikeClickListener onLikeClick;
    private OnMoreClickListener onMoreClick;

    public SongAdapter() {
        this.songs = new ArrayList<>();
    }

    public SongAdapter(List<Song> songs) {
        this.songs = songs != null ? songs : new ArrayList<>();
    }

    public void setActionIconMode(ActionIconMode mode) {
        this.actionIconMode = mode != null ? mode : ActionIconMode.ADD;
        notifyDataSetChanged();
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);

        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        Context context = holder.itemView.getContext();

        if (holder.tvIndex != null) {
            holder.tvIndex.setText(String.valueOf(position + 1));
        }

        if (holder.tvName != null) {
            holder.tvName.setText(song.getName());
        }

        if (holder.tvArtist != null) {
            holder.tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "");
        }

        if (holder.imgSong != null) {
            ImageFileHelper.loadIntoImageView(
                    context,
                    song.getImage(),
                    holder.imgSong,
                    R.drawable.ic_music_note
            );
        }

        int iconRes = actionIconMode == ActionIconMode.DELETE
                ? R.drawable.ic_delete
                : R.drawable.ic_add;

        int iconColor = actionIconMode == ActionIconMode.DELETE
                ? Color.parseColor("#FF5252")
                : context.getColor(R.color.spotify_green);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) return;
            if (adapterPosition < 0 || adapterPosition >= songs.size()) return;

            if (onSongClick != null) {
                onSongClick.onSongClick(songs.get(adapterPosition));
            }
        });

        if (holder.btnLike != null) {
            holder.btnLike.setImageResource(iconRes);
            holder.btnLike.setColorFilter(iconColor);

            holder.btnLike.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();

                if (adapterPosition == RecyclerView.NO_POSITION) return;
                if (adapterPosition < 0 || adapterPosition >= songs.size()) return;

                Song clickedSong = songs.get(adapterPosition);

                if (onMoreClick != null) {
                    onMoreClick.onMoreClick(clickedSong, adapterPosition);
                } else if (onLikeClick != null) {
                    onLikeClick.onLikeClick(clickedSong, adapterPosition);
                }
            });
        }

        if (holder.btnMore != null && holder.btnMore != holder.btnLike) {
            holder.btnMore.setImageResource(iconRes);
            holder.btnMore.setColorFilter(iconColor);

            holder.btnMore.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();

                if (adapterPosition == RecyclerView.NO_POSITION) return;
                if (adapterPosition < 0 || adapterPosition >= songs.size()) return;

                if (onMoreClick != null) {
                    onMoreClick.onMoreClick(songs.get(adapterPosition), adapterPosition);
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
            if (imgSong == null) {
                imgSong = itemView.findViewById(R.id.img_song_thumbnail);
            }

            tvIndex = itemView.findViewById(R.id.tvSongIndex);

            tvName = itemView.findViewById(R.id.tvSongTitle);
            if (tvName == null) {
                tvName = itemView.findViewById(R.id.tv_song_title);
            }

            tvArtist = itemView.findViewById(R.id.tvArtistName);
            if (tvArtist == null) {
                tvArtist = itemView.findViewById(R.id.tv_artist_name);
            }

            btnLike = itemView.findViewById(R.id.btnLike);
            if (btnLike == null) {
                btnLike = itemView.findViewById(R.id.favoriteButton);
            }

            btnMore = itemView.findViewById(R.id.btnMore);
            if (btnMore == null) {
                btnMore = itemView.findViewById(R.id.btn_more);
            }
        }
    }
}