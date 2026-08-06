package com.karan.ecommerce.orderservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karan.ecommerce.orderservice.entity.OrderEntity;
import com.karan.ecommerce.orderservice.entity.OrderItemEntity;
import com.karan.ecommerce.orderservice.event.OrderCancelledEvent;
import com.karan.ecommerce.orderservice.event.OrderConfirmedEvent;
import com.karan.ecommerce.orderservice.event.OrderCreatedEvent;
import com.karan.ecommerce.orderservice.event.OrderItemEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
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

        publish(
                KafkaTopics.ORDER_CREATED,
                order.getId().toString(),
                event,
                order.getId()
        );
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

        publish(
                KafkaTopics.ORDER_CONFIRMED,
                order.getId().toString(),
                event,
                order.getId()
        );
    }

    public void publishOrderCancelled(OrderEntity order, String reason) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getKeycloakUserId(),
                order.getTotalAmount(),
                order.getCurrency(),
                reason,
                mapItems(order.getItems()),
                LocalDateTime.now()
        );

        publish(
                KafkaTopics.ORDER_CANCELLED,
                order.getId().toString(),
                event,
                order.getId()
        );
    }

    private void publish(
            String topic,
            String key,
            Object event,
            Long orderId
    ) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, key, payload)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error(
                                    "Failed to publish event: topic={}, orderId={}",
                                    topic,
                                    orderId,
                                    exception
                            );
                            return;
                        }

                        log.info(
                                "Published event: topic={}, orderId={}, partition={}, offset={}",
                                topic,
                                orderId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize event for orderId=" + orderId,
                    exception
            );
        }
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