package com.karan.ecommerce.orderservice.dto;

import com.karan.ecommerce.orderservice.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminOrderStatusUpdateRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    @Size(max = 1000)
    private String reason;

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
