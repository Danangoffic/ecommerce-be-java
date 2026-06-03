package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderRequestResponse(
        Long id,
        Long orderId,
        String orderNumber,
        String requestType,
        String status,
        String reason,
        String notes,
        BigDecimal requestedAmount,
        String adminNotes,
        Instant resolvedAt,
        Instant createdAt
) {
}
