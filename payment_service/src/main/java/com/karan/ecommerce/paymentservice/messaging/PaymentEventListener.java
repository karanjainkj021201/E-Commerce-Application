package com.karan.ecommerce.paymentservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karan.ecommerce.paymentservice.event.OrderCreatedEvent;
import com.karan.ecommerce.paymentservice.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public PaymentEventListener(ObjectMapper objectMapper, PaymentService paymentService) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "payment-service")
    public void onOrderCreated(String payload) throws JsonProcessingException {
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        paymentService.createPaymentForOrder(event);
    }
}
