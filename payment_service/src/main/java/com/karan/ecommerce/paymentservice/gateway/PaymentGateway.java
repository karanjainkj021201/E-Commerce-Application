package com.karan.ecommerce.paymentservice.gateway;

import com.karan.ecommerce.paymentservice.entity.enums.PaymentProvider;

public interface PaymentGateway {
    PaymentProvider provider();
    String buildRedirectUri(PaymentGatewayRequest request);
}
