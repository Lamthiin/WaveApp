package com.ptithcm.waveapp.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.model.Genre;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Dùng cho:
 *   - AllCategoriesActivity  (lưới 2 cột)
 *   - HomeFragment           (Khám phá các thể loại)
 *   - fragment_search        (tabGenres)
 */
public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {

    public interface OnGenreClickListener { void onGenreClick(Genre genre); }

    private List<Genre> genres;
    private OnGenreClickListener onGenreClick;

    public GenreAdapter() { this.genres = new ArrayList<>(); }

    public void setOnGenreClickListener(OnGenreClickListener l) { this.onGenreClick = l; }

    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? genres : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_genre, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Genre genre = genres.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvName.setText(genre.getName());
        if (holder.tvDescription != null)
            holder.tvDescription.setText(genre.getDescription());

        ImageFileHelper.loadIntoImageView(ctx, genre.getImageUrl(),
                holder.imgGenre, R.drawable.ic_music_note);

        holder.itemView.setOnClickListener(v -> {
            if (onGenreClick != null) onGenreClick.onGenreClick(genre);
        });
    }

    @Override
    public int getItemCount() { return genres.size(); }

    static class GenreViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGenre;
        TextView  tvName, tvDescription;

        GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGenre      = itemView.findViewById(R.id.imgGenre);
            tvName        = itemView.findViewById(R.id.tvGenreName);
            tvDescription = itemView.findViewById(R.id.tvGenreDesc);
        }
    }
}
