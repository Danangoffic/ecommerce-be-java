package com.ecommerce.service;

import com.ecommerce.dto.request.ProductReviewRequest;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductReviewResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductReview;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductReviewRepository;
import com.ecommerce.repository.UserRepository;
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
class ProductReviewServiceTest {

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PageUtils pageUtils;

    @InjectMocks
    private ProductReviewService productReviewService;

    private User testUser;
    private Product testProduct;
    private ProductReview testReview;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("reviewer@example.com");
        testUser.setName("Reviewer");
        testUser.setRole(Role.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Clean Architecture");
        testProduct.setPrice(new BigDecimal("45.00"));
        testProduct.setStatus(ProductStatus.ACTIVE);

        testReview = new ProductReview();
        testReview.setId(1L);
        testReview.setProduct(testProduct);
        testReview.setUser(testUser);
        testReview.setRating(5);
        testReview.setComment("Must read for developers!");
        testReview.setCreatedAt(Instant.now());
    }

    @Test
    void addReviewSuccessfullyAfterPurchase() {
        // Arrange
        ProductReviewRequest request = new ProductReviewRequest(1L, 5, "Must read for developers!");
        when(productReviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(orderRepository.hasCompletedOrderWithProduct(1L, 1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findDetailedById(1L)).thenReturn(Optional.of(testProduct));
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testReview);

        // Act
        ProductReviewResponse response = productReviewService.addReview(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.comment()).isEqualTo("Must read for developers!");
        verify(productReviewRepository).save(any(ProductReview.class));
    }

    @Test
    void addReviewThrowsExceptionWhenProductNotFound() {
        // Arrange
        ProductReviewRequest request = new ProductReviewRequest(999L, 5, "Great!");
        when(productReviewRepository.existsByUserIdAndProductId(1L, 999L)).thenReturn(false);
        when(orderRepository.hasCompletedOrderWithProduct(1L, 999L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productReviewService.addReview(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    void addReviewThrowsExceptionWhenNoPurchaseHistory() {
        // Arrange
        ProductReviewRequest request = new ProductReviewRequest(1L, 5, "Great!");
        when(productReviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(orderRepository.hasCompletedOrderWithProduct(1L, 1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productReviewService.addReview(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You can only review products you have purchased and completed the order");
    }

    @Test
    void addReviewThrowsExceptionWhenDuplicateReview() {
        // Arrange
        ProductReviewRequest request = new ProductReviewRequest(1L, 5, "Must read!");
        when(productReviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productReviewService.addReview(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("You have already reviewed this product");
    }

    @Test
    void getProductReviewsReturnsPagedResults() {
        // Arrange
        Page<ProductReview> reviewPage = new PageImpl<>(
                List.of(testReview),
                PageRequest.of(0, 10),
                1
        );
        when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
        when(productReviewRepository.findDetailedByProductId(1L, PageRequest.of(0, 10)))
                .thenReturn(reviewPage);

        // Act
        PageResponse<ProductReviewResponse> response = productReviewService.getProductReviews(1L, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).comment()).isEqualTo("Must read for developers!");
    }

    @Test
    void getUserReviewsReturnsPagedResults() {
        // Arrange
        Page<ProductReview> reviewPage = new PageImpl<>(
                List.of(testReview),
                PageRequest.of(0, 10),
                1
        );
        when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
        when(productReviewRepository.findDetailedByUserId(1L, PageRequest.of(0, 10)))
                .thenReturn(reviewPage);

        // Act
        PageResponse<ProductReviewResponse> response = productReviewService.getUserReviews(1L, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).rating()).isEqualTo(5);
    }

    @Test
    void searchAdminReviewsReturnsFilteredResults() {
        // Arrange
        Page<ProductReview> reviewPage = new PageImpl<>(
                List.of(testReview),
                PageRequest.of(0, 10),
                1
        );
        when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
        when(productReviewRepository.searchAdmin(5, "Must", PageRequest.of(0, 10)))
                .thenReturn(reviewPage);

        // Act
        PageResponse<ProductReviewResponse> response = productReviewService.searchAdminReviews(5, "Must", 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void deleteReviewSuccessfully() {
        // Arrange
        when(productReviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // Act
        productReviewService.deleteReview(1L);

        // Assert
        verify(productReviewRepository).delete(testReview);
    }

    @Test
    void deleteReviewThrowsExceptionWhenNotFound() {
        // Arrange
        when(productReviewRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productReviewService.deleteReview(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Review not found");
    }

    @Test
    void getProductReviewsReturnsEmptyWhenNoReviews() {
        // Arrange
        Page<ProductReview> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );
        when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
        when(productReviewRepository.findDetailedByProductId(1L, PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        // Act
        PageResponse<ProductReviewResponse> response = productReviewService.getProductReviews(1L, 0, 10);

        // Assert
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
    }
}
