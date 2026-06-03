package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record LatestOrderResponse(
        Long id,
        String orderNumber,
        String status,
        BigDecimal totalAmount,
        Instant createdAt
) {
}
