package com.karan.ecommerce.paymentservice.controller;

import com.karan.ecommerce.paymentservice.dto.MockPaymentFailureRequest;
import com.karan.ecommerce.paymentservice.dto.MockPaymentSuccessRequest;
import com.karan.ecommerce.paymentservice.dto.PaymentResponse;
import com.karan.ecommerce.paymentservice.dto.RefundRequest;
import com.karan.ecommerce.paymentservice.dto.RefundResponse;
import com.karan.ecommerce.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminPaymentController {

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getPayments(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                             @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(paymentService.getPaymentsForAdmin(page, size));
    }

    @GetMapping("/{paymentReference}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentReference) {
        return ResponseEntity.ok(paymentService.getPaymentForAdmin(paymentReference));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderIdForAdmin(orderId));
    }

    @PostMapping("/{paymentReference}/mock-success")
    public ResponseEntity<PaymentResponse> markSuccess(@PathVariable String paymentReference,
                                                       @RequestBody(required = false) MockPaymentSuccessRequest request) {
        String gatewayReference = request == null ? null : request.getGatewayReference();
        return ResponseEntity.ok(paymentService.markPaymentSucceeded(paymentReference, gatewayReference));
    }

    @PostMapping("/{paymentReference}/mock-failure")
    public ResponseEntity<PaymentResponse> markFailure(@PathVariable String paymentReference,
                                                       @Valid @RequestBody MockPaymentFailureRequest request) {
        return ResponseEntity.ok(paymentService.markPaymentFailed(paymentReference, request.getFailureReason()));
    }

    @PostMapping("/{paymentReference}/refunds")
    public ResponseEntity<RefundResponse> refund(@PathVariable String paymentReference,
                                                 @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentReference, request.getAmount(), request.getReason()));
    }
}
