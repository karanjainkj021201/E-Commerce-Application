package com.karan.ecommerce.paymentservice.dto;

import com.karan.ecommerce.paymentservice.entity.enums.PaymentProvider;
import com.karan.ecommerce.paymentservice.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private String keycloakUserId;
    private BigDecimal amount;
    private String currency;
    private PaymentProvider provider;
    private PaymentStatus status;
    private String paymentMethod;
    private String paymentReference;
    private String gatewayReference;
    private String gatewayPaymentUrl;
    private String failureReason;
    private List<RefundResponse> refunds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime succeededAt;
    private LocalDateTime failedAt;

    public PaymentResponse(Long id, Long orderId, String orderNumber, String keycloakUserId,
                           BigDecimal amount, String currency, PaymentProvider provider, PaymentStatus status,
                           String paymentMethod, String paymentReference, String gatewayReference,
                           String gatewayPaymentUrl, String failureReason, List<RefundResponse> refunds,
                           LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime succeededAt,
                           LocalDateTime failedAt) {
        this.id = id;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.keycloakUserId = keycloakUserId;
        this.amount = amount;
        this.currency = currency;
        this.provider = provider;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.gatewayReference = gatewayReference;
        this.gatewayPaymentUrl = gatewayPaymentUrl;
        this.failureReason = failureReason;
        this.refunds = refunds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.succeededAt = succeededAt;
        this.failedAt = failedAt;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public String getKeycloakUserId() { return keycloakUserId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentProvider getProvider() { return provider; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public String getGatewayReference() { return gatewayReference; }
    public String getGatewayPaymentUrl() { return gatewayPaymentUrl; }
    public String getFailureReason() { return failureReason; }
    public List<RefundResponse> getRefunds() { return refunds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getSucceededAt() { return succeededAt; }
    public LocalDateTime getFailedAt() { return failedAt; }
}
