package com.karan.ecommerce.productservice.service.impl;

import com.karan.ecommerce.productservice.dto.CategoryResponse;
import com.karan.ecommerce.productservice.dto.ProductRequest;
import com.karan.ecommerce.productservice.dto.ProductResponse;
import com.karan.ecommerce.productservice.dto.ProductSnapshotResponse;
import com.karan.ecommerce.productservice.entity.CategoryEntity;
import com.karan.ecommerce.productservice.entity.ProductEntity;
import com.karan.ecommerce.productservice.entity.enums.ProductStatus;
import com.karan.ecommerce.productservice.exception.DuplicateResourceException;
import com.karan.ecommerce.productservice.exception.ResourceNotFoundException;
import com.karan.ecommerce.productservice.repository.CategoryRepository;
import com.karan.ecommerce.productservice.repository.ProductRepository;
import com.karan.ecommerce.productservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        validateSkuUniqueness(request.getSku(), null);
        ProductEntity product = new ProductEntity();
        applyRequestToProduct(product, request);
        product.setStatus(ProductStatus.ACTIVE);
        return mapToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        ProductEntity product = getProductEntityById(id);
        validateSkuUniqueness(request.getSku(), id);
        applyRequestToProduct(product, request);
        return mapToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProductStatus(Long id, ProductStatus status) {
        ProductEntity product = getProductEntityById(id);
        product.setStatus(status);
        return mapToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProductForAdmin(Long id) {
        return mapToResponse(getProductEntityById(id));
    }

    @Override
    public ProductResponse getProductForPublic(Long id) {
        ProductEntity product = getProductEntityById(id);
        if (product.getStatus() != ProductStatus.ACTIVE || !product.getCategory().isActive()) {
            throw new ResourceNotFoundException("Product not found");
        }
        return mapToResponse(product);
    }

    @Override
    public Page<ProductResponse> getProductsForAdmin(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size)).map(this::mapToResponse);
    }

    @Override
    public Page<ProductResponse> searchPublicProducts(String search, Long categoryId, int page, int size) {
        String nameFilter = search == null ? "" : search.trim();
        Page<ProductEntity> products;
        if (categoryId != null && !nameFilter.isBlank()) {
            products = productRepository.findByStatusAndCategory_IdAndCategory_ActiveTrueAndNameContainingIgnoreCase(
                    ProductStatus.ACTIVE, categoryId, nameFilter, PageRequest.of(page, size));
        } else if (categoryId != null) {
            products = productRepository.findByStatusAndCategory_IdAndCategory_ActiveTrue(
                    ProductStatus.ACTIVE, categoryId, PageRequest.of(page, size));
        } else {
            products = productRepository.findByStatusAndCategory_ActiveTrueAndNameContainingIgnoreCase(
                    ProductStatus.ACTIVE, nameFilter, PageRequest.of(page, size));
        }
        return products.map(this::mapToResponse);
    }

    @Override
    public ProductSnapshotResponse getProductSnapshot(Long id) {
        ProductEntity product = getProductEntityById(id);

        boolean available = product.getStatus() == ProductStatus.ACTIVE
                && product.getCategory().isActive();

        if (!available) {
            throw new ResourceNotFoundException("Product is not available for ordering");
        }

        return new ProductSnapshotResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getCurrency(),
                true
        );
    }

    private void applyRequestToProduct(ProductEntity product, ProductRequest request) {
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for id " + request.getCategoryId()));
        product.setSku(request.getSku());
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCurrency(request.getCurrency());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
    }

    private void validateSkuUniqueness(String sku, Long currentId) {
        productRepository.findAll().forEach(existing -> {
            if (existing.getSku().equalsIgnoreCase(sku) && (currentId == null || !existing.getId().equals(currentId))) {
                throw new DuplicateResourceException("Product SKU already exists");
            }
        });
    }

    private ProductEntity getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id " + id));
    }

    private ProductResponse mapToResponse(ProductEntity product) {
        CategoryEntity category = product.getCategory();
        CategoryResponse categoryResponse = new CategoryResponse(
                category.getId(), category.getName(), category.getCode(), category.getDescription(),
                category.isActive(), category.getCreatedAt(), category.getUpdatedAt());

        return new ProductResponse(
                product.getId(), product.getSku(), product.getName(), product.getDescription(),
                product.getPrice(), product.getCurrency(), product.getImageUrl(), product.getStatus(),
                categoryResponse, product.getCreatedAt(), product.getUpdatedAt());
    }
}
