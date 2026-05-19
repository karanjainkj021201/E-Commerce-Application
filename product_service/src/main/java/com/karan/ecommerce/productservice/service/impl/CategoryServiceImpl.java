package com.karan.ecommerce.productservice.service.impl;

import com.karan.ecommerce.productservice.dto.CategoryRequest;
import com.karan.ecommerce.productservice.dto.CategoryResponse;
import com.karan.ecommerce.productservice.entity.CategoryEntity;
import com.karan.ecommerce.productservice.exception.DuplicateResourceException;
import com.karan.ecommerce.productservice.exception.ResourceNotFoundException;
import com.karan.ecommerce.productservice.repository.CategoryRepository;
import com.karan.ecommerce.productservice.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        validateUniqueness(request, null);
        CategoryEntity category = new CategoryEntity();
        category.setName(request.getName().trim());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setActive(true);
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        CategoryEntity category = getEntityById(id);
        validateUniqueness(request, id);
        category.setName(request.getName().trim());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse updateCategoryActiveFlag(Long id, boolean active) {
        CategoryEntity category = getEntityById(id);
        category.setActive(active);
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getAllCategoriesForAdmin() {
        return categoryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<CategoryResponse> getActiveCategoriesForPublic() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream().map(this::mapToResponse).toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return mapToResponse(getEntityById(id));
    }

    private CategoryEntity getEntityById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found for id " + id));
    }

    private void validateUniqueness(CategoryRequest request, Long currentId) {
        categoryRepository.findAll().forEach(existing -> {
            if (existing.getName().equalsIgnoreCase(request.getName()) && (currentId == null || !existing.getId().equals(currentId))) {
                throw new DuplicateResourceException("Category name already exists");
            }
            if (existing.getCode().equalsIgnoreCase(request.getCode()) && (currentId == null || !existing.getId().equals(currentId))) {
                throw new DuplicateResourceException("Category code already exists");
            }
        });
    }

    private CategoryResponse mapToResponse(CategoryEntity category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getCode(), category.getDescription(), category.isActive(), category.getCreatedAt(), category.getUpdatedAt());
    }
}
