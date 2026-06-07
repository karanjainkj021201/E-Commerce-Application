package com.karan.ecommerce.inventoryservice.event;

public class StockReservationFailedEvent {
    private Long orderId;
    private String failureReason;

    public StockReservationFailedEvent() {
    }

    public StockReservationFailedEvent(Long orderId, String failureReason) {
        this.orderId = orderId;
        this.failureReason = failureReason;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
