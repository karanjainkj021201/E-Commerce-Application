package com.karan.ecommerce.inventoryservice.dto;

import java.time.LocalDateTime;

public class StockBalanceResponse {
    private Long id;
    private Long productId;
    private String sku;
    private String productName;
    private String warehouseCode;
    private Integer totalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StockBalanceResponse(Long id, Long productId, String sku, String productName, String warehouseCode,
                                Integer totalQuantity, Integer reservedQuantity, Integer availableQuantity,
                                boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.warehouseCode = warehouseCode;
        this.totalQuantity = totalQuantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public String getWarehouseCode() { return warehouseCode; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
