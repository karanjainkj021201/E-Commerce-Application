package com.karan.ecommerce.shippingservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karan.ecommerce.shippingservice.event.OrderCancelledEvent;
import com.karan.ecommerce.shippingservice.event.OrderConfirmedEvent;
import com.karan.ecommerce.shippingservice.service.ShipmentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentEventListener {

    private final ObjectMapper objectMapper;
    private final ShipmentService shipmentService;

    public ShipmentEventListener(ObjectMapper objectMapper, ShipmentService shipmentService) {
        this.objectMapper = objectMapper;
        this.shipmentService = shipmentService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CONFIRMED, groupId = "shipping-service")
    public void onOrderConfirmed(String payload) throws JsonProcessingException {
        OrderConfirmedEvent event = objectMapper.readValue(payload, OrderConfirmedEvent.class);
        shipmentService.createShipmentFromOrder(event);
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "shipping-service")
    public void onOrderCancelled(String payload) throws JsonProcessingException {
        OrderCancelledEvent event = objectMapper.readValue(payload, OrderCancelledEvent.class);
        shipmentService.cancelShipmentFromOrder(event);
    }
}
