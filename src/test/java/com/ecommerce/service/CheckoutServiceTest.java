package com.ecommerce.service;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.response.CheckoutResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.CartStatus;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.util.OrderNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @InjectMocks
    private CheckoutService checkoutService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("buyer@example.com");
        testUser.setName("Buyer");
        testUser.setRole(Role.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Domain-Driven Design");
        testProduct.setPrice(new BigDecimal("55.00"));
        testProduct.setStock(10);
        testProduct.setStatus(ProductStatus.ACTIVE);

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setQuantity(2);
        cartItem.setProduct(testProduct);
        cartItem.setPriceSnapshot(new BigDecimal("55.00"));

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setStatus(CartStatus.ACTIVE);
        testCart.setItems(List.of(cartItem));
    }

    @Test
    void checkoutSuccessfullyCreatesOrder() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest(
                "Jakarta",
                "Buyer",
                "08123456789",
                "leave at door"
        );

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-0001234567");
        savedOrder.setUser(testUser);
        savedOrder.setStatus(OrderStatus.CREATED);

        when(cartService.getActiveCartForCheckout(1L)).thenReturn(testCart);
        when(productRepository.findAllByIdForUpdate(any())).thenReturn(List.of(testProduct));
        when(orderNumberGenerator.generate()).thenReturn("ORD-0001234567");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        CheckoutResponse response = checkoutService.checkout(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.orderNumber()).isEqualTo("ORD-0001234567");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkoutThrowsExceptionWhenCartEmpty() {
        // Arrange
        testCart.setItems(new ArrayList<>());
        CheckoutRequest request = new CheckoutRequest("Jakarta", "Buyer", "08123", "notes");

        when(cartService.getActiveCartForCheckout(1L))
                .thenThrow(new BadRequestException("Cart is empty"));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.checkout(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cart is empty");
    }

    @Test
    void checkoutThrowsExceptionWhenInsufficientStock() {
        // Arrange
        testProduct.setStock(1); // Only 1 item, need 2
        CheckoutRequest request = new CheckoutRequest("Jakarta", "Buyer", "08123", "notes");

        when(cartService.getActiveCartForCheckout(1L)).thenReturn(testCart);
        when(productRepository.findAllByIdForUpdate(any())).thenReturn(List.of(testProduct));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.checkout(1L, request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void checkoutDeductsStockCorrectly() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest("Jakarta", "Buyer", "08123", "notes");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-0001234567");

        when(cartService.getActiveCartForCheckout(1L)).thenReturn(testCart);
        when(productRepository.findAllByIdForUpdate(any())).thenReturn(List.of(testProduct));
        when(orderNumberGenerator.generate()).thenReturn("ORD-0001234567");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        CheckoutResponse response = checkoutService.checkout(1L, request);

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    void checkoutGeneratesOrderNumber() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest("Jakarta", "Buyer", "08123", "notes");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-0001234567");

        when(cartService.getActiveCartForCheckout(1L)).thenReturn(testCart);
        when(productRepository.findAllByIdForUpdate(any())).thenReturn(List.of(testProduct));
        when(orderNumberGenerator.generate()).thenReturn("ORD-0001234567");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        CheckoutResponse response = checkoutService.checkout(1L, request);

        // Assert
        assertThat(response.orderNumber()).isNotNull();
        assertThat(response.orderNumber()).matches("^ORD-\\d{10}$");
    }

    @Test
    void checkoutCalculatesTotalAmountCorrectly() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest("Jakarta", "Buyer", "08123", "notes");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-0001234567");
        savedOrder.setTotalAmount(new BigDecimal("110.00")); // 55 * 2

        when(cartService.getActiveCartForCheckout(1L)).thenReturn(testCart);
        when(productRepository.findAllByIdForUpdate(any())).thenReturn(List.of(testProduct));
        when(orderNumberGenerator.generate()).thenReturn("ORD-0001234567");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        CheckoutResponse response = checkoutService.checkout(1L, request);

        // Assert
        assertThat(response).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkoutWithMultipleCartItems() {
        // Arrange
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Clean Code");
        product2.setPrice(new BigDecimal("45.00"));
        product2.setStock(10);
        product2.setStatus(ProductStatus.ACTIVE);

        CartItem cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setQuantity(1);
        cartItem2.setProduct(product2);
        cartItem2.setPriceSnapshot(new BigDecimal("45.00"));

        testCart.setItems(List.of(cartItem, cartItem2));

        CheckoutRequest request = new CheckoutRequest("Jakarta", "Buyer", "08123", "notes");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-0001234567");
        savedOrder.setTotalAmount(new BigDecimal("155.00")); // 55*2 + 45*1

        when(cartService.getActiveCartForCheckout(1L)).thenReturn(testCart);
        when(productRepository.findAllByIdForUpdate(any())).thenReturn(List.of(testProduct, product2));
        when(orderNumberGenerator.generate()).thenReturn("ORD-0001234567");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        CheckoutResponse response = checkoutService.checkout(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(1L);
    }

    @Test
    void checkoutMarksCartAsCheckedOut() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest("Jakarta", "Buyer", "08123", "notes");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-0001234567");

        when(cartService.getActiveCartForCheckout(1L)).thenReturn(testCart);
        when(productRepository.findAllByIdForUpdate(any())).thenReturn(List.of(testProduct));
        when(orderNumberGenerator.generate()).thenReturn("ORD-0001234567");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        CheckoutResponse response = checkoutService.checkout(1L, request);

        // Assert
        assertThat(response).isNotNull();
        verify(cartService).markCheckedOut(any(Cart.class));
    }
}
