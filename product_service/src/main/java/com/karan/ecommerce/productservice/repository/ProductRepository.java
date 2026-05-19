package com.karan.ecommerce.productservice.repository;

import com.karan.ecommerce.productservice.entity.ProductEntity;
import com.karan.ecommerce.productservice.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByStatusAndCategory_ActiveTrueAndNameContainingIgnoreCase(ProductStatus status, String name, Pageable pageable);
    Page<ProductEntity> findByStatusAndCategory_IdAndCategory_ActiveTrue(ProductStatus status, Long categoryId, Pageable pageable);
    Page<ProductEntity> findByStatusAndCategory_IdAndCategory_ActiveTrueAndNameContainingIgnoreCase(ProductStatus status, Long categoryId, String name, Pageable pageable);
}
