package com.karan.ecommerce.paymentservice.gateway;

import com.karan.ecommerce.paymentservice.entity.enums.PaymentProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.RoundingMode;

@Component
public class GooglePayUpiRedirectGateway implements PaymentGateway {

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.GOOGLE_PAY;
    }

    @Override
    public String buildRedirectUri(PaymentGatewayRequest request) {
        String amount = request.getAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme("upi")
                .host("pay")
                .queryParam("pa", request.getMerchantVpa())
                .queryParam("pn", request.getMerchantName())
                .queryParam("tr", request.getPaymentReference())
                .queryParam("tn", "Order " + request.getOrderNumber())
                .queryParam("am", amount)
                .queryParam("cu", request.getCurrency());

        if (request.getMerchantCode() != null && !request.getMerchantCode().isBlank()) {
            builder.queryParam("mc", request.getMerchantCode());
        }

        return builder.build()
                .encode()
                .toUriString();
    }
}
