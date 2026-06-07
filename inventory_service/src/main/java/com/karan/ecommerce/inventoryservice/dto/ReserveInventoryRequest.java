package com.karan.ecommerce.inventoryservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ReserveInventoryRequest {

    @NotNull(message = "Order id is required")
    private Long orderId;

    @Size(max = 80)
    private String orderNumber;

    @Size(max = 60)
    private String warehouseCode;

    @NotEmpty(message = "Reservation must contain at least one item")
    @Valid
    private List<ReserveInventoryItemRequest> items;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public List<ReserveInventoryItemRequest> getItems() { return items; }
    public void setItems(List<ReserveInventoryItemRequest> items) { this.items = items; }
}
