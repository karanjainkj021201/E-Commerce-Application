package com.karan.ecommerce.inventoryservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karan.ecommerce.inventoryservice.event.OrderCancelledEvent;
import com.karan.ecommerce.inventoryservice.event.OrderCreatedEvent;
import com.karan.ecommerce.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;

    public InventoryEventListener(ObjectMapper objectMapper, InventoryService inventoryService) {
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service")
    public void onOrderCreated(String payload) throws JsonProcessingException {
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        inventoryService.reserveInventoryForOrder(event);
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "inventory-service")
    public void onOrderCancelled(String payload) throws JsonProcessingException {
        OrderCancelledEvent event = objectMapper.readValue(payload, OrderCancelledEvent.class);
        inventoryService.releaseReservationForOrder(event);
    }
}
