package com.karan.ecommerce.inventoryservice.dto;

public class ReservationItemResponse {
    private Long id;
    private Long stockBalanceId;
    private Long productId;
    private String sku;
    private String warehouseCode;
    private Integer quantity;

    public ReservationItemResponse(Long id, Long stockBalanceId, Long productId, String sku, String warehouseCode, Integer quantity) {
        this.id = id;
        this.stockBalanceId = stockBalanceId;
        this.productId = productId;
        this.sku = sku;
        this.warehouseCode = warehouseCode;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getStockBalanceId() { return stockBalanceId; }
    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getWarehouseCode() { return warehouseCode; }
    public Integer getQuantity() { return quantity; }
}
