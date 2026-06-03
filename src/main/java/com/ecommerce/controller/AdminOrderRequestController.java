package com.ecommerce.controller;

import com.ecommerce.dto.request.ResolveOrderRequestRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/order-requests")
@RequiredArgsConstructor
public class AdminOrderRequestController {

    private final OrderRequestService orderRequestService;

    @GetMapping
    public ApiResponse<PageResponse<OrderRequestResponse>> list(@RequestParam(required = false) String status,
                                                                @RequestParam(required = false) String type,
                                                                @RequestParam(required = false) Integer page,
                                                                @RequestParam(required = false) Integer size) {
        return ApiResponse.success("Order requests fetched", orderRequestService.listAll(status, type, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderRequestResponse> resolve(@AuthenticationPrincipal AuthenticatedUser user,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody ResolveOrderRequestRequest request) {
        return ApiResponse.success("Order request updated", orderRequestService.resolve(id, request, user.getId()));
    }
}
