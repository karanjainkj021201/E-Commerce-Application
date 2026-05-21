package com.karan.ecommerce.orderservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karan.ecommerce.orderservice.event.*;
import com.karan.ecommerce.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public OrderEventListener(ObjectMapper objectMapper, OrderService orderService) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_SUCCEEDED, groupId = "order-service")
    public void onPaymentSucceeded(String payload) throws JsonProcessingException {
        PaymentSucceededEvent event = objectMapper.readValue(payload, PaymentSucceededEvent.class);
        orderService.markPaymentSucceeded(event.getOrderId(), event.getPaymentReference(), event.getGatewayReference());
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "order-service")
    public void onPaymentFailed(String payload) throws JsonProcessingException {
        PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
        orderService.markPaymentFailed(event.getOrderId(), event.getFailureReason());
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RESERVED, groupId = "order-service")
    public void onStockReserved(String payload) throws JsonProcessingException {
        StockReservedEvent event = objectMapper.readValue(payload, StockReservedEvent.class);
        orderService.markStockReserved(event.getOrderId(), event.getReservationId());
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RESERVATION_FAILED, groupId = "order-service")
    public void onStockReservationFailed(String payload) throws JsonProcessingException {
        StockReservationFailedEvent event = objectMapper.readValue(payload, StockReservationFailedEvent.class);
        orderService.markStockReservationFailed(event.getOrderId(), event.getFailureReason());
    }

    @KafkaListener(topics = KafkaTopics.SHIPMENT_CREATED, groupId = "order-service")
    public void onShipmentCreated(String payload) throws JsonProcessingException {
        ShipmentCreatedEvent event = objectMapper.readValue(payload, ShipmentCreatedEvent.class);
        orderService.markShipmentCreated(event.getOrderId(), event.getShipmentId(), event.getCarrier(), event.getTrackingNumber());
    }

    @KafkaListener(topics = KafkaTopics.SHIPMENT_DELIVERED, groupId = "order-service")
    public void onShipmentDelivered(String payload) throws JsonProcessingException {
        ShipmentDeliveredEvent event = objectMapper.readValue(payload, ShipmentDeliveredEvent.class);
        orderService.markShipmentDelivered(event.getOrderId(), event.getShipmentId(), event.getTrackingNumber());
    }
}
