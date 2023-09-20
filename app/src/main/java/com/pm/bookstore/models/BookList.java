package com.pm.bookstore.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BookList {

    @SerializedName("kind")
    @Expose
    private String kind;
    @SerializedName("items")
    @Expose
    private List<Book> results = new ArrayList<Book>();
    @SerializedName("totalItems")
    @Expose
    private Integer totalItems;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<Book> getResults() {
        return results;
    }

    public void setResults(List<Book> results) {
        this.results = results;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
}