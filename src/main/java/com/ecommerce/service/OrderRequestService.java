package com.ecommerce.service;

import com.ecommerce.dto.request.OrderRequestCreateRequest;
import com.ecommerce.dto.request.ResolveOrderRequestRequest;
import com.ecommerce.dto.response.OrderRequestResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderRequest;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductVariant;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.OrderRequestStatus;
import com.ecommerce.entity.enums.OrderRequestType;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderRequestRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductVariantRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderRequestService {

    private static final EnumSet<OrderStatus> CANCELLABLE = EnumSet.of(OrderStatus.CREATED, OrderStatus.PROCESSING);
    private static final EnumSet<OrderStatus> RETURNABLE = EnumSet.of(OrderStatus.SHIPPED, OrderStatus.COMPLETED);

    private final OrderRequestRepository orderRequestRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final PageUtils pageUtils;

    public PageResponse<OrderRequestResponse> listMine(Long userId, Integer page, Integer size) {
        List<OrderRequestResponse> requests = orderRequestRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.from(paginate(requests, pageUtils.pageable(page, size, Sort.by("createdAt").descending())));
    }

    public OrderRequestResponse getMine(Long userId, Long requestId) {
        return toResponse(orderRequestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order request not found")));
    }

    public PageResponse<OrderRequestResponse> listAll(String status, String type, Integer page, Integer size) {
        List<OrderRequest> requests;
        if (status != null && !status.isBlank()) {
            requests = orderRequestRepository.findByStatusOrderByCreatedAtDesc(parseStatus(status));
        } else if (type != null && !type.isBlank()) {
            requests = orderRequestRepository.findByTypeOrderByCreatedAtDesc(parseType(type));
        } else {
            requests = orderRequestRepository.findAll(Sort.by("createdAt").descending());
        }
        List<OrderRequestResponse> response = requests.stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.from(paginate(response, pageUtils.pageable(page, size, Sort.by("createdAt").descending())));
    }

    @Transactional
    public OrderRequestResponse create(Long userId, OrderRequestCreateRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to the authenticated user");
        }
        OrderRequestType type = parseType(request.type());
        validateOrderState(type, order.getStatus());
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrder(order);
        orderRequest.setUser(order.getUser());
        orderRequest.setType(type);
        orderRequest.setStatus(OrderRequestStatus.PENDING);
        orderRequest.setReason(request.reason());
        orderRequest.setNotes(request.notes());
        orderRequest.setRequestedAmount(request.requestedAmount() == null ? order.getTotalAmount() : request.requestedAmount());
        return toResponse(orderRequestRepository.save(orderRequest));
    }

    @Transactional
    public OrderRequestResponse resolve(Long requestId, ResolveOrderRequestRequest request, Long adminUserId) {
        OrderRequest orderRequest = orderRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Order request not found"));
        if (orderRequest.getStatus() != OrderRequestStatus.PENDING) {
            throw new BadRequestException("Order request has already been processed");
        }
        OrderRequestStatus nextStatus = parseStatus(request.status());
        if (nextStatus == OrderRequestStatus.REJECTED) {
            orderRequest.setStatus(nextStatus);
            orderRequest.setAdminNotes(request.adminNotes());
            orderRequest.setResolvedAt(Instant.now());
            orderRequest.setResolvedBy(getAdminUser(adminUserId));
            return toResponse(orderRequestRepository.save(orderRequest));
        }

        if (orderRequest.getType() == OrderRequestType.CANCEL && orderRequest.getOrder().getStatus() != OrderStatus.CANCELLED) {
            cancelOrder(orderRequest.getOrder());
        }

        orderRequest.setStatus(nextStatus);
        orderRequest.setAdminNotes(request.adminNotes());
        orderRequest.setResolvedAt(Instant.now());
        orderRequest.setResolvedBy(getAdminUser(adminUserId));
        return toResponse(orderRequestRepository.save(orderRequest));
    }

    private void cancelOrder(Order order) {
        if (!CANCELLABLE.contains(order.getStatus())) {
            throw new BadRequestException("Order can no longer be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);

        List<Long> productIds = order.getItems().stream().map(OrderItem::getProductId).toList();
        List<Product> lockedProducts = productRepository.findAllByIdForUpdate(productIds);
        Map<Long, Product> productMap = lockedProducts.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<Long> variantIds = order.getItems().stream()
                .map(OrderItem::getVariantId)
                .filter(java.util.Objects::nonNull)
                .toList();
        Map<Long, ProductVariant> variantMap = variantIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : productVariantRepository.findAllByIdForUpdate(variantIds).stream()
                        .collect(java.util.stream.Collectors.toMap(ProductVariant::getId, v -> v));

        for (OrderItem item : order.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) continue;
            if (item.getVariantId() != null) {
                ProductVariant variant = variantMap.get(item.getVariantId());
                if (variant != null) {
                    variant.setStock(variant.getStock() + item.getQuantity());
                }
            }
            product.setStock(product.getStock() + item.getQuantity());
        }

        orderRepository.save(order);
    }

    private void validateOrderState(OrderRequestType type, OrderStatus status) {
        if (type == OrderRequestType.CANCEL && !CANCELLABLE.contains(status)) {
            throw new BadRequestException("Order can no longer be cancelled");
        }
        if ((type == OrderRequestType.RETURN || type == OrderRequestType.REFUND) && !RETURNABLE.contains(status)) {
            throw new BadRequestException("Return or refund is only available after shipment or completion");
        }
    }

    private OrderRequestType parseType(String value) {
        try {
            return OrderRequestType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid request type");
        }
    }

    private OrderRequestStatus parseStatus(String value) {
        try {
            return OrderRequestStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid request status");
        }
    }

    private User getAdminUser(Long adminUserId) {
        return userRepository.findById(adminUserId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE && user.getRole() == Role.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
    }

    private OrderRequestResponse toResponse(OrderRequest orderRequest) {
        return new OrderRequestResponse(
                orderRequest.getId(),
                orderRequest.getOrder().getId(),
                orderRequest.getOrder().getOrderNumber(),
                orderRequest.getType().name(),
                orderRequest.getStatus().name(),
                orderRequest.getReason(),
                orderRequest.getNotes(),
                orderRequest.getRequestedAmount(),
                orderRequest.getAdminNotes(),
                orderRequest.getResolvedAt(),
                orderRequest.getCreatedAt()
        );
    }

    private <T> Page<T> paginate(List<T> items, org.springframework.data.domain.Pageable pageable) {
        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), items.size());
        List<T> content = fromIndex >= items.size() ? List.of() : items.subList(fromIndex, toIndex);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, items.size());
    }
}
