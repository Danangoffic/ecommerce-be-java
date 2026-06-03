package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        BigDecimal totalAmount,
        String status,
        Instant orderDate,
        String recipientName,
        String recipientPhone,
        String shippingAddress,
        String notes,
        List<OrderItemResponse> items
) {
}
