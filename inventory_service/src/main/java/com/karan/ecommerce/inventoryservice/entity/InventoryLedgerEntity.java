package com.karan.ecommerce.inventoryservice.entity;

import com.karan.ecommerce.inventoryservice.entity.enums.LedgerMovementType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_ledger")
public class InventoryLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_balance_id", nullable = false)
    private Long stockBalanceId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(name = "warehouse_code", nullable = false, length = 60)
    private String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private LedgerMovementType movementType;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "total_quantity_after", nullable = false)
    private Integer totalQuantityAfter;

    @Column(name = "reserved_quantity_after", nullable = false)
    private Integer reservedQuantityAfter;

    @Column(name = "available_quantity_after", nullable = false)
    private Integer availableQuantityAfter;

    @Column(name = "reference_type", length = 60)
    private String referenceType;

    @Column(name = "reference_id", length = 120)
    private String referenceId;

    @Column(length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.sku != null) this.sku = this.sku.trim().toUpperCase();
        if (this.warehouseCode != null) this.warehouseCode = this.warehouseCode.trim().toUpperCase();
    }

    public Long getId() { return id; }
    public Long getStockBalanceId() { return stockBalanceId; }
    public void setStockBalanceId(Long stockBalanceId) { this.stockBalanceId = stockBalanceId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public LedgerMovementType getMovementType() { return movementType; }
    public void setMovementType(LedgerMovementType movementType) { this.movementType = movementType; }
    public Integer getQuantityChange() { return quantityChange; }
    public void setQuantityChange(Integer quantityChange) { this.quantityChange = quantityChange; }
    public Integer getTotalQuantityAfter() { return totalQuantityAfter; }
    public void setTotalQuantityAfter(Integer totalQuantityAfter) { this.totalQuantityAfter = totalQuantityAfter; }
    public Integer getReservedQuantityAfter() { return reservedQuantityAfter; }
    public void setReservedQuantityAfter(Integer reservedQuantityAfter) { this.reservedQuantityAfter = reservedQuantityAfter; }
    public Integer getAvailableQuantityAfter() { return availableQuantityAfter; }
    public void setAvailableQuantityAfter(Integer availableQuantityAfter) { this.availableQuantityAfter = availableQuantityAfter; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
