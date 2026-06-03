package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductReviewRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductReviewResponse;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @PostMapping("/reviews")
    public ApiResponse<ProductReviewResponse> addReview(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @Valid @RequestBody ProductReviewRequest request) {
        return ApiResponse.success("Review submitted successfully", productReviewService.addReview(user.getId(), request));
    }

    @GetMapping("/reviews/me")
    public ApiResponse<PageResponse<ProductReviewResponse>> getMyReviews(@AuthenticationPrincipal AuthenticatedUser user,
                                                                         @RequestParam(required = false) Integer page,
                                                                         @RequestParam(required = false) Integer size) {
        return ApiResponse.success("User reviews fetched", productReviewService.getUserReviews(user.getId(), page, size));
    }

    @GetMapping("/products/{productId}/reviews")
    public ApiResponse<PageResponse<ProductReviewResponse>> getProductReviews(@PathVariable Long productId,
                                                                              @RequestParam(required = false) Integer page,
                                                                              @RequestParam(required = false) Integer size) {
        return ApiResponse.success("Product reviews fetched", productReviewService.getProductReviews(productId, page, size));
    }
}
