package com.example.project.dto.dish;

import javax.validation.constraints.NotNull;

public class DishDTO {

    private String id;
    private @NotNull String searchId;
    private @NotNull String nameEn;
    private @NotNull String nameUa;
    private @NotNull String imageData;
    private @NotNull double price;
    private @NotNull String descriptionEn;
    private @NotNull String descriptionUa;
    private @NotNull String categoryId;
    private @NotNull double minSales;
    private @NotNull double maxSales;
    private @NotNull double costPrice;

    public DishDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameUa() {
        return nameUa;
    }

    public void setNameUa(String nameUa) {
        this.nameUa = nameUa;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionUa() {
        return descriptionUa;
    }

    public void setDescriptionUa(String descriptionUa) {
        this.descriptionUa = descriptionUa;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getMinSales() {
        return minSales;
    }

    public void setMinSales(double minSales) {
        this.minSales = minSales;
    }

    public double getMaxSales() {
        return maxSales;
    }

    public void setMaxSales(double maxSales) {
        this.maxSales = maxSales;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }
}
