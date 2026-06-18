package com.ptithcm.waveapp.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.model.Artist;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Dùng cho:
 *   - HomeFragment         (Nghệ sĩ phổ biến - cuộn ngang)
 *   - fragment_library     (tabArtists - Nghệ sĩ yêu thích)
 *   - fragment_search      (tabArtists)
 */
public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    public enum LayoutMode { GRID, LIST, SEARCH_GRID }

    public interface OnArtistClickListener  { void onArtistClick(Artist artist); }
    public interface OnFollowClickListener  { void onFollowClick(Artist artist, int position); }

    private List<Artist> artists;
    private OnArtistClickListener  onArtistClick;
    private OnFollowClickListener  onFollowClick;
    private LayoutMode layoutMode = LayoutMode.GRID;

    public ArtistAdapter() { this.artists = new ArrayList<>(); }

    public void setOnArtistClickListener(OnArtistClickListener l) { this.onArtistClick = l; }
    public void setOnFollowClickListener(OnFollowClickListener l)  { this.onFollowClick  = l; }
    public void setLayoutMode(LayoutMode mode) {
        this.layoutMode = mode != null ? mode : LayoutMode.GRID;
        notifyDataSetChanged();
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists != null ? artists : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        if (layoutMode == LayoutMode.LIST) {
            layoutRes = R.layout.item_search_artist;
        } else if (layoutMode == LayoutMode.SEARCH_GRID) {
            layoutRes = R.layout.item_search_artist_grid;
        } else {
            layoutRes = R.layout.item_artist;
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = artists.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvName.setText(artist.getName());

        // Load ảnh nghệ sĩ (tròn) từ file path
        ImageFileHelper.loadIntoImageView(ctx, artist.getImage(),
                holder.imgArtist, R.drawable.ic_avatar);

        holder.itemView.setOnClickListener(v -> {
            if (onArtistClick != null) onArtistClick.onArtistClick(artist);
        });

        if (holder.btnFollow != null) {
            holder.btnFollow.setVisibility(onFollowClick != null ? View.VISIBLE : View.GONE);
            String userId = new com.ptithcm.waveapp.util.TokenManager(ctx).getUserId();
            boolean isFollowing = false;
            if (userId != null) {
                isFollowing = com.ptithcm.waveapp.ServiceLocator.getInstance().userFollowArtistRepository.existsByUserIdAndArtistId(userId, artist.getId());
            }
            holder.btnFollow.setImageResource(isFollowing ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

            holder.btnFollow.setOnClickListener(v -> {
                if (onFollowClick != null) onFollowClick.onFollowClick(artist, position);
            });
        }
    }

    @Override
    public int getItemCount() { return artists.size(); }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgArtist;
        TextView  tvName;
        ImageView btnFollow;

        ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgArtist = itemView.findViewById(R.id.imgArtist);
            tvName    = itemView.findViewById(R.id.tvArtistName);
            btnFollow = itemView.findViewById(R.id.favoriteButton);
        }
    }
}
