# Unit Tests Refactoring - Complete Guide

## Overview

The ecommerce project's integration tests have been systematically refactored into isolated unit tests for each service layer. This document guides you through the structure and how to use/extend them.

## What Was Done

### ✅ Created Unit Tests

The following service unit tests have been created and are compiling successfully:

1. **AuthServiceTest.java** - 7 test cases
   - User registration (success & duplicate email)
   - User login
   - OAuth2 login scenarios
   - Current user profile retrieval

2. **CartServiceTest.java** - 8 test cases
   - Get/create cart
   - Add items (success & error cases)
   - Update cart item quantity
   - Remove items
   - Clear cart
   - Checkout validation

3. **CategoryServiceTest.java** - 6 test cases
   - List active categories
   - Create category
   - Update category
   - Deactivate category
   - Error handling

### 📋 Documentation Created

- **SERVICE_UNIT_TESTS_SUMMARY.md** - Comprehensive conversion overview
- **SERVICE_TEST_GENERATION_GUIDE.md** - Templates and patterns for remaining tests
- **UNIT_TESTS_README.md** - This file

## Quick Start

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test file
mvn test -Dtest=AuthServiceTest

# Run specific test method
mvn test -Dtest=AuthServiceTest#registerSuccessfully

# Run with coverage
mvn clean test jacoco:report
# View report: target/site/jacoco/index.html
```

### Project Structure

```
src/test/java/com/ecommerce/service/
├── AuthServiceTest.java          ✅ Complete
├── CartServiceTest.java          ✅ Complete
├── CategoryServiceTest.java      ✅ Complete
├── ProductServiceTest.java       (Template provided)
├── ProductReviewServiceTest.java (Template provided)
├── OrderServiceTest.java         (Template provided)
├── CheckoutServiceTest.java      (Template provided)
└── WishlistServiceTest.java      (Template provided)

src/test/java/com/ecommerce/
└── EcommerceApplicationTests.java (Original integration tests - preserved)
```

## Integration vs Unit Tests

### Integration Tests (Preserved)
- **File**: `EcommerceApplicationTests.java`
- **Purpose**: Test complete workflows end-to-end
- **Examples**: Register → Login → Checkout flow, OAuth2 handler
- **Run time**: ~2-3 seconds
- **Requires**: Spring Boot context, H2 database

### Unit Tests (New)
- **Files**: `*ServiceTest.java` in service directory
- **Purpose**: Test individual service methods in isolation
- **Examples**: AuthService.register, CartService.addItem
- **Run time**: ~50-100ms per test
- **Dependencies**: Mocked repositories only

## Creating Remaining Tests

The following tests need to be implemented. Use the templates in `SERVICE_TEST_GENERATION_GUIDE.md`:

### 1. ProductServiceTest.java
Key methods to test:
- `listPublic()` - Basic and with category/keyword filters
- `listAdmin()` - For admin view
- `getPublicDetail()` - Single product retrieval
- Error cases (product not found, inactive product)

### 2. ProductReviewServiceTest.java
Key methods to test:
- `addReview()` - Verified purchase check
- `getProductReviews()` - Pagination
- `getUserReviews()` - User's own reviews
- `searchAdminReviews()` - Admin search with filters
- `deleteReview()` - Admin deletion

### 3. OrderServiceTest.java
Key methods to test:
- `getCustomerOrders()` - List with pagination
- `getCustomerOrderDetail()` - Single order (with permission check)
- `getAdminOrders()` - Admin list with filters
- `getAdminOrderDetail()` - Admin order detail

### 4. CheckoutServiceTest.java
Key methods to test:
- `checkout()` - Main transaction
- Stock deduction validation
- Order number generation
- Cart clearing after checkout

### 5. WishlistServiceTest.java
Key methods to test:
- `add()` - Add to wishlist
- `remove()` - Remove from wishlist
- `list()` - Paginated wishlist
- `check()` - Check if in wishlist

## Key Testing Patterns

### Pattern 1: Basic Service Test
```java
@Test
void methodNameDoesExpectedBehavior() {
    // Arrange - Set up mocks
    when(repository.findById(1L)).thenReturn(Optional.of(entity));
    
    // Act - Call service method
    Response response = service.method(1L);
    
    // Assert - Verify result
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(1L);
    verify(repository).findById(1L);
}
```

### Pattern 2: Error Handling
```java
@Test
void methodThrowsExceptionWhenCondition() {
    // Arrange
    when(repository.findById(999L)).thenReturn(Optional.empty());
    
    // Act & Assert
    assertThatThrownBy(() -> service.method(999L))
        .isInstanceOf(ResourceNotFoundException.class);
}
```

### Pattern 3: Complex Operations (e.g., Checkout)
```java
@Test
void checkoutProcessesCompleteTransaction() {
    // Arrange - Set up entire scenario
    Cart cart = setupCart(products, quantities);
    when(cartService.getActiveCartForCheckout(1L)).thenReturn(cart);
    when(productRepository.findAllByIdForUpdate(any())).thenReturn(products);
    
    // Act
    CheckoutResponse response = checkoutService.checkout(1L, request);
    
    // Assert - Verify full transaction
    assertThat(response.orderId()).isNotNull();
    assertThat(response.orderNumber()).matches("^ORD-\\d{10}$");
    verify(orderRepository).save(any(Order.class));
}
```

## DTO Accessor Guide

**Important**: All DTOs are **records**, not classes. Use direct field access:

```java
// ✅ Correct (record fields)
response.id()
response.content()
response.name()
response.status()

