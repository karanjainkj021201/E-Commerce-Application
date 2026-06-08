package com.karan.ecommerce.paymentservice.event;

public class PaymentSucceededEvent {
    private Long orderId;
    private String paymentReference;
    private String gatewayReference;
    private String message;

    public PaymentSucceededEvent() {
    }

    public PaymentSucceededEvent(Long orderId, String paymentReference, String gatewayReference, String message) {
        this.orderId = orderId;
        this.paymentReference = paymentReference;
        this.gatewayReference = gatewayReference;
        this.message = message;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getGatewayReference() { return gatewayReference; }
    public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
