package com.ecommerce.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Long variantId,
        String variantLabel,
        Integer quantity,
        BigDecimal priceSnapshot,
        BigDecimal subtotal
) {
}
