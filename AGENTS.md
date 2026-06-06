# AGENTS.md

Panduan untuk AI coding agent yang bekerja pada repo ini.

## Project Overview

Backend REST API ecommerce: auth, katalog produk, kategori, cart, checkout,
order management, admin product/order, reporting, warehouse management, wishlist & product review.
Lihat `PRD.md` untuk requirement & business rules lengkap.

## Implemented Features

### 1. Authentication & Authorization
- **Register** - `POST /api/v1/auth/register`
- **Login** - `POST /api/v1/auth/login`
- **Refresh Token** - `POST /api/v1/auth/refresh` (rotate refresh token, issue new access token)
- **Forgot Password** - `POST /api/v1/auth/forgot-password`
- **Reset Password** - `POST /api/v1/auth/reset-password`
- **Email Verification** - `GET /api/v1/auth/verify-email`
- **OAuth2 Login (Google)** - `GET /api/v1/auth/oauth2/success`
- **Get Current User** - `GET /api/v1/auth/me`
- JWT access token (2 jam) + refresh token (UUID opaque, 30 hari, disimpan di `users.refresh_token`)
- Refresh token di-rotate setiap kali login/register/oauth2; token lama langsung invalid

### 2. User Management
- **Update Profile** - `PATCH /api/v1/users/profile`

### 3. Product Catalog (Customer)
- **List Products** - `GET /api/v1/products` (supports categoryId, keyword, pagination, sort; includes `isInWishlist` untuk user terautentikasi)
- **Search Products** - `GET /api/v1/products/search`
- **Product Detail** - `GET /api/v1/products/{id}`
- Response produk menyertakan `hasVariants`, `images` (list), `variants` (list dengan size/color/price/stock)

### 4. Category Management
- **Customer: List Categories** - `GET /api/v1/categories`
- **Admin: Create Category** - `POST /api/v1/admin/categories`
- **Admin: Update Category** - `PUT /api/v1/admin/categories/{id}`
- **Admin: Deactivate Category** - `DELETE /api/v1/admin/categories/{id}`

### 5. Cart Management
- **Get Cart** - `GET /api/v1/cart`
- **Add Item** - `POST /api/v1/cart/items` (field `variantId` opsional; wajib bila produk punya varian)
- **Update Item** - `PUT /api/v1/cart/items/{itemId}`
- **Remove Item** - `DELETE /api/v1/cart/items/{itemId}`
- **Clear Cart** - `DELETE /api/v1/cart`

### 6. Checkout
- **Checkout** - `POST /api/v1/checkout` (validates cart, deducts stock, creates order, clears cart)
- Mendukung produk dengan varian: lock & deduct stok varian secara atomik; snapshot `variantId`, `variantSku`, `variantLabel` tersimpan di order item

### 7. Order Management (Customer)
- **List Orders** - `GET /api/v1/orders` (paginated)
- **Order Detail** - `GET /api/v1/orders/{id}`

### 8. Order Requests (Customer) - Refund/Cancellation
- **List Requests** - `GET /api/v1/orders/requests`
- **Request Detail** - `GET /api/v1/orders/requests/{id}`
- **Create Request** - `POST /api/v1/orders/requests` (refund/cancellation requests)

### 9. Product Reviews (Customer)
- **Add Review** - `POST /api/v1/reviews`
- **My Reviews** - `GET /api/v1/reviews/me`
- **Product Reviews** - `GET /api/v1/products/{productId}/reviews`

### 10. Wishlist (Customer)
- **Add to Wishlist** - `POST /api/v1/wishlist/products/{productId}`
- **Remove from Wishlist** - `DELETE /api/v1/wishlist/products/{productId}`
- **List Wishlist** - `GET /api/v1/wishlist` (paginated)
- **Check Status** - `GET /api/v1/wishlist/products/{productId}/check`

### 11. Admin - Product Management
- **List Products** - `GET /api/v1/admin/products`
- **Create Product** - `POST /api/v1/admin/products`
- **Update Product** - `PUT /api/v1/admin/products/{id}`
- **Deactivate Product** - `DELETE /api/v1/admin/products/{id}`
- **Update Stock** - `PATCH /api/v1/admin/products/{id}/stock`
- **Update Status** - `PATCH /api/v1/admin/products/{id}/status`
- **Update Image (legacy)** - `PATCH /api/v1/admin/products/{id}/image`
- **Import Products** - `POST /api/v1/admin/products/import` (CSV/Excel)
- **Export Products** - `GET /api/v1/admin/products/export` (CSV/Excel)

