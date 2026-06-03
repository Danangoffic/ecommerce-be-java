package com.ecommerce.service;

import com.ecommerce.dto.response.LatestOrderResponse;
import com.ecommerce.dto.response.LowStockProductResponse;
import com.ecommerce.dto.response.DashboardResponse;
import com.ecommerce.dto.response.ReportSummaryResponse;
import com.ecommerce.dto.response.SalesChartPointResponse;
import com.ecommerce.dto.response.StatusCountResponse;
import com.ecommerce.dto.response.WarehouseStockResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Warehouse;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReportSummaryResponse getSummary() {
        return new ReportSummaryResponse(
                productRepository.count(),
                productRepository.countByStatus(ProductStatus.ACTIVE),
                orderRepository.count(),
                zeroIfNull(orderRepository.sumValidSales()),
                userRepository.countByRole(Role.CUSTOMER),
                orderRepository.findTop5ByOrderByCreatedAtDesc().stream()
                        .map(order -> new LatestOrderResponse(
                                order.getId(),
                                order.getOrderNumber(),
                                order.getStatus().name(),
                                order.getTotalAmount(),
                                order.getCreatedAt()
                        ))
                        .toList()
        );
    }

    public DashboardResponse getDashboard(Integer days) {
        int resolvedDays = days == null || days < 1 ? 30 : Math.min(days, 365);
        Instant to = Instant.now();
        Instant from = to.minus(resolvedDays - 1L, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        List<Order> ordersInRange = orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(from, to);
        List<Product> products = productRepository.findAllDetailed();
        List<LatestOrderResponse> latestOrders = orderRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(order -> new LatestOrderResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getStatus().name(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                ))
                .toList();
        return new DashboardResponse(
                getSummary(),
                buildSalesSeries(ordersInRange, from, resolvedDays),
                buildStatusBreakdown(ordersInRange),
                buildLowStockProducts(products),
                buildWarehouseStock(products),
                latestOrders
        );
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private List<SalesChartPointResponse> buildSalesSeries(List<Order> orders, Instant from, int days) {
        Map<LocalDate, SalesBucket> buckets = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            buckets.put(from.atZone(ZoneOffset.UTC).toLocalDate().plusDays(i), new SalesBucket());
        }
        orders.forEach(order -> {
            LocalDate day = order.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
            SalesBucket bucket = buckets.computeIfAbsent(day, ignored -> new SalesBucket());
            if (order.getStatus() != com.ecommerce.entity.enums.OrderStatus.CANCELLED) {
                bucket.totalSales = bucket.totalSales.add(order.getTotalAmount());
            }
            bucket.totalOrders++;
        });
        return buckets.entrySet().stream()
                .map(entry -> new SalesChartPointResponse(
                        entry.getKey().toString(),
                        entry.getValue().totalSales,
                        entry.getValue().totalOrders
                ))
                .toList();
    }

    private List<StatusCountResponse> buildStatusBreakdown(List<Order> orders) {
        Map<String, Long> counts = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getStatus().name(), TreeMap::new, Collectors.counting()));
        return counts.entrySet().stream()
                .map(entry -> new StatusCountResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<LowStockProductResponse> buildLowStockProducts(List<Product> products) {
        return products.stream()
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                .filter(product -> product.getMinimumStockLevel() != null && product.getStock() <= product.getMinimumStockLevel())
                .sorted(Comparator.comparing(Product::getStock).thenComparing(Product::getId))
                .map(product -> new LowStockProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getStock(),
                        product.getMinimumStockLevel(),
                        product.getWarehouse() == null ? null : product.getWarehouse().getName(),
                        product.getPrice()
                ))
                .toList();
    }

    private List<WarehouseStockResponse> buildWarehouseStock(List<Product> products) {
        Map<Long, List<Product>> grouped = products.stream()
                .filter(product -> product.getWarehouse() != null)
                .collect(Collectors.groupingBy(product -> product.getWarehouse().getId(), TreeMap::new, Collectors.toList()));
        return grouped.values().stream()
                .map(list -> {
                    Warehouse warehouse = list.get(0).getWarehouse();
                    int totalStock = list.stream().mapToInt(Product::getStock).sum();
                    long lowStock = list.stream()
                            .filter(product -> product.getMinimumStockLevel() != null && product.getStock() <= product.getMinimumStockLevel())
                            .count();
                    BigDecimal stockValue = list.stream()
                            .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getStock())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new WarehouseStockResponse(
                            warehouse.getId(),
                            warehouse.getCode(),
                            warehouse.getName(),
                            list.size(),
                            lowStock,
                            totalStock,
                            stockValue
                    );
                })
                .toList();
    }

    private static class SalesBucket {
        private BigDecimal totalSales = BigDecimal.ZERO;
        private long totalOrders = 0;
    }
}
