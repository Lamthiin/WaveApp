package com.ptithcm.waveapp.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.model.Playlist;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Dùng cho:
 *   - MyPlaylistsActivity  (danh sách playlist của tôi)
 *   - fragment_library     (tabCustomPlaylists)
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    public interface OnPlaylistClickListener  { void onPlaylistClick(Playlist playlist); }
    public interface OnDeleteClickListener    { void onDeleteClick(Playlist playlist, int position); }

    private List<Playlist> playlists;
    private OnPlaylistClickListener  onPlaylistClick;
    private OnDeleteClickListener    onDeleteClick;

    public PlaylistAdapter() { this.playlists = new ArrayList<>(); }

    public void setOnPlaylistClickListener(OnPlaylistClickListener l)  { this.onPlaylistClick = l; }
    public void setOnDeleteClickListener(OnDeleteClickListener l)      { this.onDeleteClick   = l; }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists != null ? playlists : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        playlists.remove(position);
        notifyItemRemoved(position);
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
        Context ctx = holder.itemView.getContext();

        holder.tvName.setText(playlist.getName());

        ImageFileHelper.loadIntoImageView(ctx, playlist.getImage(),
                holder.imgPlaylist, R.drawable.ic_playlist);

        holder.itemView.setOnClickListener(v -> {
            if (onPlaylistClick != null) onPlaylistClick.onPlaylistClick(playlist);
        });

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (onDeleteClick != null) onDeleteClick.onDeleteClick(playlist, position);
            });
        }
    }

    @Override
    public int getItemCount() { return playlists.size(); }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlaylist;
        TextView  tvName;
        ImageView btnDelete;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlaylist = itemView.findViewById(R.id.imgPlaylist);
            tvName      = itemView.findViewById(R.id.tvPlaylistName);
            btnDelete   = itemView.findViewById(R.id.btnDelete);
        }
    }
}
