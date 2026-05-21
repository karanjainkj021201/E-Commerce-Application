package com.karan.ecommerce.orderservice.dto;

import java.math.BigDecimal;

public class ProductSnapshotResponse {

    private Long productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private String currency;
    private boolean availableForOrder;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public boolean isAvailableForOrder() { return availableForOrder; }
    public void setAvailableForOrder(boolean availableForOrder) { this.availableForOrder = availableForOrder; }
}
