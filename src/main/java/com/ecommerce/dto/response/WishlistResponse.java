package com.ecommerce.dto.response;

import java.time.Instant;

public record WishlistResponse(
        Long id,
        ProductResponse product,
        Instant createdAt
) {
}
