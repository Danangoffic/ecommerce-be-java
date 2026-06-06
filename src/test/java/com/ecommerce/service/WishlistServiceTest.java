package com.ecommerce.service;

import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.WishlistResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.Wishlist;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PageUtils pageUtils;

    @InjectMocks
    private WishlistService wishlistService;

    private User testUser;
    private Product testProduct;
    private Wishlist testWishlist;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("wishlister@example.com");
        testUser.setName("Wishlister");
        testUser.setRole(Role.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Smart TV");
        testProduct.setPrice(new BigDecimal("499.99"));
        testProduct.setStatus(ProductStatus.ACTIVE);

        testWishlist = new Wishlist();
        testWishlist.setId(1L);
        testWishlist.setUser(testUser);
        testWishlist.setProduct(testProduct);
        testWishlist.setCreatedAt(Instant.now());
    }

    @Test
    void addToWishlistSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findDetailedById(1L)).thenReturn(Optional.of(testProduct));
        when(wishlistRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);
        
        var mockProductResponse = new com.ecommerce.dto.response.ProductResponse(
                1L, "Smart TV", "Test", new BigDecimal("499.99"), 10, 5, true, false, 
                null, "ACTIVE", null, null, 0.0, 0L, false, false, null, null, null
        );
        when(productService.toResponse(testProduct)).thenReturn(mockProductResponse);

        // Act
        WishlistResponse response = wishlistService.add(1L, 1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.product().name()).isEqualTo("Smart TV");
    }

    @Test
    void addToWishlistThrowsExceptionWhenProductNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> wishlistService.add(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    void addToWishlistThrowsExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> wishlistService.add(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void addToWishlistThrowsExceptionWhenAlreadyExists() {
        // Arrange
        when(wishlistRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> wishlistService.add(1L, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Product is already in wishlist");
    }

    @Test
    void removeFromWishlistSuccessfully() {
        // Arrange
        when(wishlistRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testWishlist));

        // Act
        wishlistService.remove(1L, 1L);

        // Assert
        verify(wishlistRepository).delete(testWishlist);
    }

    @Test
    void removeFromWishlistThrowsExceptionWhenNotFound() {
        // Arrange
        when(wishlistRepository.findByUserIdAndProductId(1L, 999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> wishlistService.remove(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wishlist entry not found");
    }

    @Test
    void listWishlistReturnsPagedResults() {
        // Arrange
        Page<Wishlist> wishlistPage = new PageImpl<>(
                List.of(testWishlist),
                PageRequest.of(0, 10),
                1
        );
        when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
        when(wishlistRepository.findDetailedByUserId(1L, PageRequest.of(0, 10)))
                .thenReturn(wishlistPage);
        
        var mockProductResponse = new com.ecommerce.dto.response.ProductResponse(
                1L, "Smart TV", "Test", new BigDecimal("499.99"), 10, 5, true, false, 
                null, "ACTIVE", null, null, 0.0, 0L, false, false, null, null, null
        );
        when(productService.toResponse(testProduct)).thenReturn(mockProductResponse);

        // Act
        PageResponse<WishlistResponse> response = wishlistService.list(1L, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).product().name()).isEqualTo("Smart TV");
    }

    @Test
    void checkWishlistStatusReturnsTrueWhenExists() {
        // Arrange
        when(wishlistRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        // Act
        boolean exists = wishlistService.check(1L, 1L);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void checkWishlistStatusReturnsFalseWhenNotExists() {
        // Arrange
        when(wishlistRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);

        // Act
        boolean exists = wishlistService.check(1L, 1L);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void listWishlistReturnsEmptyWhenNoItems() {
        // Arrange
        Page<Wishlist> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );
        when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
        when(wishlistRepository.findDetailedByUserId(1L, PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        // Act
        PageResponse<WishlistResponse> response = wishlistService.list(1L, 0, 10);

        // Assert
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
    }

    @Test
    void listWishlistReturnsPaginationMetadata() {
        // Arrange
        Page<Wishlist> wishlistPage = new PageImpl<>(
                List.of(testWishlist),
                PageRequest.of(0, 10),
                15
        );
        when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
        when(wishlistRepository.findDetailedByUserId(1L, PageRequest.of(0, 10)))
                .thenReturn(wishlistPage);
        
        var mockProductResponse = new com.ecommerce.dto.response.ProductResponse(
                1L, "Smart TV", "Test", new BigDecimal("499.99"), 10, 5, true, false, 
                null, "ACTIVE", null, null, 0.0, 0L, false, false, null, null, null
        );
        when(productService.toResponse(testProduct)).thenReturn(mockProductResponse);

        // Act
        PageResponse<WishlistResponse> response = wishlistService.list(1L, 0, 10);

        // Assert
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(15);
        assertThat(response.first()).isTrue();
    }
}
