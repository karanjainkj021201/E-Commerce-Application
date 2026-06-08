package com.karan.ecommerce.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;

public class MockPaymentFailureRequest {
    @NotBlank(message = "failureReason is required")
    private String failureReason;

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
