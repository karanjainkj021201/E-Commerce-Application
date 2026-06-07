package com.karan.ecommerce.inventoryservice.repository;

import com.karan.ecommerce.inventoryservice.entity.InventoryLedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLedgerRepository extends JpaRepository<InventoryLedgerEntity, Long> {
    Page<InventoryLedgerEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<InventoryLedgerEntity> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
}
