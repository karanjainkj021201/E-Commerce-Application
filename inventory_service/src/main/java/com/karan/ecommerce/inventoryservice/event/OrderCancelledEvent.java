package com.karan.ecommerce.inventoryservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderCancelledEvent {
    private Long orderId;
    private String orderNumber;
    private String keycloakUserId;
    private BigDecimal totalAmount;
    private String currency;
    private String reason;
    private List<OrderItemEvent> items;
    private LocalDateTime occurredAt;

    public OrderCancelledEvent() {
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getKeycloakUserId() { return keycloakUserId; }
    public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<OrderItemEvent> getItems() { return items; }
    public void setItems(List<OrderItemEvent> items) { this.items = items; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
