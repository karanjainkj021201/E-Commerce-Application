package com.karan.ecommerce.orderservice.event;

public class StockReservedEvent {
    private Long orderId;
    private String reservationId;
    private String message;

    public StockReservedEvent() {
    }

    public StockReservedEvent(Long orderId, String reservationId, String message) {
        this.orderId = orderId;
        this.reservationId = reservationId;
        this.message = message;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
