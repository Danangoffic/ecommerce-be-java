package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        Long userId = user != null ? user.getId() : null;
        return ApiResponse.success("Products fetched", productService.listPublic(categoryId, keyword, page, size, sort, userId));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductResponse>> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        Long userId = user != null ? user.getId() : null;
        return ApiResponse.success("Products fetched", productService.listPublic(categoryId, keyword, page, size, sort, userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> detail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        Long userId = user != null ? user.getId() : null;
        return ApiResponse.success("Product fetched", productService.getPublicDetail(id, userId));
    }
}
