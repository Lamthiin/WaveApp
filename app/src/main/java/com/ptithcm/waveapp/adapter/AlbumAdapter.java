package com.ptithcm.waveapp.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.model.Album;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Dùng cho:
 *   - fragment_library     (tabAlbums - Album yêu thích)
 *   - fragment_search      (tabAlbums)
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    public enum LayoutMode { GRID, LIST }

    public interface OnAlbumClickListener { void onAlbumClick(Album album); }
    public interface OnLikeClickListener  { void onLikeClick(Album album, int position); }

    private List<Album> albums;
    private OnAlbumClickListener onAlbumClick;
    private OnLikeClickListener  onLikeClick;
    private LayoutMode layoutMode = LayoutMode.GRID;

    public AlbumAdapter() { this.albums = new ArrayList<>(); }

    public void setOnAlbumClickListener(OnAlbumClickListener l) { this.onAlbumClick = l; }
    public void setOnLikeClickListener(OnLikeClickListener l)   { this.onLikeClick  = l; }
    public void setLayoutMode(LayoutMode mode) {
        this.layoutMode = mode != null ? mode : LayoutMode.GRID;
        notifyDataSetChanged();
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums != null ? albums : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutMode == LayoutMode.LIST ? R.layout.item_album_list : R.layout.item_album_grid, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvAlbumName.setText(album.getName());
        if (holder.tvArtistName != null && album.getArtist() != null)
            holder.tvArtistName.setText("Album • " + album.getArtist().getName());

        // Load ảnh bìa album từ file path
        ImageFileHelper.loadIntoImageView(ctx, album.getImage(),
                holder.imgAlbum, R.drawable.ic_music_note);

        holder.itemView.setOnClickListener(v -> {
            if (onAlbumClick != null) onAlbumClick.onAlbumClick(album);
        });

        if (holder.btnLike != null) {
            String userId = new com.ptithcm.waveapp.util.TokenManager(ctx).getUserId();
            boolean isLiked = false;
            if (userId != null) {
                isLiked = com.ptithcm.waveapp.ServiceLocator.getInstance().likedAlbumRepository.existsByUserIdAndAlbumId(userId, album.getId());
            }
            holder.btnLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            holder.btnLike.setColorFilter(isLiked ? android.graphics.Color.parseColor("#1DB954") : android.graphics.Color.WHITE);

            holder.btnLike.setOnClickListener(v -> {
                if (onLikeClick != null) onLikeClick.onLikeClick(album, position);
            });
        }
    }

    @Override
    public int getItemCount() { return albums.size(); }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAlbum, btnLike;
        TextView  tvAlbumName, tvArtistName;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbum    = itemView.findViewById(R.id.imgAlbum);
            tvAlbumName = itemView.findViewById(R.id.tvAlbumName);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            btnLike     = itemView.findViewById(R.id.btnLike);
        }
    }
}
