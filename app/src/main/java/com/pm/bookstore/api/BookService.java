package com.pm.bookstore.api;

import com.pm.bookstore.models.BookList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BookService {

    @GET("volumes")
    Call<BookList> get(
            @Query("q") String query,
            @Query("maxResults") String maxResults,
            @Query("startIndex") int startIndex
    );
}
