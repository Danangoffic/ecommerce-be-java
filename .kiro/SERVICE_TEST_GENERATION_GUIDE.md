# Service Unit Test Generation Guide

This guide provides templates and patterns for creating unit tests for each service.

## Quick Reference Table

| Service | Method | Return Type | Key Mocks |
|---------|--------|-------------|-----------|
| CartService | `addItem(userId, request)` | CartResponse | productService, cartRepository |
| CartService | `updateItem(userId, itemId, request)` | CartResponse | cartItemRepository |
| CartService | `removeItem(userId, itemId)` | CartResponse | cartRepository |
| CartService | `clear(userId)` | CartResponse | cartRepository |
| CategoryService | `create(request)` | CategoryResponse | categoryRepository |
| CategoryService | `update(id, request)` | CategoryResponse | categoryRepository |
| ProductService | `listPublic(...)` | PageResponse<ProductResponse> | productRepository, pageUtils |
| WishlistService | `add(userId, productId)` | WishlistResponse | wishlistRepository, productRepository |
| WishlistService | `remove(userId, productId)` | void | wishlistRepository |
| ProductReviewService | `addReview(userId, request)` | ProductReviewResponse | orderRepository, productReviewRepository |
| CheckoutService | `checkout(userId, request)` | CheckoutResponse | cartService, productRepository, orderRepository |
| OrderService | `getCustomerOrders(userId, ...)` | PageResponse<OrderResponse> | orderRepository, pageUtils |

## Pattern 1: Repository-Based Service Tests (CartService, ProductService)

```java
@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductService productService;
    @Mock private UserRepository userRepository;
    
    @InjectMocks private CartService cartService;
    
    private Cart testCart;
    private Product testProduct;
    
    @BeforeEach void setUp() {
        // Create test entities
        testCart = new Cart(); // Initialize fields
        testProduct = new Product(); // Initialize fields
    }
    
    @Test
    void addItemToCartSuccessfully() {
        // Arrange
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);
        when(cartRepository.findDetailedByUserIdAndStatus(1L, CartStatus.ACTIVE))
            .thenReturn(Optional.of(testCart));
        when(productService.getManagedProduct(1L)).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        
        // Act
        CartResponse response = cartService.addItem(1L, request);
        
        // Assert
        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }
}
```

## Pattern 2: Service-to-Service Tests (CheckoutService)

```java
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {
    @Mock private CartService cartService;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderNumberGenerator orderNumberGenerator;
    
    @InjectMocks private CheckoutService checkoutService;
    
    @Test
    void checkoutSuccessfullyCreatesOrder() {
        // Arrange
        Cart cart = createTestCart();
        CheckoutRequest request = new CheckoutRequest(...);
        when(cartService.getActiveCartForCheckout(1L)).thenReturn(cart);
        when(productRepository.findAllByIdForUpdate(any())).thenReturn(products);
        when(orderNumberGenerator.generate()).thenReturn("ORD-001");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        // Act
        CheckoutResponse response = checkoutService.checkout(1L, request);
        
        // Assert
        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.orderNumber()).isEqualTo("ORD-001");
    }
}
```

## Pattern 3: Data Validation Tests

```java
@Test
void addItemThrowsExceptionWhenInsufficientStock() {
    // Arrange
    testProduct.setStock(1); // Less than request quantity
    AddCartItemRequest request = new AddCartItemRequest(1L, 5);
    when(...).thenReturn(...);
    
    // Act & Assert
    assertThatThrownBy(() -> cartService.addItem(1L, request))
        .isInstanceOf(InsufficientStockException.class);
}
```

## Pattern 4: Pagination Tests (ProductService, OrderService)

