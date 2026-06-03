package com.ecommerce.dto.response;

import java.math.BigDecimal;

public record WarehouseStockResponse(
        Long warehouseId,
        String warehouseCode,
        String warehouseName,
        long totalProducts,
        long lowStockProducts,
        Integer totalStock,
        BigDecimal stockValue
) {
}
