package com.ecommerce.dto.response;

import java.time.Instant;

public record ProductReviewResponse(
        Long id,
        ProductResponse product,
        Long userId,
        String userName,
        Integer rating,
        String comment,
        Instant createdAt
) {
}
