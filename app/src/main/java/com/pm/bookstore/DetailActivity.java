package com.pm.bookstore;

import static com.pm.bookstore.PaginationAdapter.BOOK_AUTHOR;
import static com.pm.bookstore.PaginationAdapter.BOOK_DESCRIPTION;
import static com.pm.bookstore.PaginationAdapter.BOOK_IMAGE;
import static com.pm.bookstore.PaginationAdapter.BOOK_TITLE;
import static com.pm.bookstore.PaginationAdapter.BOOK_URL;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pm.bookstore.databinding.ActivityDetailBinding;

import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private String url = "";
    private String title = "";
    private String author = "";
    private String description = "";
    private String image = "";
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDetailBinding binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        if (b != null) {
            title = b.getString(BOOK_TITLE, "");
            author = b.getString(BOOK_AUTHOR, "");
            description = b.getString(BOOK_DESCRIPTION, "");
            image = b.getString(BOOK_IMAGE, "");
            url = b.getString(BOOK_URL, "");
        }

        binding.fab.setOnClickListener(view -> {
            isFavorite=!isFavorite;
            updateFab(isFavorite, binding.fab);
        });

        binding.bookBuy.setVisibility(url.isEmpty() ? View.GONE : View.VISIBLE);

        binding.bookBuy.setOnClickListener(view ->  openBrowser(url));

        binding.bookTitle.setText(title);
        binding.bookAuthor.setText(author);
        binding.bookDescription.setText(description);

        Glide.with(this)
                .load(image)
                .centerCrop()
                .into(binding.bookImg);

    }

    private void updateFab(boolean isFavorite, FloatingActionButton fab) {
            if (isFavorite) {
                fab.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.baseline_favorite_24));
            } else {
                fab.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.baseline_favorite_border_24));
            }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openBrowser(String url) {
        Uri uri = Uri.parse("googlechrome://navigate?url=" + url);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        if (i.resolveActivity(getPackageManager()) == null) {
            i.setData(Uri.parse(url));
        }
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}