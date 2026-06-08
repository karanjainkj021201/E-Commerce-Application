package com.karan.ecommerce.paymentservice.repository;

import com.karan.ecommerce.paymentservice.entity.PaymentAttemptEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttemptEntity, Long> {
    boolean existsByOrderId(Long orderId);
    Optional<PaymentAttemptEntity> findByOrderId(Long orderId);
    Optional<PaymentAttemptEntity> findByPaymentReference(String paymentReference);
    Optional<PaymentAttemptEntity> findByPaymentReferenceAndKeycloakUserId(String paymentReference, String keycloakUserId);
    Page<PaymentAttemptEntity> findByKeycloakUserIdOrderByCreatedAtDesc(String keycloakUserId, Pageable pageable);
}
