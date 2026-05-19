package com.karan.ecommerce.productservice.controller;

import com.karan.ecommerce.productservice.dto.CategoryResponse;
import com.karan.ecommerce.productservice.dto.ProductResponse;
import com.karan.ecommerce.productservice.service.CategoryService;
import com.karan.ecommerce.productservice.service.ProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/catalog")
@Validated
public class PublicCatalogController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public PublicCatalogController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> getProducts(@RequestParam(required = false) String search,
                                                             @RequestParam(required = false) Long categoryId,
                                                             @RequestParam(defaultValue = "0") @Min(0) int page,
                                                             @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(productService.searchPublicProducts(search, categoryId, page, size));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductForPublic(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategoriesForPublic());
    }
}