### 12. Admin - Product Images (Multi-image)
- **Add Image** - `POST /api/v1/admin/products/{productId}/images` (multipart; param `primary`)
- **Set Primary** - `PATCH /api/v1/admin/products/{productId}/images/{imageId}/primary`
- **Delete Image** - `DELETE /api/v1/admin/products/{productId}/images/{imageId}`
- `product.imageUrl` di-sync otomatis ke gambar primary

### 13. Admin - Product Variants
- **Add Variant** - `POST /api/v1/admin/products/{productId}/variants`
- **Update Variant** - `PUT /api/v1/admin/products/{productId}/variants/{variantId}`
- **Delete Variant** - `DELETE /api/v1/admin/products/{productId}/variants/{variantId}`
- `product.stock` dipertahankan sebagai agregat stok varian (auto-sync saat variant save/delete)
- Variant punya: `sku` (unik global), `size`, `color`, `price` (opsional, fallback ke product price), `stock`, `status`

### 14. Admin - Order Management
- **List Orders** - `GET /api/v1/admin/orders` (filter by status, orderNumber)
- **Order Detail** - `GET /api/v1/admin/orders/{id}`
- **Update Status** - `PUT /api/v1/admin/orders/{id}/status` — saat transisi ke CANCELLED, stok produk & varian dikembalikan secara atomik (pessimistic lock)
- **Generate Invoice** - `GET /api/v1/admin/orders/{id}/invoice` (PDF)

### 15. Admin - Order Requests Management
- **List Requests** - `GET /api/v1/admin/order-requests` (filter by status, type)
- **Resolve Request** - `PUT /api/v1/admin/order-requests/{id}` — saat approve cancel request, stok produk & varian dikembalikan

### 16. Admin - Warehouse Management
- **List Warehouses** - `GET /api/v1/admin/warehouses`
- **List Active Warehouses** - `GET /api/v1/admin/warehouses/active`
- **Create Warehouse** - `POST /api/v1/admin/warehouses`
- **Update Warehouse** - `PUT /api/v1/admin/warehouses/{id}`
- **Deactivate Warehouse** - `DELETE /api/v1/admin/warehouses/{id}`

### 17. Admin - Product Reviews Management
- **Search Reviews** - `GET /api/v1/admin/reviews` (filter by rating, keyword)
- **Delete Review** - `DELETE /api/v1/admin/reviews/{id}`

### 18. Admin - Reports & Dashboard
- **Summary Report** - `GET /api/v1/admin/reports/summary`
- **Dashboard** - `GET /api/v1/admin/reports/dashboard`
- **Export Report** - `GET /api/v1/admin/reports/export` (CSV/Excel/PDF)

### 19. Admin - Email
- **Send Email** - `POST /api/v1/admin/emails/send`

### 20. File Storage
- **Get Product Image** - `GET /api/v1/files/products/{filename}`

### Data Models (Entities)

| Entity | Description |
|--------|-------------|
| User | id, name, email, password, phone, role, status, refreshToken, refreshTokenExpiry |
| Category | id, name, description, status |
| Product | id, category, name, description, price, stock, warehouse, minimumStockLevel, imageUrl, status |
| ProductImage | id, product, imageUrl, sortOrder, isPrimary |
| ProductVariant | id, product, sku, size, color, price (nullable), stock, status |
| Cart | id, user, status |
| CartItem | id, cart, product, variant (nullable), quantity, priceSnapshot |
| Order | id, user, orderNumber, recipientName, recipientPhone, shippingAddress, notes, totalAmount, status |
| OrderItem | id, order, productId, productName, variantId (nullable), variantSku, variantLabel, price, quantity, subtotal |
| OrderRequest | id, order, user, type (REFUND/CANCELLATION), status, reason, notes, requestedAmount |
| ProductReview | id, product, user, rating, comment |
| Wishlist | id, user, product |
| Warehouse | id, code, name, location, status |

