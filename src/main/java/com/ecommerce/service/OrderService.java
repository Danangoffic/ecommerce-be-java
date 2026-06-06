package com.ecommerce.service;

import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.response.OrderItemResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.ProductVariant;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.exception.InvalidOrderStatusException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductVariantRepository;
import com.ecommerce.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PageUtils pageUtils;

    private static final Map<OrderStatus, EnumSet<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public PageResponse<OrderResponse> getCustomerOrders(Long userId, Integer page, Integer size) {
        Page<OrderResponse> result = orderRepository.findByUserId(userId, pageUtils.pageable(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponseSummary);
        return PageResponse.from(result);
    }

    public OrderResponse getCustomerOrderDetail(Long userId, Long orderId) {
        return orderRepository.findDetailedByIdAndUserId(orderId, userId)
                .map(this::toResponseDetail)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public PageResponse<OrderResponse> getAdminOrders(String status, String orderNumber, Integer page, Integer size) {
        OrderStatus parsedStatus = status == null || status.isBlank() ? null : OrderStatus.valueOf(status.toUpperCase());
        Page<OrderResponse> result = orderRepository.searchAdmin(
                        parsedStatus,
                        orderNumber == null || orderNumber.isBlank() ? null : orderNumber,
                        pageUtils.pageable(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponseSummary);
        return PageResponse.from(result);
    }

    public OrderResponse getAdminOrderDetail(Long orderId) {
        return orderRepository.findDetailedById(orderId)
                .map(this::toResponseDetail)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public Order getAdminOrderEntity(Long orderId) {
        return orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus nextStatus = OrderStatus.valueOf(request.status().toUpperCase());
        if (!ALLOWED_TRANSITIONS.get(order.getStatus()).contains(nextStatus)) {
            throw new InvalidOrderStatusException("Invalid order status transition");
        }
        if (nextStatus == OrderStatus.CANCELLED) {
            rollbackStock(order);
        }
        order.setStatus(nextStatus);
        return toResponseSummary(orderRepository.save(order));
    }

    private void rollbackStock(Order order) {
        List<Long> productIds = order.getItems().stream().map(OrderItem::getProductId).toList();
        Map<Long, com.ecommerce.entity.Product> productMap = productRepository.findAllByIdForUpdate(productIds)
                .stream().collect(Collectors.toMap(com.ecommerce.entity.Product::getId, p -> p));

        List<Long> variantIds = order.getItems().stream()
                .map(OrderItem::getVariantId).filter(Objects::nonNull).toList();
        Map<Long, ProductVariant> variantMap = variantIds.isEmpty() ? Collections.emptyMap()
                : productVariantRepository.findAllByIdForUpdate(variantIds).stream()
                        .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        for (OrderItem item : order.getItems()) {
            com.ecommerce.entity.Product product = productMap.get(item.getProductId());
            if (product == null) continue;
            if (item.getVariantId() != null) {
                ProductVariant variant = variantMap.get(item.getVariantId());
                if (variant != null) variant.setStock(variant.getStock() + item.getQuantity());
            }
            product.setStock(product.getStock() + item.getQuantity());
        }
    }

    OrderResponse toResponseDetail(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getShippingAddress(),
                order.getNotes(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProductId(),
                                item.getProductName(),
                                item.getVariantId(),
                                item.getVariantSku(),
                                item.getVariantLabel(),
                                item.getPrice(),
                                item.getQuantity(),
                                item.getSubtotal()))
                        .toList()
        );
    }

    private OrderResponse toResponseSummary(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getShippingAddress(),
                order.getNotes(),
                null
        );
    }
}
