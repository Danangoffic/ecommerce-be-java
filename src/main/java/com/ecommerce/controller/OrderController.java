package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> list(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size) {
        return ApiResponse.success("Orders fetched", orderService.getCustomerOrders(user.getId(), page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> detail(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.success("Order fetched", orderService.getCustomerOrderDetail(user.getId(), id));
    }
}
