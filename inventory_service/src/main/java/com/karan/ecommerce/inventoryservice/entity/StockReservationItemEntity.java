package com.karan.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_reservation_items")
public class StockReservationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private StockReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_balance_id", nullable = false)
    private StockBalanceEntity stockBalance;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(name = "warehouse_code", nullable = false, length = 60)
    private String warehouseCode;

    @Column(nullable = false)
    private Integer quantity;

    @PrePersist
    @PreUpdate
    public void normalizeFields() {
        if (this.sku != null) this.sku = this.sku.trim().toUpperCase();
        if (this.warehouseCode != null) this.warehouseCode = this.warehouseCode.trim().toUpperCase();
    }

    public Long getId() { return id; }
    public StockReservationEntity getReservation() { return reservation; }
    public void setReservation(StockReservationEntity reservation) { this.reservation = reservation; }
    public StockBalanceEntity getStockBalance() { return stockBalance; }
    public void setStockBalance(StockBalanceEntity stockBalance) { this.stockBalance = stockBalance; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
