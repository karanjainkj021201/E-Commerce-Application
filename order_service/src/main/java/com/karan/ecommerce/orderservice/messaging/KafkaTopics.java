package com.karan.ecommerce.orderservice.messaging;

public final class KafkaTopics {
    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "OrderCreated";
    public static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    public static final String PAYMENT_FAILED = "PaymentFailed";
    public static final String STOCK_RESERVED = "StockReserved";
    public static final String STOCK_RESERVATION_FAILED = "StockReservationFailed";
    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String SHIPMENT_CREATED = "ShipmentCreated";
    public static final String SHIPMENT_DELIVERED = "ShipmentDelivered";
    public static final String ORDER_CANCELLED = "OrderCancelled";
}
