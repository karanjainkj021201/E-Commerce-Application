package com.karan.ecommerce.inventoryservice.repository;

import com.karan.ecommerce.inventoryservice.entity.StockReservationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockReservationRepository extends JpaRepository<StockReservationEntity, Long> {
    Optional<StockReservationEntity> findByReservationNumber(String reservationNumber);
    Optional<StockReservationEntity> findByOrderId(Long orderId);
    Page<StockReservationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
