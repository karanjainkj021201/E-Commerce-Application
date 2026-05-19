package com.karan.ecommerce.productservice.service;

import com.karan.ecommerce.productservice.dto.CategoryRequest;
import com.karan.ecommerce.productservice.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    CategoryResponse updateCategoryActiveFlag(Long id, boolean active);
    List<CategoryResponse> getAllCategoriesForAdmin();
    List<CategoryResponse> getActiveCategoriesForPublic();
    CategoryResponse getCategoryById(Long id);
}
