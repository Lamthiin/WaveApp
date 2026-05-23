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

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {

    public interface OnGenreClickListener {
        void onGenreClick(Genre genre);
    }

    private List<Genre> genres = new ArrayList<>();
    private OnGenreClickListener onGenreClick;

    public void setOnGenreClickListener(OnGenreClickListener listener) {
        this.onGenreClick = listener;
    }

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
        Context context = holder.itemView.getContext();

        if (holder.tvName != null) {
            holder.tvName.setText(genre.getName());
        }

        if (holder.tvDescription != null) {
            holder.tvDescription.setText(genre.getDescription());
        }

        if (holder.imgGenre != null) {
            ImageFileHelper.loadIntoImageView(
                    context,
                    genre.getImageUrl(),
                    holder.imgGenre,
                    R.drawable.ic_logo
            );
        }

        holder.itemView.setOnClickListener(v -> {
            if (onGenreClick != null) {
                onGenreClick.onGenreClick(genre);
            }
        });
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    static class GenreViewHolder extends RecyclerView.ViewHolder {

        ImageView imgGenre;
        TextView tvName;
        TextView tvDescription;

        GenreViewHolder(@NonNull View itemView) {
            super(itemView);

            imgGenre = itemView.findViewById(R.id.imgGenre);
            if (imgGenre == null) {
                imgGenre = itemView.findViewById(R.id.img_category);
            }

            tvName = itemView.findViewById(R.id.tvGenreName);
            if (tvName == null) {
                tvName = itemView.findViewById(R.id.tv_category_name);
            }

            tvDescription = itemView.findViewById(R.id.tvGenreDesc);
        }
    }
}