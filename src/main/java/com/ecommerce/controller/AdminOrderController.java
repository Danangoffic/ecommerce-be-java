package com.ecommerce.controller;

import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.service.InvoiceService;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> list(@RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String orderNumber,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size) {
        return ApiResponse.success("Orders fetched", orderService.getAdminOrders(status, orderNumber, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> detail(@PathVariable Long id) {
        return ApiResponse.success("Order fetched", orderService.getAdminOrderDetail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.success("Order status updated", orderService.updateStatus(id, request));
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> invoice(@PathVariable Long id) {
        var order = orderService.getAdminOrderEntity(id);
        byte[] bytes = invoiceService.generatePdf(order);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + order.getOrderNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
