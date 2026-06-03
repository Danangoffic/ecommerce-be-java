package com.ecommerce.controller;

import com.ecommerce.dto.request.CategoryUpsertRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryUpsertRequest request) {
        return ApiResponse.success("Category created", categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryUpsertRequest request) {
        return ApiResponse.success("Category updated", categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<CategoryResponse> deactivate(@PathVariable Long id) {
        return ApiResponse.success("Category deactivated", categoryService.deactivate(id));
    }
}
