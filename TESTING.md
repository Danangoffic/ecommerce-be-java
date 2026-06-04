# Testing Guide

Panduan untuk membuat dan menjalankan unit tests di project ini.

## Current Testing Setup

### Integration Tests
**File**: `src/test/java/com/ecommerce/EcommerceApplicationTests.java`

Menggunakan `@SpringBootTest` untuk end-to-end testing:
- Bekerja dengan database H2 in-memory
- Test business logic melalui HTTP layer (MockMvc)
- Test scenarios lengkap: auth, checkout, wishlist, reviews

**Jalankan:**
```bash
mvn test
```

**7 test cases:**
1. `registerLoginAndMeFlowWorks()` - Auth flow
2. `publicProductListOnlyShowsActiveProducts()` - Product filtering
3. `customerCanCheckoutAndAdminCanAdvanceStatus()` - Checkout & order management
4. `customerCannotAccessAdminReport()` - Authorization
5. `wishlistLifecycleWorks()` - Wishlist add/remove/check
6. `productReviewVerifiedPurchaseFlowWorks()` - Reviews dengan verified purchase
7. `oauth2SuccessHandlerFlowWorks()` - OAuth2 integration

## How to Create Service-Level Unit Tests

Jika ingin menambah unit tests untuk service tertentu, ikuti pola ini:

### Step 1: Create Test File

```
src/test/java/com/ecommerce/service/[ServiceName]Test.java
```

Contoh: `WishlistServiceTest.java`, `CartServiceTest.java`

### Step 2: Setup Test Class

```java
@ExtendWith(MockitoExtension.class)
class [ServiceName]Test {
    
    @Mock
    private DependencyA dependencyA;
    
    @Mock
    private DependencyB dependencyB;
    
    private [ServiceName] service;
    
    @BeforeEach
    void setUp() {
        service = new [ServiceName](dependencyA, dependencyB);
    }
}
```

### Step 3: Organize Tests with @Nested

```java
@Nested
class MethodName {
    
    @Test
    void shouldDoSomething() {
        // Given
        
        // When
        
        // Then
    }
}
```

### Step 4: Example - WishlistService Unit Test

```java
package com.ecommerce.service;

import com.ecommerce.entity.Wishlist;
import com.ecommerce.entity.User;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    private WishlistService service;

    @BeforeEach
    void setUp() {
        service = new WishlistService(
            wishlistRepository, userRepository, productRepository, productService, null
        );
    }

    @Nested
    class Check {

        @Test
        void shouldReturnTrueWhenProductInWishlist() {
            // Given
            Long userId = 1L;
            Long productId = 2L;
            given(wishlistRepository.existsByUserIdAndProductId(userId, productId))
                .willReturn(true);

            // When
            boolean result = service.check(userId, productId);

            // Then
            assertThat(result).isTrue();
            verify(wishlistRepository).existsByUserIdAndProductId(userId, productId);
        }

        @Test
        void shouldReturnFalseWhenProductNotInWishlist() {
            // Given
            Long userId = 1L;
            Long productId = 2L;
            given(wishlistRepository.existsByUserIdAndProductId(userId, productId))
                .willReturn(false);

            // When
            boolean result = service.check(userId, productId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class Remove {

        @Test
        void shouldThrowResourceNotFoundExceptionWhenWishlistEntryNotFound() {
            // Given
            Long userId = 1L;
            Long productId = 2L;
            given(wishlistRepository.findByUserIdAndProductId(userId, productId))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.remove(userId, productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wishlist entry not found");
            verify(wishlistRepository, org.mockito.Mockito.never()).delete(any());
        }
    }
}
```

## Best Practices

1. **Use Given-When-Then Pattern**
   ```java
   // Given - Setup test data
   // When - Call the method
   // Then - Assert results
   ```

2. **Mock External Dependencies**
   - Use `@Mock` for dependencies
   - Use `given()` for stubbing behavior
   - Verify interactions dengan `verify()`

3. **Organize with @Nested**
   - Group related tests
   - Better readability
   - Clear test structure

4. **Test Error Paths**
   - Test both happy path dan error cases
   - Use `assertThatThrownBy()` untuk exceptions

5. **Keep Tests Independent**
   - Each test should stand alone
   - Use `@BeforeEach` untuk setup
   - No shared state between tests

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EcommerceApplicationTests

# Run specific test method
mvn test -Dtest=EcommerceApplicationTests#registerLoginAndMeFlowWorks

# Skip tests during build
mvn clean package -DskipTests
```

## Test Results

Test results akan tersimpan di:
- Surefire reports: `target/surefire-reports/`
- Test output: Console output

## CI/CD Integration

GitHub Actions workflow di `.github/workflows/ci.yml` akan:
1. Compile code
2. Run `mvn test`
3. Upload test results sebagai artifact
4. Fail jika ada test yang gagal

## Notes

- **EcommerceApplicationTests** adalah integration test utama
- Mencakup complete user flows (checkout, wishlist, reviews, auth)
- Lebih baik untuk validasi end-to-end behavior
- Service-level unit tests bagus untuk isolated logic testing