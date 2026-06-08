package com.karan.ecommerce.paymentservice.gateway;

import java.math.BigDecimal;

public class PaymentGatewayRequest {
    private final String merchantVpa;
    private final String merchantName;
    private final String merchantCode;
    private final String paymentReference;
    private final String orderNumber;
    private final BigDecimal amount;
    private final String currency;

    public PaymentGatewayRequest(String merchantVpa, String merchantName, String merchantCode,
                                 String paymentReference, String orderNumber, BigDecimal amount, String currency) {
        this.merchantVpa = merchantVpa;
        this.merchantName = merchantName;
        this.merchantCode = merchantCode;
        this.paymentReference = paymentReference;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.currency = currency;
    }

    public String getMerchantVpa() { return merchantVpa; }
    public String getMerchantName() { return merchantName; }
    public String getMerchantCode() { return merchantCode; }
    public String getPaymentReference() { return paymentReference; }
    public String getOrderNumber() { return orderNumber; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
