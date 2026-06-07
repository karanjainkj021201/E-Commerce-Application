package com.karan.ecommerce.inventoryservice.dto;

import com.karan.ecommerce.inventoryservice.entity.enums.LedgerMovementType;

import java.time.LocalDateTime;

public class LedgerResponse {
    private Long id;
    private Long stockBalanceId;
    private Long productId;
    private String sku;
    private String warehouseCode;
    private LedgerMovementType movementType;
    private Integer quantityChange;
    private Integer totalQuantityAfter;
    private Integer reservedQuantityAfter;
    private Integer availableQuantityAfter;
    private String referenceType;
    private String referenceId;
    private String reason;
    private LocalDateTime createdAt;

    public LedgerResponse(Long id, Long stockBalanceId, Long productId, String sku, String warehouseCode,
                          LedgerMovementType movementType, Integer quantityChange, Integer totalQuantityAfter,
                          Integer reservedQuantityAfter, Integer availableQuantityAfter, String referenceType,
                          String referenceId, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.stockBalanceId = stockBalanceId;
        this.productId = productId;
        this.sku = sku;
        this.warehouseCode = warehouseCode;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.totalQuantityAfter = totalQuantityAfter;
        this.reservedQuantityAfter = reservedQuantityAfter;
        this.availableQuantityAfter = availableQuantityAfter;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getStockBalanceId() { return stockBalanceId; }
    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getWarehouseCode() { return warehouseCode; }
    public LedgerMovementType getMovementType() { return movementType; }
    public Integer getQuantityChange() { return quantityChange; }
    public Integer getTotalQuantityAfter() { return totalQuantityAfter; }
    public Integer getReservedQuantityAfter() { return reservedQuantityAfter; }
    public Integer getAvailableQuantityAfter() { return availableQuantityAfter; }
    public String getReferenceType() { return referenceType; }
    public String getReferenceId() { return referenceId; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
