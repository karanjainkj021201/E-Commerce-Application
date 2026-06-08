package com.karan.ecommerce.paymentservice.messaging;

public final class KafkaTopics {
    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "OrderCreated";
    public static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    public static final String PAYMENT_FAILED = "PaymentFailed";
}
