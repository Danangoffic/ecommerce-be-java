package com.ecommerce.controller;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.CheckoutResponse;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ApiResponse<CheckoutResponse> checkout(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.success("Checkout success", checkoutService.checkout(user.getId(), request));
    }
}
