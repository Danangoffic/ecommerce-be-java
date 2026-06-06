package com.ecommerce.service;

import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.enums.CategoryStatus;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.repository.ProductImageRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductReviewRepository;
import com.ecommerce.repository.ProductVariantRepository;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private PageUtils pageUtils;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setStatus(CategoryStatus.ACTIVE);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCategory(testCategory);
        testProduct.setName("Smart TV");
        testProduct.setDescription("55-inch 4K Smart TV");
        testProduct.setPrice(new BigDecimal("499.99"));
        testProduct.setStock(10);
        testProduct.setStatus(ProductStatus.ACTIVE);

        pageRequest = PageRequest.of(0, 10);
    }

    @Test
    void listPublicProductsReturnsOnlyActiveProducts() {
        // Arrange
        Page<Product> productPage = new PageImpl<>(
                List.of(testProduct),
                pageRequest,
                1
        );
        when(productRepository.search(eq(true), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(productPage);
        when(pageUtils.pageable(eq(0), eq(10), any())).thenReturn(pageRequest);

        // Act
        PageResponse<ProductResponse> response = productService.listPublic(null, null, 0, 10, "name");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("Smart TV");
    }

    @Test
    void listPublicProductsByCategoryFilter() {
        // Arrange
        Page<Product> productPage = new PageImpl<>(
                List.of(testProduct),
                pageRequest,
                1
        );
        when(productRepository.search(eq(true), eq(1L), eq(null), any(Pageable.class)))
                .thenReturn(productPage);
        when(pageUtils.pageable(eq(0), eq(10), any())).thenReturn(pageRequest);

        // Act
        PageResponse<ProductResponse> response = productService.listPublic(1L, null, 0, 10, "name");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void listPublicProductsByKeywordSearch() {
        // Arrange
        Page<Product> productPage = new PageImpl<>(
                List.of(testProduct),
                pageRequest,
                1
        );
        when(productRepository.search(eq(true), eq(null), eq("Smart"), any(Pageable.class)))
                .thenReturn(productPage);
        when(pageUtils.pageable(eq(0), eq(10), any())).thenReturn(pageRequest);

        // Act
        PageResponse<ProductResponse> response = productService.listPublic(null, "Smart", 0, 10, "name");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void listAdminProductsReturnsAllStatuses() {
        // Arrange
        Page<Product> productPage = new PageImpl<>(
                List.of(testProduct),
                pageRequest,
                1
        );
        when(productRepository.search(eq(false), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(productPage);
        when(pageUtils.pageable(eq(0), eq(10), any())).thenReturn(pageRequest);

        // Act
        PageResponse<ProductResponse> response = productService.listAdmin(null, null, 0, 10, "name");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void listPublicWithWishlistCheckIncludesWishlistStatus() {
        // Arrange
        Page<Product> productPage = new PageImpl<>(
                List.of(testProduct),
                pageRequest,
                1
        );
        when(productRepository.search(eq(true), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(productPage);
        when(pageUtils.pageable(eq(0), eq(10), any())).thenReturn(pageRequest);

        // Act
        PageResponse<ProductResponse> response = productService.listPublic(null, null, 0, 10, "name", 1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void listPublicProductsReturnsEmptyPage() {
        // Arrange
        Page<Product> emptyPage = new PageImpl<>(
                List.of(),
                pageRequest,
                0
        );
        when(productRepository.search(eq(true), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(pageUtils.pageable(eq(0), eq(10), any())).thenReturn(pageRequest);

        // Act
        PageResponse<ProductResponse> response = productService.listPublic(null, null, 0, 10, "name");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
    }
}
