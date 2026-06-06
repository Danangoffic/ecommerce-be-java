package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductVariantUpsertRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.service.ProductVariantService;
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
@RequestMapping("/api/v1/admin/products/{productId}/variants")
@RequiredArgsConstructor
public class AdminProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    public ApiResponse<ProductResponse> create(@PathVariable Long productId,
                                               @Valid @RequestBody ProductVariantUpsertRequest request) {
        return ApiResponse.success("Variant created", productVariantService.addVariant(productId, request));
    }

    @PutMapping("/{variantId}")
    public ApiResponse<ProductResponse> update(@PathVariable Long productId,
                                               @PathVariable Long variantId,
                                               @Valid @RequestBody ProductVariantUpsertRequest request) {
        return ApiResponse.success("Variant updated", productVariantService.updateVariant(productId, variantId, request));
    }

    @DeleteMapping("/{variantId}")
    public ApiResponse<ProductResponse> delete(@PathVariable Long productId, @PathVariable Long variantId) {
        return ApiResponse.success("Variant deleted", productVariantService.deleteVariant(productId, variantId));
    }
}
