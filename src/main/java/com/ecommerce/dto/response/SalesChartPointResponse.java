package com.ecommerce.dto.response;

import java.math.BigDecimal;

public record SalesChartPointResponse(
        String label,
        BigDecimal totalSales,
        long totalOrders
) {
}
