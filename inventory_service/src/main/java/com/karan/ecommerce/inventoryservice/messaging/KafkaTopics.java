package com.karan.ecommerce.inventoryservice.messaging;

public final class KafkaTopics {
    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_CANCELLED = "OrderCancelled";
    public static final String STOCK_RESERVED = "StockReserved";
    public static final String STOCK_RESERVATION_FAILED = "StockReservationFailed";
}
