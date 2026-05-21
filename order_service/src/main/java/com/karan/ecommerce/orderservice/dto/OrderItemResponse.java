package com.karan.ecommerce.orderservice.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String sku;
    private String productNameSnapshot;
    private BigDecimal unitPriceSnapshot;
    private String currency;
    private Integer quantity;
    private BigDecimal lineTotal;

    public OrderItemResponse(Long id, Long productId, String sku, String productNameSnapshot,
                             BigDecimal unitPriceSnapshot, String currency, Integer quantity, BigDecimal lineTotal) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.productNameSnapshot = productNameSnapshot;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.currency = currency;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public BigDecimal getUnitPriceSnapshot() { return unitPriceSnapshot; }
    public String getCurrency() { return currency; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
