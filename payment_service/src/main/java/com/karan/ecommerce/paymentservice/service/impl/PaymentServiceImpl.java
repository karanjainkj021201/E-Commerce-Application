package com.karan.ecommerce.paymentservice.service.impl;

import com.karan.ecommerce.paymentservice.dto.PaymentResponse;
import com.karan.ecommerce.paymentservice.dto.RefundResponse;
import com.karan.ecommerce.paymentservice.entity.PaymentAttemptEntity;
import com.karan.ecommerce.paymentservice.entity.PaymentRefundEntity;
import com.karan.ecommerce.paymentservice.entity.enums.PaymentProvider;
import com.karan.ecommerce.paymentservice.entity.enums.PaymentStatus;
import com.karan.ecommerce.paymentservice.entity.enums.RefundStatus;
import com.karan.ecommerce.paymentservice.event.OrderCreatedEvent;
import com.karan.ecommerce.paymentservice.exception.BadRequestException;
import com.karan.ecommerce.paymentservice.exception.ResourceNotFoundException;
import com.karan.ecommerce.paymentservice.gateway.PaymentGateway;
import com.karan.ecommerce.paymentservice.gateway.PaymentGatewayRequest;
import com.karan.ecommerce.paymentservice.messaging.PaymentEventPublisher;
import com.karan.ecommerce.paymentservice.repository.PaymentAttemptRepository;
import com.karan.ecommerce.paymentservice.repository.PaymentRefundRepository;
import com.karan.ecommerce.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter REF_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentEventPublisher eventPublisher;
    private final Map<PaymentProvider, PaymentGateway> paymentGateways;
    private final String merchantVpa;
    private final String merchantName;
    private final String merchantCode;

    public PaymentServiceImpl(PaymentAttemptRepository paymentAttemptRepository,
                              PaymentRefundRepository paymentRefundRepository,
                              PaymentEventPublisher eventPublisher,
                              List<PaymentGateway> paymentGateways,
                              @Value("${payment.google-pay.merchant-vpa}") String merchantVpa,
                              @Value("${payment.google-pay.merchant-name}") String merchantName,
                              @Value("${payment.google-pay.merchant-code}") String merchantCode) {
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.eventPublisher = eventPublisher;
        this.paymentGateways = paymentGateways.stream()
                .collect(Collectors.toMap(PaymentGateway::provider, Function.identity()));
        this.merchantVpa = merchantVpa;
        this.merchantName = merchantName;
        this.merchantCode = merchantCode;
    }

    @Override
    @Transactional
    public PaymentResponse createPaymentForOrder(OrderCreatedEvent event) {
        if (event.getOrderId() == null) {
            throw new BadRequestException("orderId is required to create payment");
        }

        return paymentAttemptRepository.findByOrderId(event.getOrderId())
                .map(this::mapToResponse)
                .orElseGet(() -> mapToResponse(createNewPaymentAttempt(event)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getMyPayment(String paymentReference, String keycloakUserId) {
        PaymentAttemptEntity paymentAttempt = paymentAttemptRepository
                .findByPaymentReferenceAndKeycloakUserId(paymentReference, keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for logged-in user"));
        return mapToResponse(paymentAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(String keycloakUserId, int page, int size) {
        return paymentAttemptRepository.findByKeycloakUserIdOrderByCreatedAtDesc(keycloakUserId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentForAdmin(String paymentReference) {
        return mapToResponse(getPaymentByReference(paymentReference));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderIdForAdmin(Long orderId) {
        PaymentAttemptEntity paymentAttempt = paymentAttemptRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id " + orderId));
        return mapToResponse(paymentAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsForAdmin(int page, int size) {
        return paymentAttemptRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public String getRedirectUrl(String paymentReference, String keycloakUserId) {
        PaymentAttemptEntity paymentAttempt = paymentAttemptRepository
                .findByPaymentReferenceAndKeycloakUserId(paymentReference, keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for logged-in user"));

        if (paymentAttempt.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new BadRequestException("Payment is already successful");
        }

        if (paymentAttempt.getStatus() == PaymentStatus.FAILED || paymentAttempt.getStatus() == PaymentStatus.CANCELLED) {
            throw new BadRequestException("Payment is not payable in status " + paymentAttempt.getStatus());
        }

        return paymentAttempt.getGatewayPaymentUrl();
    }

    @Override
    @Transactional
    public PaymentResponse markPaymentSucceeded(String paymentReference, String gatewayReference) {
        PaymentAttemptEntity paymentAttempt = getPaymentByReference(paymentReference);

        if (paymentAttempt.getStatus() == PaymentStatus.SUCCEEDED) {
            return mapToResponse(paymentAttempt);
        }

        if (paymentAttempt.getStatus() == PaymentStatus.FAILED || paymentAttempt.getStatus() == PaymentStatus.CANCELLED) {
            throw new BadRequestException("Cannot mark payment success from status " + paymentAttempt.getStatus());
        }

        paymentAttempt.setStatus(PaymentStatus.SUCCEEDED);
        paymentAttempt.setGatewayReference(firstNonBlank(gatewayReference, "LOCAL-GPAY-" + paymentReference));
        paymentAttempt.setFailureReason(null);
        paymentAttempt.setSucceededAt(LocalDateTime.now());

        PaymentAttemptEntity saved = paymentAttemptRepository.save(paymentAttempt);
        eventPublisher.publishPaymentSucceeded(saved);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse markPaymentFailed(String paymentReference, String failureReason) {
        PaymentAttemptEntity paymentAttempt = getPaymentByReference(paymentReference);

        if (paymentAttempt.getStatus() == PaymentStatus.SUCCEEDED || paymentAttempt.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Cannot fail payment in status " + paymentAttempt.getStatus());
        }

        if (paymentAttempt.getStatus() == PaymentStatus.FAILED) {
            return mapToResponse(paymentAttempt);
        }

        paymentAttempt.setStatus(PaymentStatus.FAILED);
        paymentAttempt.setFailureReason(blankToNull(failureReason) == null ? "Payment failed" : failureReason.trim());
        paymentAttempt.setFailedAt(LocalDateTime.now());

        PaymentAttemptEntity saved = paymentAttemptRepository.save(paymentAttempt);
        eventPublisher.publishPaymentFailed(saved);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RefundResponse refundPayment(String paymentReference, BigDecimal amount, String reason) {
        PaymentAttemptEntity paymentAttempt = getPaymentByReference(paymentReference);

        if (paymentAttempt.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new BadRequestException("Only successful payments can be refunded");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Refund amount must be greater than 0");
        }

        if (amount.compareTo(paymentAttempt.getAmount()) > 0) {
            throw new BadRequestException("Refund amount cannot be greater than payment amount");
        }

        PaymentRefundEntity refund = new PaymentRefundEntity();
        refund.setRefundReference(generateReference("REF"));
        refund.setAmount(amount);
        refund.setCurrency(paymentAttempt.getCurrency());
        refund.setStatus(RefundStatus.SUCCEEDED);
        refund.setReason(blankToNull(reason));
        refund.setCompletedAt(LocalDateTime.now());

        paymentAttempt.addRefund(refund);
        paymentAttempt.setStatus(PaymentStatus.REFUNDED);

        paymentAttemptRepository.save(paymentAttempt);
        PaymentRefundEntity savedRefund = paymentRefundRepository.save(refund);

        return mapRefundToResponse(savedRefund);
    }

    private PaymentAttemptEntity createNewPaymentAttempt(OrderCreatedEvent event) {
        validateOrderCreatedEvent(event);

        PaymentProvider provider = PaymentProvider.GOOGLE_PAY;
        String paymentReference = generateReference("PAY");
        String currency = blankToNull(event.getCurrency()) == null ? "INR" : event.getCurrency().trim().toUpperCase();

        PaymentGateway gateway = paymentGateways.get(provider);
        if (gateway == null) {
            throw new BadRequestException("No payment gateway configured for " + provider);
        }

        String redirectUri = gateway.buildRedirectUri(new PaymentGatewayRequest(
                merchantVpa,
                merchantName,
                merchantCode,
                paymentReference,
                event.getOrderNumber(),
                event.getTotalAmount(),
                currency
        ));

        PaymentAttemptEntity paymentAttempt = new PaymentAttemptEntity();
        paymentAttempt.setOrderId(event.getOrderId());
        paymentAttempt.setOrderNumber(event.getOrderNumber());
        paymentAttempt.setKeycloakUserId(event.getKeycloakUserId());
        paymentAttempt.setAmount(event.getTotalAmount());
        paymentAttempt.setCurrency(currency);
        paymentAttempt.setProvider(provider);
        paymentAttempt.setStatus(PaymentStatus.REDIRECT_CREATED);
        paymentAttempt.setPaymentMethod(firstNonBlank(event.getPaymentMethod(), "GOOGLE_PAY"));
        paymentAttempt.setPaymentReference(paymentReference);
        paymentAttempt.setGatewayPaymentUrl(redirectUri);

        return paymentAttemptRepository.save(paymentAttempt);
    }

    private void validateOrderCreatedEvent(OrderCreatedEvent event) {
        if (event.getOrderId() == null) throw new BadRequestException("orderId is required");
        if (blankToNull(event.getOrderNumber()) == null) throw new BadRequestException("orderNumber is required");
        if (blankToNull(event.getKeycloakUserId()) == null) throw new BadRequestException("keycloakUserId is required");
        if (event.getTotalAmount() == null || event.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("totalAmount must be greater than 0");
        }
    }

    private PaymentAttemptEntity getPaymentByReference(String paymentReference) {
        return paymentAttemptRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for reference " + paymentReference));
    }

    private PaymentResponse mapToResponse(PaymentAttemptEntity paymentAttempt) {
        return new PaymentResponse(
                paymentAttempt.getId(),
                paymentAttempt.getOrderId(),
                paymentAttempt.getOrderNumber(),
                paymentAttempt.getKeycloakUserId(),
                paymentAttempt.getAmount(),
                paymentAttempt.getCurrency(),
                paymentAttempt.getProvider(),
                paymentAttempt.getStatus(),
                paymentAttempt.getPaymentMethod(),
                paymentAttempt.getPaymentReference(),
                paymentAttempt.getGatewayReference(),
                paymentAttempt.getGatewayPaymentUrl(),
                paymentAttempt.getFailureReason(),
                paymentAttempt.getRefunds().stream().map(this::mapRefundToResponse).toList(),
                paymentAttempt.getCreatedAt(),
                paymentAttempt.getUpdatedAt(),
                paymentAttempt.getSucceededAt(),
                paymentAttempt.getFailedAt()
        );
    }

    private RefundResponse mapRefundToResponse(PaymentRefundEntity refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getRefundReference(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getReason(),
                refund.getCreatedAt(),
                refund.getCompletedAt()
        );
    }

    private String generateReference(String prefix) {
        String timestamp = LocalDateTime.now().format(REF_DATE_FORMAT);
        int randomNumber = 100000 + RANDOM.nextInt(900000);
        return prefix + "-" + timestamp + "-" + randomNumber;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        return blankToNull(second);
    }
}
