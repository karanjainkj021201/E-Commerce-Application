package com.karan.ecommerce.productservice.service;

import com.karan.ecommerce.productservice.dto.ProductRequest;
import com.karan.ecommerce.productservice.dto.ProductResponse;
import com.karan.ecommerce.productservice.dto.ProductSnapshotResponse;
import com.karan.ecommerce.productservice.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    ProductResponse updateProductStatus(Long id, ProductStatus status);
    ProductResponse getProductForAdmin(Long id);
    ProductResponse getProductForPublic(Long id);
    Page<ProductResponse> getProductsForAdmin(int page, int size);
    Page<ProductResponse> searchPublicProducts(String search, Long categoryId, int page, int size);
    ProductSnapshotResponse getProductSnapshot(Long id);
}
