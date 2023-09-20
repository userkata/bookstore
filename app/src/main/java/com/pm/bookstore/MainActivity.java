package com.pm.bookstore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pm.bookstore.api.BookApi;
import com.pm.bookstore.api.BookService;
import com.pm.bookstore.models.BookList;
import com.pm.bookstore.models.Book;
import com.pm.bookstore.utils.PaginationAdapterCallback;
import com.pm.bookstore.utils.PaginationScrollListener;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements PaginationAdapterCallback {

    private static final String TAG = "MainActivity";

    PaginationAdapter adapter;
    GridLayoutManager gridLayoutManager;

    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final int PAGE_START = 0;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int TOTAL_PAGES = 10000;
    private static final String MAX_RESULTS = "20";
    private int currentPage = PAGE_START;
    private BookService bookService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.main_recycler);
        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);
        btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = findViewById(R.id.main_swiperefresh);

        adapter = new PaginationAdapter(this);

        gridLayoutManager = new GridLayoutManager(this, 2);
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);

        rv.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 20;

                loadNextPage();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        bookService = BookApi.getClient(this).create(BookService.class);
        loadFirstPage();
        btnRetry.setOnClickListener(view -> loadFirstPage());
        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            swipeRefreshLayout.setRefreshing(true);
            doRefresh();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callBookListApi().isExecuted())
            callBookListApi().cancel();

        adapter.clearAdapter();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
        isLastPage = false;
    }

    private void loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ");

        hideErrorView();
        currentPage = PAGE_START;

        callBookListApi().enqueue(new Callback<BookList>() {
            @Override
            public void onResponse(@NonNull Call<BookList> call, @NonNull Response<BookList> response) {
                hideErrorView();

                List<Book> results = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                adapter.addAll(results);

                if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(@NonNull Call<BookList> call, @NonNull Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }

    private List<Book> fetchResults(Response<BookList> response) {
        BookList bookList = response.body();
        assert bookList != null;
        return bookList.getResults();
    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callBookListApi().enqueue(new Callback<BookList>() {
            @Override
            public void onResponse(@NonNull Call<BookList> call, @NonNull Response<BookList> response) {

                adapter.removeLoadingFooter();
                isLoading = false;

                List<Book> results = fetchResults(response);
                adapter.addAll(results);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(@NonNull Call<BookList> call, @NonNull Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    private Call<BookList> callBookListApi() {
        return bookService.get(
                getString(R.string.query),
                MAX_RESULTS,
                currentPage
        );
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }

    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}