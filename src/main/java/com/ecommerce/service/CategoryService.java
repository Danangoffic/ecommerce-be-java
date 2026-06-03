package com.ecommerce.service;

import com.ecommerce.dto.request.CategoryUpsertRequest;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.enums.CategoryStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByStatusOrderByNameAsc(CategoryStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryUpsertRequest request) {
        Category category = new Category();
        apply(category, request);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryUpsertRequest request) {
        Category category = getCategory(id);
        apply(category, request);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse deactivate(Long id) {
        Category category = getCategory(id);
        if (productRepository.existsByCategoryId(id)) {
            category.setStatus(CategoryStatus.INACTIVE);
        } else {
            category.setStatus(CategoryStatus.INACTIVE);
        }
        return toResponse(categoryRepository.save(category));
    }

    public Category getActiveCategory(Long id) {
        Category category = getCategory(id);
        if (category.getStatus() != CategoryStatus.ACTIVE) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private void apply(Category category, CategoryUpsertRequest request) {
        category.setName(request.name());
        category.setDescription(request.description());
        category.setStatus(CategoryStatus.valueOf(request.status().toUpperCase()));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), category.getStatus().name());
    }
}
