package com.karan.ecommerce.paymentservice.messaging;

import com.karan.ecommerce.paymentservice.entity.PaymentAttemptEntity;
import com.karan.ecommerce.paymentservice.event.PaymentFailedEvent;
import com.karan.ecommerce.paymentservice.event.PaymentSucceededEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentSucceeded(PaymentAttemptEntity paymentAttempt) {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                paymentAttempt.getOrderId(),
                paymentAttempt.getPaymentReference(),
                paymentAttempt.getGatewayReference(),
                "Payment completed successfully"
        );
        kafkaTemplate.send(KafkaTopics.PAYMENT_SUCCEEDED, paymentAttempt.getOrderId().toString(), event);
    }

    public void publishPaymentFailed(PaymentAttemptEntity paymentAttempt) {
        PaymentFailedEvent event = new PaymentFailedEvent(
                paymentAttempt.getOrderId(),
                paymentAttempt.getPaymentReference(),
                paymentAttempt.getFailureReason()
        );
        kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, paymentAttempt.getOrderId().toString(), event);
    }
}
