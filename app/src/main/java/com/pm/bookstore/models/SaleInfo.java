package com.pm.bookstore.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaleInfo {

    @SerializedName("saleability")
    @Expose
    private String saleability;
    @SerializedName("buyLink")
    @Expose
    private String buyLink;

    public String getSaleability() {
        return saleability;
    }

    public void setSaleability(String saleability) {
        this.saleability = saleability;
    }

    public String getBuyLink() {
        return buyLink;
    }

    public void setBuyLink(String buyLink) {
        this.buyLink = buyLink;
    }
}