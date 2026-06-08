package com.karan.ecommerce.paymentservice.entity;

import com.karan.ecommerce.paymentservice.entity.enums.PaymentProvider;
import com.karan.ecommerce.paymentservice.entity.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_attempts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_attempt_order_id", columnNames = "order_id"),
        @UniqueConstraint(name = "uk_payment_attempt_reference", columnNames = "payment_reference")
})
public class PaymentAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 80)
    private String orderNumber;

    @Column(name = "keycloak_user_id", nullable = false, length = 100)
    private String keycloakUserId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentStatus status;

    @Column(name = "payment_method", nullable = false, length = 60)
    private String paymentMethod;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 120)
    private String paymentReference;

    @Column(name = "gateway_reference", length = 120)
    private String gatewayReference;

    @Column(name = "gateway_payment_url", nullable = false, length = 2000)
    private String gatewayPaymentUrl;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @OneToMany(mappedBy = "paymentAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRefundEntity> refunds = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "succeeded_at")
    private LocalDateTime succeededAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = PaymentStatus.PENDING;
        if (this.provider == null) this.provider = PaymentProvider.GOOGLE_PAY;
        if (this.currency == null || this.currency.isBlank()) this.currency = "INR";
        if (this.paymentMethod == null || this.paymentMethod.isBlank()) this.paymentMethod = "GOOGLE_PAY";
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addRefund(PaymentRefundEntity refund) {
        refunds.add(refund);
        refund.setPaymentAttempt(this);
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getKeycloakUserId() { return keycloakUserId; }
    public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency == null ? null : currency.trim().toUpperCase(); }
    public PaymentProvider getProvider() { return provider; }
    public void setProvider(PaymentProvider provider) { this.provider = provider; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getGatewayReference() { return gatewayReference; }
    public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }
    public String getGatewayPaymentUrl() { return gatewayPaymentUrl; }
    public void setGatewayPaymentUrl(String gatewayPaymentUrl) { this.gatewayPaymentUrl = gatewayPaymentUrl; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public List<PaymentRefundEntity> getRefunds() { return refunds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getSucceededAt() { return succeededAt; }
    public void setSucceededAt(LocalDateTime succeededAt) { this.succeededAt = succeededAt; }
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
}
