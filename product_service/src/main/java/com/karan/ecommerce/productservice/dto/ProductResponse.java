package com.karan.ecommerce.productservice.dto;

import com.karan.ecommerce.productservice.entity.enums.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String imageUrl;
    private ProductStatus status;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse(Long id, String sku, String name, String description, BigDecimal price, String currency, String imageUrl, ProductStatus status, CategoryResponse category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.imageUrl = imageUrl;
        this.status = status;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public String getImageUrl() { return imageUrl; }
    public ProductStatus getStatus() { return status; }
    public CategoryResponse getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