### Enums

- **Role**: CUSTOMER, ADMIN
- **OrderStatus**: CREATED, PROCESSING, SHIPPED, COMPLETED, CANCELLED
- **ProductStatus**: ACTIVE, INACTIVE
- **OrderRequestStatus**: PENDING, APPROVED, REJECTED, COMPLETED
- **OrderRequestType**: REFUND, CANCELLATION
- **WarehouseStatus**: ACTIVE, INACTIVE

## Tech Stack

- Java 17, Spring Boot 3.3.2 (Maven, **tanpa** wrapper — pakai `mvn` sistem)
- Spring Web, Spring Security + JWT (jjwt 0.12.6), OAuth2 Client (Google)
- Spring Data JPA + MySQL (production), H2 in-memory (test)
- Flyway untuk migrasi DB (`src/main/resources/db/migration`, V1..V8)
- Lombok, springdoc OpenAPI (Swagger UI di `/swagger-ui.html`)
- Apache POI + PDFBox untuk export report/invoice; Spring Mail (Resend) untuk email

Catatan: folder `build/`, `bin/` adalah artefak lama; build resmi memakai Maven (`pom.xml`).

## Commands

```bash
mvn clean compile        # build
mvn test                 # jalankan test (pakai H2, profil test)
mvn spring-boot:run      # jalankan app lokal
mvn clean package        # build jar
```

Selalu jalankan `mvn test` setelah perubahan kode.

## Architecture & Conventions

Package root `com.ecommerce`, layered:

```
config · security · controller · service · repository
entity (+enums) · dto/{request,response} · exception · mapper · util
```

- Endpoint base `/api/v1/...`; endpoint admin di bawah `/api/v1/admin/...`.
- Controller mengembalikan `ApiResponse<T>` (`ApiResponse.success(message, data)`);
  list pakai `PageResponse<T>` untuk pagination.
- **Jangan expose entity langsung** — selalu lewat DTO request/response.
- Validasi pakai annotation Bean Validation; error ditangani terpusat di
  `exception/GlobalExceptionHandler` dengan exception custom (`ResourceNotFoundException`,
  `BadRequestException`, `ConflictException`, `InsufficientStockException`, dst).
- Constructor injection via Lombok `@RequiredArgsConstructor` (field `final`).
- Operasi yang mengubah stok/order (mis. checkout, cancel) harus `@Transactional` dan atomic; gunakan pessimistic lock (`findAllByIdForUpdate`) untuk stok.
- Produk/kategori pakai soft delete (status), bukan hapus permanen; order item menyimpan snapshot harga, nama produk, dan info varian.
- Produk dengan varian: `product.stock` adalah agregat dari `SUM(variant.stock)`, diperbarui otomatis oleh `ProductVariantService.syncAggregateStock()`.

## Database

- Schema dikelola Flyway; `spring.jpa.hibernate.ddl-auto=validate` (jangan andalkan auto-DDL).
  Perubahan skema = tambah file migrasi `V{n}__deskripsi.sql` baru, jangan ubah yang lama.
- Migrasi saat ini: V1 (init schema), V2 (seed), V3 (oauth2 fields), V4 (account features),
  V5 (dashboard/stock/requests), V6 (wishlist/reviews), V7 (product_images/variants), V8 (refresh token)

## Configuration

Semua secret/config via environment variable (lihat `src/main/resources/application.properties`):
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `RESEND_API_KEY`,
`google.client-id`, `google.client-secret`, `OAUTH2_FRONTEND_REDIRECT_URI`,
`PRODUCT_IMAGES_DIR`. Jangan hardcode atau commit secret.

## Testing

Test di `src/test/java/com/ecommerce`:
- Unit test service (`src/test/java/com/ecommerce/service/`) — Mockito, tanpa Spring context
- Integration test (`EcommerceApplicationTests`) — `@SpringBootTest`, H2 in-memory

Tambah/ubah test saat menambah fitur atau memperbaiki bug.

## Git

- Branch utama `main`; jangan push langsung ke `main` tanpa izin.
- Commit hanya jika diminta. Stage file spesifik, hindari `git add .`.
