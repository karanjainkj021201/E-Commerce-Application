package com.karan.ecommerce.productservice.dto;

import com.karan.ecommerce.productservice.entity.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public class ProductStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ProductStatus status;

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
}
