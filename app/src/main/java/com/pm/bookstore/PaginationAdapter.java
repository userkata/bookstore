package com.pm.bookstore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.pm.bookstore.models.Book;
import com.pm.bookstore.utils.PaginationAdapterCallback;

import java.util.ArrayList;
import java.util.List;

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    public static final String BOOK_ID = "book_id";
    public static final String BOOK_TITLE = "book_title";
    public static final String BOOK_AUTHOR = "book_author";
    public static final String BOOK_DESCRIPTION = "book_description";
    public static final String BOOK_IMAGE = "book_image";
    public static final String BOOK_URL = "book_url";

    private final List<Book> bookResults;
    final private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private final PaginationAdapterCallback mCallback;

    private String errorMsg;

    PaginationAdapter(Context context) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) context;
        bookResults = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.book, parent, false);
                viewHolder = new BookVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }
        assert viewHolder != null;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Book result = bookResults.get(position);

        switch (getItemViewType(position)) {

            case ITEM:
                final BookVH bookVH = (BookVH) holder;

                loadImage(getBookImage(result))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                bookVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                bookVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(bookVH.mThumbImg);

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, DetailActivity.class);

                    Bundle b = new Bundle();
                    b.putString(BOOK_ID, result.getId());
                    b.putString(BOOK_TITLE, result.getVolumeInfo().getTitle());
                    b.putString(BOOK_AUTHOR, formatAuthorLabel(result));
                    b.putString(BOOK_DESCRIPTION, result.getVolumeInfo().getDescription());
                    b.putString(BOOK_IMAGE, getBookImage(result));
                    b.putString(BOOK_URL, result.getSaleInfo().getBuyLink());
                    intent.putExtras(b);

                    context.startActivity(intent);
                });
                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == bookResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    private String getBookImage(Book result) {
        if (result.getVolumeInfo().getImageLinks() != null &&
                result.getVolumeInfo().getImageLinks().getSmallThumbnail() != null){
            return result.getVolumeInfo().getImageLinks().getSmallThumbnail();
        } else {
            return context.getResources().getString(R.string.image_placehorder);
        }
    }

    private String formatAuthorLabel(Book result) {
        return result.getVolumeInfo().getAuthors() == null ? "" :
                String.join(", ", result.getVolumeInfo().getAuthors());
    }

    private RequestBuilder<Drawable> loadImage(@NonNull String posterPath) {
        return Glide
                .with(context)
                .load(posterPath)
                .centerCrop();
    }

    public void add(Book r) {
        bookResults.add(r);
        notifyItemInserted(bookResults.size() - 1);
    }

    public void addAll(List<Book> bookResults) {
        for (Book result : bookResults) {
            add(result);
        }
    }

    public void remove(Book r) {
        int position = bookResults.indexOf(r);
        if (position > -1) {
            bookResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clearAdapter() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Book());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = bookResults.size() - 1;
        Book result = getItem(position);

        if (result != null) {
            bookResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Book getItem(int position) {
        return bookResults.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(bookResults.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    private static class BookVH extends RecyclerView.ViewHolder {
        private final ImageView mThumbImg;
        private final ProgressBar mProgress;

        public BookVH(View itemView) {
            super(itemView);

            mThumbImg = itemView.findViewById(R.id.book_thumb);
            mProgress = itemView.findViewById(R.id.book_progress);
        }
    }

    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ProgressBar mProgressBar;
        private final TextView mErrorTxt;
        private final LinearLayout mErrorLayout;

        public LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            ImageButton mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }
}