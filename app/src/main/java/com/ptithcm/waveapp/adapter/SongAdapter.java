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
import com.ptithcm.waveapp.model.Song;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Dùng cho:
 *   - HomeFragment         (bảng xếp hạng)
 *   - SongsByCategoryActivity
 *   - PlaylistDetailActivity
 *   - AddSongsToPlaylistActivity
 *   - fragment_library (tabSongs - bài hát yêu thích)
 *   - fragment_search  (tabSongs)
 */
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

    // ── Setters listener ─────────────────────────────────
    public void setOnSongClickListener(OnSongClickListener l) { this.onSongClick = l; }
    public void setOnLikeClickListener(OnLikeClickListener l) { this.onLikeClick = l; }
    public void setOnMoreClickListener(OnMoreClickListener l) { this.onMoreClick = l; }

    // ── Cập nhật dữ liệu ─────────────────────────────────
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

    // ── RecyclerView ─────────────────────────────────────
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
        Context ctx = holder.itemView.getContext();

        holder.tvName.setText(song.getName());
        holder.tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "");

        ImageFileHelper.loadIntoImageView(ctx, song.getImage(),
                holder.imgSong, R.drawable.ic_music_note);

        holder.itemView.setOnClickListener(v -> {
            if (onSongClick != null) onSongClick.onSongClick(song);
        });

        if (holder.btnLike != null) {
            String userId = new com.ptithcm.waveapp.util.TokenManager(ctx).getUserId();
            boolean isLiked = false;
            if (userId != null) {
                isLiked = com.ptithcm.waveapp.ServiceLocator.getInstance().likedSongRepository.existsByUserIdAndSongId(userId, song.getId());
            }
            holder.btnLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            // Remove manual color filter as the drawable itself has the colors
            holder.btnLike.setColorFilter(null);

            holder.btnLike.setOnClickListener(v -> {
                if (onLikeClick != null) onLikeClick.onLikeClick(song, position);
            });
        }

        if (holder.btnMore != null) {
            holder.btnMore.setOnClickListener(v -> {
                if (onMoreClick != null) onMoreClick.onMoreClick(song, position);
            });
        }
    }

    @Override
    public int getItemCount() { return songs.size(); }

    // ── ViewHolder ────────────────────────────────────────
    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView  tvName, tvArtist;
        ImageView btnLike, btnMore;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong  = itemView.findViewById(R.id.imgSong);
            tvName   = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtistName);
            btnLike  = itemView.findViewById(R.id.btnLike);   // null nếu layout không có
            btnMore  = itemView.findViewById(R.id.btnMore);   // null nếu layout không có
        }
    }
}