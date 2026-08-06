package com.karan.ecommerce.shippingservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karan.ecommerce.shippingservice.entity.ShipmentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class ShipmentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ShipmentEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishShipmentCreated(ShipmentEntity shipment) {
        Map<String, Object> event = new LinkedHashMap<>();

        event.put("orderId", shipment.getOrderId());
        event.put("shipmentId", shipment.getId().toString());
        event.put("carrier", shipment.getCarrier());
        event.put("trackingNumber", shipment.getTrackingNumber());

        publish(
                KafkaTopics.SHIPMENT_CREATED,
                shipment.getOrderId().toString(),
                event,
                shipment.getOrderId()
        );
    }

    public void publishShipmentDelivered(ShipmentEntity shipment) {
        Map<String, Object> event = new LinkedHashMap<>();

        event.put("orderId", shipment.getOrderId());
        event.put("shipmentId", shipment.getId().toString());
        event.put("trackingNumber", shipment.getTrackingNumber());

        publish(
                KafkaTopics.SHIPMENT_DELIVERED,
                shipment.getOrderId().toString(),
                event,
                shipment.getOrderId()
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
                                    "Failed to publish shipping event: topic={}, orderId={}",
                                    topic,
                                    orderId,
                                    exception
                            );
                            return;
                        }

                        log.info(
                                "Published shipping event: topic={}, orderId={}, partition={}, offset={}",
                                topic,
                                orderId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize shipping event for orderId=" + orderId,
                    exception
            );
        }
    }
}