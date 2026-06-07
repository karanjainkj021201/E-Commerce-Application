package com.karan.ecommerce.inventoryservice.repository;

import com.karan.ecommerce.inventoryservice.entity.StockBalanceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockBalanceRepository extends JpaRepository<StockBalanceEntity, Long> {

    Optional<StockBalanceEntity> findByProductIdAndWarehouseCode(Long productId, String warehouseCode);

    List<StockBalanceEntity> findByProductIdOrderByWarehouseCodeAsc(Long productId);

    Page<StockBalanceEntity> findByProductId(Long productId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockBalanceEntity s where s.id = :id")
    Optional<StockBalanceEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockBalanceEntity s where s.productId = :productId and s.warehouseCode = :warehouseCode")
    Optional<StockBalanceEntity> findByProductIdAndWarehouseCodeForUpdate(@Param("productId") Long productId,
                                                                          @Param("warehouseCode") String warehouseCode);
}
