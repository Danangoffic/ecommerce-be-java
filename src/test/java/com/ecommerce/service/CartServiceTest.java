package com.ecommerce.service;

import com.ecommerce.dto.request.AddCartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductVariant;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.CartStatus;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductVariantRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setStatus(CartStatus.ACTIVE);
        testCart.setItems(new ArrayList<>());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setStock(10);
        testProduct.setStatus(ProductStatus.ACTIVE);
    }

    @Test
    void getCartReturnsActiveCart() {
        // Arrange
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        // Act
        CartResponse response = cartService.getCart(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(cartRepository).findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE);
    }

    @Test
    void getCartCreatesNewCartIfNotExists() {
        // Arrange
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.getCart(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemToCartSuccessfully() {
        // Arrange
        AddCartItemRequest request = new AddCartItemRequest(1L, null, 2);
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productService.getManagedProduct(1L)).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.addItem(1L, request);

        // Assert
        assertThat(response).isNotNull();
        verify(productService).getManagedProduct(1L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemThrowsExceptionWhenProductNotPurchasable() {
        // Arrange
        testProduct.setStatus(ProductStatus.INACTIVE);
        AddCartItemRequest request = new AddCartItemRequest(1L, null, 2);
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productService.getManagedProduct(1L)).thenReturn(testProduct);

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void addItemThrowsExceptionWhenInsufficientStock() {
        // Arrange
        testProduct.setStock(1);
        AddCartItemRequest request = new AddCartItemRequest(1L, null, 5);
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productService.getManagedProduct(1L)).thenReturn(testProduct);

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void addItemRequiresVariantWhenProductHasVariants() {
        // Arrange
        AddCartItemRequest request = new AddCartItemRequest(1L, null, 1);
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productService.getManagedProduct(1L)).thenReturn(testProduct);
        when(productVariantRepository.countByProductId(1L)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void addItemWithVariantSuccessfully() {
        // Arrange
        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setProduct(testProduct);
        variant.setSku("SKU-L-RED");
        variant.setSize("L");
        variant.setColor("Red");
        variant.setStock(5);
        variant.setStatus(ProductStatus.ACTIVE);

        AddCartItemRequest request = new AddCartItemRequest(1L, 10L, 2);
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productService.getManagedProduct(1L)).thenReturn(testProduct);
        when(productVariantRepository.findByIdAndProductId(10L, 1L)).thenReturn(Optional.of(variant));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.addItem(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(testCart.getItems()).hasSize(1);
        assertThat(testCart.getItems().get(0).getVariant()).isEqualTo(variant);
        verify(productVariantRepository).findByIdAndProductId(10L, 1L);
    }

    @Test
    void addItemThrowsExceptionWhenVariantStockInsufficient() {
        // Arrange
        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setProduct(testProduct);
        variant.setSku("SKU-L-RED");
        variant.setStock(1);
        variant.setStatus(ProductStatus.ACTIVE);

        AddCartItemRequest request = new AddCartItemRequest(1L, 10L, 3);
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productService.getManagedProduct(1L)).thenReturn(testProduct);
        when(productVariantRepository.findByIdAndProductId(10L, 1L)).thenReturn(Optional.of(variant));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(InsufficientStockException.class);
    }


    @Test
    void removeItemFromCartSuccessfully() {
        // Arrange
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        testCart.getItems().add(cartItem);

        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.removeItem(1L, 1L);

        // Assert
        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void removeItemThrowsExceptionWhenNotFound() {
        // Arrange
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThatThrownBy(() -> cartService.removeItem(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void clearCartSuccessfully() {
        // Arrange
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        testCart.getItems().add(cartItem);

        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.clear(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getActiveCartForCheckoutThrowsExceptionWhenEmpty() {
        // Arrange
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThatThrownBy(() -> cartService.getActiveCartForCheckout(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cart is empty");
    }
}
