package com.karan.ecommerce.paymentservice.controller;

import com.karan.ecommerce.paymentservice.dto.PaymentResponse;
import com.karan.ecommerce.paymentservice.service.PaymentService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/payments")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/me")
    public ResponseEntity<Page<PaymentResponse>> getMyPayments(@AuthenticationPrincipal Jwt jwt,
                                                               @RequestParam(defaultValue = "0") @Min(0) int page,
                                                               @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(paymentService.getMyPayments(jwt.getSubject(), page, size));
    }

    @GetMapping("/{paymentReference}")
    public ResponseEntity<PaymentResponse> getMyPayment(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable String paymentReference) {
        return ResponseEntity.ok(paymentService.getMyPayment(paymentReference, jwt.getSubject()));
    }

    @GetMapping("/{paymentReference}/redirect")
    public ResponseEntity<Void> redirectToPaymentApp(@AuthenticationPrincipal Jwt jwt,
                                                     @PathVariable String paymentReference) {
        String redirectUrl = paymentService.getRedirectUrl(paymentReference, jwt.getSubject());
        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }
}
