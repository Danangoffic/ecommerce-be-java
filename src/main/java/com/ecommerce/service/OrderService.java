package com.ecommerce.service;

import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.response.OrderItemResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.exception.InvalidOrderStatusException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus nextStatus = OrderStatus.valueOf(request.status().toUpperCase());
        if (!ALLOWED_TRANSITIONS.get(order.getStatus()).contains(nextStatus)) {
            throw new InvalidOrderStatusException("Invalid order status transition");
        }
        order.setStatus(nextStatus);
        return toResponseSummary(orderRepository.save(order));
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
