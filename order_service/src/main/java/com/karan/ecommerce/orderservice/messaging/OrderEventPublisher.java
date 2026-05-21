package com.karan.ecommerce.orderservice.messaging;

import com.karan.ecommerce.orderservice.entity.OrderEntity;
import com.karan.ecommerce.orderservice.entity.OrderItemEntity;
import com.karan.ecommerce.orderservice.event.OrderConfirmedEvent;
import com.karan.ecommerce.orderservice.event.OrderCreatedEvent;
import com.karan.ecommerce.orderservice.event.OrderItemEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderEntity order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getKeycloakUserId(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getPaymentMethod(),
                mapItems(order.getItems()),
                LocalDateTime.now()
        );
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, order.getId().toString(), event);
    }

    public void publishOrderConfirmed(OrderEntity order) {
        OrderConfirmedEvent event = new OrderConfirmedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getKeycloakUserId(),
                order.getTotalAmount(),
                order.getCurrency(),
                mapItems(order.getItems()),
                LocalDateTime.now()
        );
        kafkaTemplate.send(KafkaTopics.ORDER_CONFIRMED, order.getId().toString(), event);
    }

    private List<OrderItemEvent> mapItems(List<OrderItemEntity> items) {
        return items.stream()
                .map(item -> new OrderItemEvent(
                        item.getProductId(),
                        item.getSku(),
                        item.getProductNameSnapshot(),
                        item.getUnitPriceSnapshot(),
                        item.getCurrency(),
                        item.getQuantity(),
                        item.getLineTotal()
                ))
                .toList();
    }
}
