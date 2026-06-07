package com.karan.ecommerce.inventoryservice.messaging;

import com.karan.ecommerce.inventoryservice.event.StockReservationFailedEvent;
import com.karan.ecommerce.inventoryservice.event.StockReservedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStockReserved(Long orderId, String reservationNumber) {
        StockReservedEvent event = new StockReservedEvent(
                orderId,
                reservationNumber,
                "Stock reserved successfully"
        );
        kafkaTemplate.send(KafkaTopics.STOCK_RESERVED, orderId.toString(), event);
    }

    public void publishStockReservationFailed(Long orderId, String failureReason) {
        StockReservationFailedEvent event = new StockReservationFailedEvent(orderId, failureReason);
        kafkaTemplate.send(KafkaTopics.STOCK_RESERVATION_FAILED, orderId.toString(), event);
    }
}
