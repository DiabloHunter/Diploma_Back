package com.example.project.dto.order.response;

import javax.validation.constraints.NotNull;

public class OrderDishDTO {
    private @NotNull Long dishId;
    private @NotNull String name;
    private @NotNull String searchId;
    private @NotNull String imageURL;
    private @NotNull Double price;
    private @NotNull String description;
    private @NotNull Double quantity;
    private @NotNull Long categoryId;

    public OrderDishDTO(Long dishId, String name, String searchId, String imageURL, Double price, String description,
                        Double quantity, Long categoryId) {
        this.dishId = dishId;
        this.name = name;
        this.searchId = searchId;
        this.imageURL = imageURL;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
        this.categoryId = categoryId;
    }

    public OrderDishDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }
}
