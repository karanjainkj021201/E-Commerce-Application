package com.karan.ecommerce.shippingservice.messaging;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String ORDER_CANCELLED = "OrderCancelled";
    public static final String SHIPMENT_CREATED = "ShipmentCreated";
    public static final String SHIPMENT_DELIVERED = "ShipmentDelivered";
}
