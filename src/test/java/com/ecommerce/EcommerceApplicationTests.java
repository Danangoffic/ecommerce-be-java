package com.ecommerce;

import com.ecommerce.dto.request.AddCartItemRequest;
import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.ProductUpsertRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.CategoryStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.repository.ProductReviewRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Collections;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EcommerceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private com.ecommerce.security.OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    @Autowired
    private com.ecommerce.security.JwtService jwtService;

    @BeforeEach
    void setUp() {
        wishlistRepository.deleteAll();
        productReviewRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .toList();
        userRepository.deleteAll(users);
    }

    @Test
    void registerLoginAndMeFlowWorks() throws Exception {
        String registerPayload = objectMapper.writeValueAsString(new RegisterRequest(
                "Alice",
                "alice@example.com",
                "password123",
                "08123456789"
        ));

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value("alice@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = readToken(registerResponse);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));

        User savedUser = userRepository.findByEmailIgnoreCase("alice@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void publicProductListOnlyShowsActiveProducts() throws Exception {
        Category category = activeCategory("Electronics");
        createProductAsAdmin(category.getId(), "Active Phone", "ACTIVE", 5, new BigDecimal("1000.00"));
        createProductAsAdmin(category.getId(), "Hidden Phone", "INACTIVE", 5, new BigDecimal("1200.00"));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Active Phone"));
    }

    @Test
    void customerCanCheckoutAndAdminCanAdvanceStatus() throws Exception {
        Category category = activeCategory("Books");
        createProductAsAdmin(category.getId(), "Domain-Driven Design", "ACTIVE", 10, new BigDecimal("55.00"));

        Long productId = productRepository.findAll().get(0).getId();
        String customerToken = registerAndGetToken("buyer@example.com");

        mockMvc.perform(post("/api/v1/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCartItemRequest(productId, null, 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));

        String checkoutResponse = mockMvc.perform(post("/api/v1/checkout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckoutRequest(
                                "Jakarta",
                                "Buyer",
                                "08123",
                                "leave at door"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode checkoutJson = objectMapper.readTree(checkoutResponse);
        long orderId = checkoutJson.path("data").path("orderId").asLong();

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].productName").value("Domain-Driven Design"))
                .andExpect(jsonPath("$.data.totalAmount").value(110.00));

        String adminToken = loginAsAdmin();
        mockMvc.perform(put("/api/v1/admin/orders/" + orderId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest("PROCESSING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    void customerCannotAccessAdminReport() throws Exception {
        String customerToken = registerAndGetToken("viewer@example.com");

        mockMvc.perform(get("/api/v1/admin/reports/summary")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isForbidden());
    }

    private Category activeCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(name + " desc");
        category.setStatus(CategoryStatus.ACTIVE);
        return categoryRepository.save(category);
    }

    private void createProductAsAdmin(Long categoryId, String name, String status, int stock, BigDecimal price) throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAsAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductUpsertRequest(
                                categoryId,
                                name,
                                name + " desc",
                                price,
                                stock,
                                null,
                                null,
                                "https://example.com/image.png",
                                status
                        ))))
                .andExpect(status().isOk());
    }

    private String registerAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "Customer",
                                email,
                                "password123",
                                "0800000000"
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return readToken(response);
    }

    private String loginAsAdmin() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "admin123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return readToken(response);
    }

    private String readToken(String json) throws Exception {
        return objectMapper.readTree(json).path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void wishlistLifecycleWorks() throws Exception {
        Category category = activeCategory("Electronics");
        createProductAsAdmin(category.getId(), "Smart TV", "ACTIVE", 10, new BigDecimal("499.99"));
        Long productId = productRepository.findAll().get(0).getId();

        String token = registerAndGetToken("wishlister@example.com");

        // 1. Check wishlist status (should be false)
        mockMvc.perform(get("/api/v1/wishlist/products/" + productId + "/check")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wishlisted").value(false));

        // 2. Add to wishlist
        mockMvc.perform(post("/api/v1/wishlist/products/" + productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.product.name").value("Smart TV"));

        // 3. Check again (should be true)
        mockMvc.perform(get("/api/v1/wishlist/products/" + productId + "/check")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wishlisted").value(true));

        // 4. List wishlist
        mockMvc.perform(get("/api/v1/wishlist")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].product.name").value("Smart TV"));

        // 5. Remove from wishlist
        mockMvc.perform(delete("/api/v1/wishlist/products/" + productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 6. Check again (should be false)
        mockMvc.perform(get("/api/v1/wishlist/products/" + productId + "/check")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wishlisted").value(false));
    }

    @Test
    void productReviewVerifiedPurchaseFlowWorks() throws Exception {
        Category category = activeCategory("Books");
        createProductAsAdmin(category.getId(), "Clean Architecture", "ACTIVE", 20, new BigDecimal("45.00"));
        Long productId = productRepository.findAll().get(0).getId();

        String token = registerAndGetToken("reviewer@example.com");

        // 1. Try to review before purchase (should fail)
        String reviewPayload = """
                {
                    "productId": %d,
                    "rating": 5,
                    "comment": "Must read for developers!"
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/v1/reviews")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You can only review products you have purchased and completed the order"));

        // 2. Buy product
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCartItemRequest(productId, null, 1))))
                .andExpect(status().isOk());

        String checkoutResponse = mockMvc.perform(post("/api/v1/checkout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckoutRequest(
                                "Bandung", "Reviewer User", "0812", "notes"
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long orderId = objectMapper.readTree(checkoutResponse).path("data").path("orderId").asLong();

        // 3. Try to review before order is completed (still processing/created, should fail)
        mockMvc.perform(post("/api/v1/reviews")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isBadRequest());

        // 4. Advance order status step-by-step as Admin: CREATED -> PROCESSING -> SHIPPED -> COMPLETED
        String adminToken = loginAsAdmin();
        mockMvc.perform(put("/api/v1/admin/orders/" + orderId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest("PROCESSING"))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/admin/orders/" + orderId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest("SHIPPED"))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/admin/orders/" + orderId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest("COMPLETED"))))
                .andExpect(status().isOk());

        // 5. Submit review (should succeed now!)
        mockMvc.perform(post("/api/v1/reviews")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Must read for developers!"));

        // 6. Try to submit duplicate review (should fail with conflict)
        mockMvc.perform(post("/api/v1/reviews")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isConflict());

        // 7. Verify review in product detail & list reviews
        mockMvc.perform(get("/api/v1/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRating").value(5.0))
                .andExpect(jsonPath("$.data.reviewCount").value(1));

        mockMvc.perform(get("/api/v1/products/" + productId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].comment").value("Must read for developers!"));

        // 8. Admin review search and delete
        String adminReviewsResponse = mockMvc.perform(get("/api/v1/admin/reviews")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long reviewId = objectMapper.readTree(adminReviewsResponse).path("data").path("content").get(0).path("id").asLong();

        // Admin deletes review
        mockMvc.perform(delete("/api/v1/admin/reviews/" + reviewId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk());

        // Detail rating stats should reset to 0
        mockMvc.perform(get("/api/v1/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRating").value(0.0))
                .andExpect(jsonPath("$.data.reviewCount").value(0));
    }

    @Test
    void oauth2SuccessHandlerFlowWorks() throws Exception {
        // 1. Create a mock OAuth2User principal
        Map<String, Object> attributes = Map.of(
                "sub", "google-oauth2-id-12345",
                "name", "OAuth User",
                "email", "oauthuser@example.com"
        );
        DefaultOAuth2User principal = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "name"
        );

        // 2. Create the Authentication token
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                "google"
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 3. Trigger success handler
        oauth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // 4. Assert redirection to frontend redirect URI with token query param
        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).startsWith("http://localhost:3000/oauth2/redirect?token=");

        String token = redirectUrl.substring("http://localhost:3000/oauth2/redirect?token=".length());
        assertThat(jwtService.extractUsername(token)).isEqualTo("oauthuser@example.com");

        // 5. Verify user is registered in database
        User user = userRepository.findByEmailIgnoreCase("oauthuser@example.com").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("OAuth User");
        assertThat(user.getProvider()).isEqualTo("google");
        assertThat(user.getProviderId()).isEqualTo("google-oauth2-id-12345");
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
    }
}
