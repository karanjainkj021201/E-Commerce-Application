package com.karan.ecommerce.paymentservice.service;

import com.karan.ecommerce.paymentservice.dto.PaymentResponse;
import com.karan.ecommerce.paymentservice.dto.RefundResponse;
import com.karan.ecommerce.paymentservice.event.OrderCreatedEvent;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentResponse createPaymentForOrder(OrderCreatedEvent event);
    PaymentResponse getMyPayment(String paymentReference, String keycloakUserId);
    Page<PaymentResponse> getMyPayments(String keycloakUserId, int page, int size);
    PaymentResponse getPaymentForAdmin(String paymentReference);
    PaymentResponse getPaymentByOrderIdForAdmin(Long orderId);
    Page<PaymentResponse> getPaymentsForAdmin(int page, int size);
    String getRedirectUrl(String paymentReference, String keycloakUserId);
    PaymentResponse markPaymentSucceeded(String paymentReference, String gatewayReference);
    PaymentResponse markPaymentFailed(String paymentReference, String failureReason);
    RefundResponse refundPayment(String paymentReference, BigDecimal amount, String reason);
}
