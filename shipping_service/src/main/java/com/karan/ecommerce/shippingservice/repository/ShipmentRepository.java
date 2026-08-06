package com.karan.ecommerce.shippingservice.repository;

import com.karan.ecommerce.shippingservice.entity.ShipmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {
    Optional<ShipmentEntity> findByOrderId(Long orderId);
    Optional<ShipmentEntity> findByTrackingNumber(String trackingNumber);
    Optional<ShipmentEntity> findByIdAndKeycloakUserId(Long id, String keycloakUserId);
    Page<ShipmentEntity> findByKeycloakUserId(String keycloakUserId, Pageable pageable);
}
