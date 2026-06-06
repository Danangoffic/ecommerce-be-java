package com.ecommerce.service;

import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.InvalidOrderStatusException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductVariantRepository;
import com.ecommerce.util.PageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private PageUtils pageUtils;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Order testOrder;
    private Product testProduct;
    private OrderItem testOrderItem;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("buyer@example.com");
        testUser.setName("Buyer");
        testUser.setRole(Role.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);

        testProduct = new Product();
        testProduct.setId(10L);
        testProduct.setStock(5);

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setProductId(10L);
        testOrderItem.setProductName("Smart TV");
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(new BigDecimal("500.00"));
        testOrderItem.setSubtotal(new BigDecimal("1000.00"));

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("ORD-0001234567");
        testOrder.setRecipientName("Buyer");
        testOrder.setRecipientPhone("08123456789");
        testOrder.setShippingAddress("Jakarta");
        testOrder.setTotalAmount(new BigDecimal("1000.00"));
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder.setCreatedAt(Instant.now());
        testOrder.setItems(new ArrayList<>(List.of(testOrderItem)));

        pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());
    }

    @Test
    void getCustomerOrdersReturnsPagedResults() {
        // Arrange
        when(pageUtils.pageable(0, 10, Sort.by("createdAt").descending())).thenReturn(pageRequest);
        Page<Order> orderPage = new PageImpl<>(
                List.of(testOrder),
                pageRequest,
                1
        );
        when(orderRepository.findByUserId(1L, pageRequest))
                .thenReturn(orderPage);

        // Act
        PageResponse<OrderResponse> response = orderService.getCustomerOrders(1L, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).orderNumber()).isEqualTo("ORD-0001234567");
    }

    @Test
    void getCustomerOrderDetailReturnsOrder() {
        // Arrange
        when(orderRepository.findDetailedByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testOrder));

        // Act
        OrderResponse response = orderService.getCustomerOrderDetail(1L, 1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.orderNumber()).isEqualTo("ORD-0001234567");
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void getCustomerOrderDetailThrowsExceptionWhenNotFound() {
        // Arrange
        when(orderRepository.findDetailedByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getCustomerOrderDetail(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    void getCustomerOrderDetailThrowsExceptionWhenUserIdMismatch() {
        // Arrange
        when(orderRepository.findDetailedByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getCustomerOrderDetail(2L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    void getAdminOrdersReturnsPagedResults() {
        // Arrange
        when(pageUtils.pageable(0, 10, Sort.by("createdAt").descending())).thenReturn(pageRequest);
        Page<Order> orderPage = new PageImpl<>(
                List.of(testOrder),
                pageRequest,
                1
        );
        when(orderRepository.searchAdmin(null, null, pageRequest))
                .thenReturn(orderPage);

        // Act
        PageResponse<OrderResponse> response = orderService.getAdminOrders(null, null, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void getAdminOrdersFiltersByStatus() {
        // Arrange
        when(pageUtils.pageable(0, 10, Sort.by("createdAt").descending())).thenReturn(pageRequest);
        testOrder.setStatus(OrderStatus.PROCESSING);
        Page<Order> orderPage = new PageImpl<>(
                List.of(testOrder),
                pageRequest,
                1
        );
        when(orderRepository.searchAdmin(OrderStatus.PROCESSING, null, pageRequest))
                .thenReturn(orderPage);

        // Act
        PageResponse<OrderResponse> response = orderService.getAdminOrders("PROCESSING", null, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void getAdminOrdersFiltersByOrderNumber() {
        // Arrange
        when(pageUtils.pageable(0, 10, Sort.by("createdAt").descending())).thenReturn(pageRequest);
        Page<Order> orderPage = new PageImpl<>(
                List.of(testOrder),
                pageRequest,
                1
        );
        when(orderRepository.searchAdmin(null, "ORD-0001234567", pageRequest))
                .thenReturn(orderPage);

        // Act
        PageResponse<OrderResponse> response = orderService.getAdminOrders(null, "ORD-0001234567", 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content().get(0).orderNumber()).isEqualTo("ORD-0001234567");
    }

    @Test
    void getAdminOrderDetailReturnsOrder() {
        // Arrange
        when(orderRepository.findDetailedById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        OrderResponse response = orderService.getAdminOrderDetail(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.recipientName()).isEqualTo("Buyer");
    }

    @Test
    void getAdminOrderDetailThrowsExceptionWhenNotFound() {
        // Arrange
        when(orderRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getAdminOrderDetail(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    void getCustomerOrdersReturnsEmptyPage() {
        // Arrange
        when(pageUtils.pageable(0, 10, Sort.by("createdAt").descending())).thenReturn(pageRequest);
        Page<Order> emptyPage = new PageImpl<>(
                List.of(),
                pageRequest,
                0
        );
        when(orderRepository.findByUserId(1L, pageRequest))
                .thenReturn(emptyPage);

        // Act
        PageResponse<OrderResponse> response = orderService.getCustomerOrders(1L, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
    }

    @Test
    void getAdminOrdersWithBothFilters() {
        // Arrange
        when(pageUtils.pageable(0, 10, Sort.by("createdAt").descending())).thenReturn(pageRequest);
        testOrder.setStatus(OrderStatus.SHIPPED);
        Page<Order> orderPage = new PageImpl<>(
                List.of(testOrder),
                pageRequest,
                1
        );
        when(orderRepository.searchAdmin(OrderStatus.SHIPPED, "ORD-0001234567", pageRequest))
                .thenReturn(orderPage);

        // Act
        PageResponse<OrderResponse> response = orderService.getAdminOrders("SHIPPED", "ORD-0001234567", 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void updateStatusToCancelledRollsBackStock() {
        // Arrange
        when(orderRepository.findDetailedById(1L)).thenReturn(Optional.of(testOrder));
        when(productRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        orderService.updateStatus(1L, new UpdateOrderStatusRequest("CANCELLED"));

        // Assert — stok dikembalikan sebesar quantity item (2)
        assertThat(testProduct.getStock()).isEqualTo(7);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateStatusToProcessingDoesNotRollbackStock() {
        // Arrange
        when(orderRepository.findDetailedById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        orderService.updateStatus(1L, new UpdateOrderStatusRequest("PROCESSING"));

        // Assert — stok tidak disentuh
        assertThat(testProduct.getStock()).isEqualTo(5);
        verify(productRepository, never()).findAllByIdForUpdate(anyList());
    }

    @Test
    void updateStatusThrowsExceptionOnInvalidTransition() {
        // Arrange — SHIPPED tidak bisa ke CANCELLED
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findDetailedById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateStatus(1L, new UpdateOrderStatusRequest("CANCELLED")))
                .isInstanceOf(InvalidOrderStatusException.class);
    }
}
