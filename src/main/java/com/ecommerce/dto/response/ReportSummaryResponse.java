package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReportSummaryResponse(
        long totalProducts,
        long totalActiveProducts,
        long totalOrders,
        BigDecimal totalSalesAmount,
        long totalCustomers,
        List<LatestOrderResponse> latestOrders
) {
}
