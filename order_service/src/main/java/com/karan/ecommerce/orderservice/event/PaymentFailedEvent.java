package com.karan.ecommerce.orderservice.event;

public class PaymentFailedEvent {
    private Long orderId;
    private String paymentReference;
    private String failureReason;

    public PaymentFailedEvent() {
    }

    public PaymentFailedEvent(Long orderId, String paymentReference, String failureReason) {
        this.orderId = orderId;
        this.paymentReference = paymentReference;
        this.failureReason = failureReason;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