```java
@Test
void listReturnsPagedResults() {
    // Arrange
    Page<Product> productPage = new PageImpl<>(
        List.of(testProduct),
        PageRequest.of(0, 10),
        1
    );
    when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
    when(productRepository.search(true, null, null, PageRequest.of(0, 10)))
        .thenReturn(productPage);
    
    // Act
    PageResponse<ProductResponse> response = productService.listPublic(null, null, 0, 10, "name");
    
    // Assert
    assertThat(response.content()).hasSize(1);
    assertThat(response.totalElements()).isEqualTo(1);
    assertThat(response.page()).isEqualTo(0);
}
```

## DTO Constructor Reference

### Request DTOs (Records)
```java
AddCartItemRequest request = new AddCartItemRequest(productId, quantity);
CheckoutRequest request = new CheckoutRequest(address, recipientName, phone, notes);
CategoryUpsertRequest request = new CategoryUpsertRequest(name, description, status);
ProductReviewRequest request = new ProductReviewRequest(productId, rating, comment);
```

### Response DTOs (Records)
```java
CartResponse response = new CartResponse(...); // Check actual constructor
OrderResponse response = new OrderResponse(...);
ProductResponse response = new ProductResponse(...);
PageResponse<T> response = new PageResponse<>(content, page, size, total, pages, first, last);
```

## Key Testing Assertions

```java
// Record field access (NOT getters)
assertThat(response.id()).isEqualTo(1L);
assertThat(response.content()).hasSize(1);
assertThat(response.content().get(0).name()).isEqualTo("Test");
assertThat(response.totalElements()).isEqualTo(1);

// Exception testing
assertThatThrownBy(() -> service.method(args))
    .isInstanceOf(CustomException.class)
    .hasMessage("Expected message");

// Mock verification
verify(repository).save(any());
verify(repository, times(2)).findById(any());
verify(repository, never()).delete(any());
```

## Common Mock Setup Patterns

```java
// Repository methods
when(repository.findById(1L)).thenReturn(Optional.of(entity));
when(repository.findAll()).thenReturn(List.of(entity1, entity2));
when(repository.save(any(Entity.class))).thenReturn(savedEntity);
when(repository.exists(...)).thenReturn(true/false);

// PageUtils
when(pageUtils.pageable(0, 10, null)).thenReturn(PageRequest.of(0, 10));
when(pageUtils.pageable(0, 10, Sort.by("name")))
    .thenReturn(PageRequest.of(0, 10, Sort.by("name")));

// Page responses
Page<Entity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
when(repository.findAll(PageRequest.of(0, 10))).thenReturn(page);
```

## Running and Debugging

```bash
# Run specific test
mvn test -Dtest=CartServiceTest#addItemSuccessfully

# Run with debug output
mvn test -X

# Run with Mockito debug
mvn test -Dmaven.surefire.debug

# Generate coverage report
mvn test jacoco:report
```

## Common Issues and Fixes

### Issue: "cannot find symbol" for DTO accessor
**Fix**: DTOs are records, use `response.fieldName()` not `response.getFieldName()`

### Issue: PageResponse is null or getContent fails
**Fix**: PageResponse is a record with `content` field, use `response.content()` not `response.getContent()`

### Issue: Mock not returning expected type
**Fix**: Check return type signature, e.g., `when(repo.save(any())).thenReturn(entityNotDto)`

### Issue: Transactional tests fail
**Fix**: Don't mock `@Transactional` methods, test the actual transaction behavior

## Test Coverage Goals

- **CartService**: 80%+ coverage
- **CategoryService**: 85%+ coverage  
- **ProductService**: 75%+ coverage (exclude complex export/import methods)
- **CheckoutService**: 90%+ coverage (critical path)
- **OrderService**: 85%+ coverage
- **ProductReviewService**: 80%+ coverage
- **WishlistService**: 85%+ coverage
- **AuthService**: 90%+ coverage (security critical)

## Adding New Tests

When adding new service methods:
1. Create positive test case (method works as expected)
2. Create negative test cases (each validation/error path)
3. Create edge case tests (empty results, null handling)
4. Add mock setup documentation in class comments
5. Run coverage report to confirm >80% coverage