// ❌ Wrong (no getters)
response.getId()
response.getContent()
response.getName()
response.getStatus()
```

## Common Mocking Setups

### Repository Methods
```java
when(repository.findById(1L)).thenReturn(Optional.of(entity));
when(repository.findAll()).thenReturn(List.of(entity1, entity2));
when(repository.save(any(Entity.class))).thenReturn(savedEntity);
when(repository.existsByEmail("test@test.com")).thenReturn(true);
```

### Pagination
```java
Page<Product> page = new PageImpl<>(
    List.of(entity1, entity2),
    PageRequest.of(0, 10),
    2  // totalElements
);
when(repository.findAll(PageRequest.of(0, 10))).thenReturn(page);
when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
```

### Optional Handling
```java
when(repository.findById(999L)).thenReturn(Optional.empty());
// Throws exception or returns default, depending on service logic
```

## Debugging Failed Tests

### Test Fails: "cannot find symbol" for DTO method
**Cause**: Using getter instead of record field accessor
**Fix**: Change `response.getId()` to `response.id()`

### Test Fails: "AssertionError: actual value: null"
**Cause**: Mock not properly set up
**Fix**: Add `when(...).thenReturn(...)` statement before calling service

### Test Fails: "verify() failed"
**Cause**: Expected method not called with expected arguments
**Fix**: Check method arguments in mock setup and verify call

### Test Fails: "InsufficientStockException"
**Cause**: Test product stock < requested quantity
**Fix**: Update test setup: `testProduct.setStock(requestedQuantity + 5)`

## Running Tests with Maven

```bash
# Compile tests
mvn test-compile

# Run all tests
mvn test

# Run with surefire plugin options
mvn test -Dtest=CartServiceTest -X

# Generate coverage report
mvn clean test jacoco:report

# Skip tests during build
mvn clean package -DskipTests

# Run tests in parallel (faster)
mvn test -Dtest.parallel=methods
```

## Coverage Goals

Target coverage for each service:

| Service | Target | Note |
|---------|--------|------|
| AuthService | 95%+ | Security critical |
| CartService | 85%+ | Core functionality |
| CategoryService | 80%+ | Relatively simple |
| ProductService | 75%+ | Complex with exports |
| ProductReviewService | 80%+ | Verified purchase logic |
| OrderService | 85%+ | Core business logic |
| CheckoutService | 90%+ | Transaction critical |
| WishlistService | 85%+ | User preferences |

## Best Practices Applied

✅ **Mockito for isolation** - Each service tested independently
✅ **Clear test names** - Describe what is being tested
✅ **Arrange-Act-Assert pattern** - Consistent structure
✅ **Focused assertions** - Test one behavior per method
✅ **Error path testing** - Both success and failure cases
✅ **Setup reusability** - @BeforeEach creates common test data
✅ **Mock verification** - Verify interactions with mocks

## Integration with CI/CD

These tests are ideal for:
- **Pre-commit hooks** - Fast feedback
- **CI/CD pipelines** - Parallel execution
- **Coverage reports** - Track code quality
- **Regression detection** - Early bug discovery

## Next Steps

1. **Implement remaining service tests** using templates in `SERVICE_TEST_GENERATION_GUIDE.md`
2. **Run coverage report** to identify gaps: `mvn clean test jacoco:report`
3. **Add to CI/CD** pipeline for automated testing
4. **Maintain integration tests** alongside unit tests
5. **Update when adding features** - Add corresponding unit tests

## Resources

- **Test Templates**: `.kiro/SERVICE_TEST_GENERATION_GUIDE.md`
- **Mockito Docs**: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **AssertJ Docs**: https://assertj.github.io/assertj-core-features-highlight.html
- **JUnit 5 Guide**: https://junit.org/junit5/docs/current/user-guide/

## Support

For questions about specific test implementations, refer to the completed test files:
- `AuthServiceTest.java` - Authentication patterns
- `CartServiceTest.java` - CRUD operations with validation
- `CategoryServiceTest.java` - Simple repository tests
