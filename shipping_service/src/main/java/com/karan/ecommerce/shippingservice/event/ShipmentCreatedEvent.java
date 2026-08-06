package com.karan.ecommerce.shippingservice.event;

public class ShipmentCreatedEvent {
    private Long orderId;
    private String shipmentId;
    private String carrier;
    private String trackingNumber;

    public ShipmentCreatedEvent() {}

    public ShipmentCreatedEvent(Long orderId, String shipmentId, String carrier, String trackingNumber) {
        this.orderId = orderId;
        this.shipmentId = shipmentId;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
}
