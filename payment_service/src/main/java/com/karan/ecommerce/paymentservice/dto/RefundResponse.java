package com.karan.ecommerce.paymentservice.dto;

import com.karan.ecommerce.paymentservice.entity.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundResponse {
    private Long id;
    private String refundReference;
    private BigDecimal amount;
    private String currency;
    private RefundStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public RefundResponse(Long id, String refundReference, BigDecimal amount, String currency,
                          RefundStatus status, String reason, LocalDateTime createdAt,
                          LocalDateTime completedAt) {
        this.id = id;
        this.refundReference = refundReference;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getId() { return id; }
    public String getRefundReference() { return refundReference; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public RefundStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
