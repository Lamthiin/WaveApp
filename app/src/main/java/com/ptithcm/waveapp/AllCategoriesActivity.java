package com.ptithcm.waveapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ptithcm.waveapp.adapter.GenreAdapter;
import com.ptithcm.waveapp.model.Genre;
import com.ptithcm.waveapp.service.CategoryService;
import java.util.List;

public class AllCategoriesActivity extends AppCompatActivity {

    private CategoryService categoryService;
    private GenreAdapter genreAdapter;
    private RecyclerView rvCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_categories);

        categoryService = ServiceLocator.getInstance().getCategoryService();

        rvCategories = findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2)); // Lưới 2 cột

        genreAdapter = new GenreAdapter();
        rvCategories.setAdapter(genreAdapter);

        loadCategories();

        genreAdapter.setOnGenreClickListener(genre -> {
            Intent intent = new Intent(this, SongsByCategoryActivity.class);
            intent.putExtra("GENRE_ID", genre.getId());
            intent.putExtra("GENRE_NAME", genre.getName());
            startActivity(intent);
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadCategories() {
        List<Genre> categories = categoryService.getAllCategories();
        genreAdapter.setGenres(categories);
    }
}
