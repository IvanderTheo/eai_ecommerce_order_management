package com.example.order_management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductRequest {

    @NotBlank(message = "Nama produk wajib diisi")
    @Size(min = 3, message = "Nama produk minimal 3 karakter")
    private String name;

    @NotNull(message = "Harga wajib diisi")
    @Min(value = 1, message = "Harga minimal 1")
    private Double price;

    @NotNull(message = "Stok wajib diisi")
    @Min(value = 0, message = "Stok tidak boleh negatif")
    private Integer stock;

    private String sku;

    private Long categoryId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
