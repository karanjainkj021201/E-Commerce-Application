package com.karan.ecommerce.productservice.dto;

import java.math.BigDecimal;

public class ProductSnapshotResponse {

    private Long productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private String currency;
    private boolean availableForOrder;

    public ProductSnapshotResponse() {
    }

    public ProductSnapshotResponse(Long productId, String sku, String name,
                                   BigDecimal price, String currency,
                                   boolean availableForOrder) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.availableForOrder = availableForOrder;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isAvailableForOrder() {
        return availableForOrder;
    }
}
