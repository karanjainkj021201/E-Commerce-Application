package com.karan.ecommerce.inventoryservice.dto;

public class InventoryAvailabilityResponse {
    private Long productId;
    private String sku;
    private String warehouseCode;
    private Integer availableQuantity;
    private boolean available;

    public InventoryAvailabilityResponse(Long productId, String sku, String warehouseCode, Integer availableQuantity, boolean available) {
        this.productId = productId;
        this.sku = sku;
        this.warehouseCode = warehouseCode;
        this.availableQuantity = availableQuantity;
        this.available = available;
    }

    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getWarehouseCode() { return warehouseCode; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public boolean isAvailable() { return available; }
}
