package com.karan.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_balances",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_product_warehouse", columnNames = {"product_id", "warehouse_code"})
)
public class StockBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "warehouse_code", nullable = false, length = 60)
    private String warehouseCode;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        normalizeFields();
        if (this.totalQuantity == null) this.totalQuantity = 0;
        if (this.reservedQuantity == null) this.reservedQuantity = 0;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        normalizeFields();
    }

    private void normalizeFields() {
        if (this.sku != null) this.sku = this.sku.trim().toUpperCase();
        if (this.warehouseCode != null) this.warehouseCode = this.warehouseCode.trim().toUpperCase();
        if (this.productName != null) this.productName = this.productName.trim();
    }

    public Integer getAvailableQuantity() {
        int total = totalQuantity == null ? 0 : totalQuantity;
        int reserved = reservedQuantity == null ? 0 : reservedQuantity;
        return total - reserved;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku == null ? null : sku.trim().toUpperCase(); }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode == null ? null : warehouseCode.trim().toUpperCase(); }
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
