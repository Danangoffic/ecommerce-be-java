package com.ecommerce.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceSnapshot,
        BigDecimal subtotal
) {
}
