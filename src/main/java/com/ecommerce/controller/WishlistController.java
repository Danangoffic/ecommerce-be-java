package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.WishlistResponse;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/products/{productId}")
    public ApiResponse<WishlistResponse> add(@AuthenticationPrincipal AuthenticatedUser user,
                                             @PathVariable Long productId) {
        return ApiResponse.success("Product added to wishlist", wishlistService.add(user.getId(), productId));
    }

    @DeleteMapping("/products/{productId}")
    public ApiResponse<Void> remove(@AuthenticationPrincipal AuthenticatedUser user,
                                    @PathVariable Long productId) {
        wishlistService.remove(user.getId(), productId);
        return ApiResponse.success("Product removed from wishlist", null);
    }

    @GetMapping
    public ApiResponse<PageResponse<WishlistResponse>> list(@AuthenticationPrincipal AuthenticatedUser user,
                                                            @RequestParam(required = false) Integer page,
                                                            @RequestParam(required = false) Integer size) {
        return ApiResponse.success("Wishlist fetched", wishlistService.list(user.getId(), page, size));
    }

    @GetMapping("/products/{productId}/check")
    public ApiResponse<Map<String, Boolean>> check(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long productId) {
        boolean exists = wishlistService.check(user.getId(), productId);
        return ApiResponse.success("Checked wishlist status", Map.of("wishlisted", exists));
    }
}
