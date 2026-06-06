package com.ecommerce.dto.response;

import java.math.BigDecimal;

public record ProductVariantResponse(
        Long id,
        String sku,
        String size,
        String color,
        String label,
        BigDecimal price,
        Integer stock,
        boolean purchasable,
        String status
) {
}
