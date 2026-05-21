package com.karan.ecommerce.orderservice.repository;

import com.karan.ecommerce.orderservice.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    Optional<OrderEntity> findByIdAndKeycloakUserId(Long id, String keycloakUserId);
    Page<OrderEntity> findByKeycloakUserIdOrderByCreatedAtDesc(String keycloakUserId, Pageable pageable);
}
