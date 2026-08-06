package com.karan.ecommerce.shippingservice.event;

public class ShipmentDeliveredEvent {
    private Long orderId;
    private String shipmentId;
    private String trackingNumber;

    public ShipmentDeliveredEvent() {}

    public ShipmentDeliveredEvent(Long orderId, String shipmentId, String trackingNumber) {
        this.orderId = orderId;
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
}
