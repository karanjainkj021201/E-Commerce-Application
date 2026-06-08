package com.karan.ecommerce.paymentservice.repository;

import com.karan.ecommerce.paymentservice.entity.PaymentRefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefundEntity, Long> {
}
