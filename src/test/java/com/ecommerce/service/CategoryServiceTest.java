package com.ecommerce.service;

import com.ecommerce.dto.request.CategoryUpsertRequest;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.enums.CategoryStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic products");
        testCategory.setStatus(CategoryStatus.ACTIVE);
    }

    @Test
    void getActiveCategoriesReturnsOnlyActiveCategories() {
        // Arrange
        when(categoryRepository.findAllByStatusOrderByNameAsc(CategoryStatus.ACTIVE))
                .thenReturn(List.of(testCategory));

        // Act
        List<CategoryResponse> categories = categoryService.getActiveCategories();

        // Assert
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).name()).isEqualTo("Electronics");
        verify(categoryRepository).findAllByStatusOrderByNameAsc(CategoryStatus.ACTIVE);
    }

    @Test
    void getActiveCategoriesReturnsEmptyList() {
        // Arrange
        when(categoryRepository.findAllByStatusOrderByNameAsc(CategoryStatus.ACTIVE))
                .thenReturn(List.of());

        // Act
        List<CategoryResponse> categories = categoryService.getActiveCategories();

        // Assert
        assertThat(categories).isEmpty();
    }

    @Test
    void createCategorySuccessfully() {
        // Arrange
        CategoryUpsertRequest request = new CategoryUpsertRequest("Books", "Book collection", "ACTIVE");
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        CategoryResponse response = categoryService.create(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategorySuccessfully() {
        // Arrange
        CategoryUpsertRequest request = new CategoryUpsertRequest("Updated Electronics", "Updated description", "ACTIVE");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        CategoryResponse response = categoryService.update(1L, request);

        // Assert
        assertThat(response).isNotNull();
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategoryThrowsExceptionWhenNotFound() {
        // Arrange
        CategoryUpsertRequest request = new CategoryUpsertRequest("Test", "Test", "ACTIVE");
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivateCategorySuccessfully() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        CategoryResponse response = categoryService.deactivate(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deactivateCategoryThrowsExceptionWhenNotFound() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deactivate(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
