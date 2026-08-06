package com.karan.ecommerce.inventoryservice.repository;

import com.karan.ecommerce.inventoryservice.entity.StockReservationEntity;
import com.karan.ecommerce.inventoryservice.entity.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockReservationRepository extends JpaRepository<StockReservationEntity, Long> {

    Optional<StockReservationEntity> findByReservationNumber(String reservationNumber);

    Optional<StockReservationEntity> findByOrderId(Long orderId);

    Page<StockReservationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from StockReservationEntity r where r.reservationNumber = :reservationNumber")
    Optional<StockReservationEntity> findByReservationNumberForUpdate(
            @Param("reservationNumber") String reservationNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from StockReservationEntity r where r.orderId = :orderId")
    Optional<StockReservationEntity> findByOrderIdForUpdate(@Param("orderId") Long orderId);

    @Query("""
            select r.reservationNumber
            from StockReservationEntity r
            where r.status = :status
              and r.expiresAt is not null
              and r.expiresAt <= :now
            order by r.expiresAt asc
            """)
    List<String> findDueReservationNumbers(
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now
    );
}
