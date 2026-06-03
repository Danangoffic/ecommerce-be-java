package com.ecommerce.controller;

import com.ecommerce.dto.request.AddCartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success("Cart fetched", cartService.getCart(user.getId()));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@AuthenticationPrincipal AuthenticatedUser user,
                                             @Valid @RequestBody AddCartItemRequest request) {
        return ApiResponse.success("Item added to cart", cartService.addItem(user.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartResponse> updateItem(@AuthenticationPrincipal AuthenticatedUser user,
                                                @PathVariable Long itemId,
                                                @Valid @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.success("Cart item updated", cartService.updateItem(user.getId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> removeItem(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long itemId) {
        return ApiResponse.success("Cart item removed", cartService.removeItem(user.getId(), itemId));
    }

    @DeleteMapping
    public ApiResponse<CartResponse> clear(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success("Cart cleared", cartService.clear(user.getId()));
    }
}
