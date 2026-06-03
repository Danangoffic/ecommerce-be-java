package com.ecommerce.dto.response;

import java.util.List;

public record DashboardResponse(
        ReportSummaryResponse summary,
        List<SalesChartPointResponse> salesByDay,
        List<StatusCountResponse> ordersByStatus,
        List<LowStockProductResponse> lowStockProducts,
        List<WarehouseStockResponse> warehouseStock,
        List<LatestOrderResponse> latestOrders
) {
}
