package com.karan.ecommerce.productservice.controller;

import com.karan.ecommerce.productservice.dto.ProductSnapshotResponse;
import com.karan.ecommerce.productservice.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {

    private final ProductService productService;

    public InternalProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}/snapshot")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    public ProductSnapshotResponse getProductSnapshot(@PathVariable Long id) {
        return productService.getProductSnapshot(id);
    }
}
