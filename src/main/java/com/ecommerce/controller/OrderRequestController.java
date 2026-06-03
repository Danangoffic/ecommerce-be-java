package com.ecommerce.controller;

import com.ecommerce.dto.request.OrderRequestCreateRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderRequestResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.service.OrderRequestService;
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
@RequestMapping("/api/v1/orders/requests")
@RequiredArgsConstructor
public class OrderRequestController {

    private final OrderRequestService orderRequestService;

    @GetMapping
    public ApiResponse<PageResponse<OrderRequestResponse>> list(@AuthenticationPrincipal AuthenticatedUser user,
                                                                @RequestParam(required = false) Integer page,
                                                                @RequestParam(required = false) Integer size) {
        return ApiResponse.success("Order requests fetched", orderRequestService.listMine(user.getId(), page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderRequestResponse> detail(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long id) {
        return ApiResponse.success("Order request fetched", orderRequestService.getMine(user.getId(), id));
    }

    @PostMapping
    public ApiResponse<OrderRequestResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @Valid @RequestBody OrderRequestCreateRequest request) {
        return ApiResponse.success("Order request created", orderRequestService.create(user.getId(), request));
    }
}
