package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        String status,
        List<CartItemResponse> items,
        BigDecimal subtotal
) {
}
