package com.ecommerce.dto.response;

import java.math.BigDecimal;

public record LowStockProductResponse(
        Long id,
        String name,
        Integer stock,
        Integer minimumStockLevel,
        String warehouseName,
        BigDecimal price
) {
}
