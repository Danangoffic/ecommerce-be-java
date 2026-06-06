package com.ecommerce.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Long variantId,
        String variantSku,
        String variantLabel,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {
}
