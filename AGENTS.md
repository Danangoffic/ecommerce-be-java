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
- **Forgot Password** - `POST /api/v1/auth/forgot-password`
- **Reset Password** - `POST /api/v1/auth/reset-password`
- **Email Verification** - `GET /api/v1/auth/verify-email`
- **OAuth2 Login (Google)** - `GET /api/v1/auth/oauth2/success`
- **Get Current User** - `GET /api/v1/auth/me`
- JWT-based authentication dengan role-based access (CUSTOMER, ADMIN)

### 2. User Management
- **Update Profile** - `PATCH /api/v1/users/profile`

### 3. Product Catalog (Customer)
- **List Products** - `GET /api/v1/products` (supports categoryId, keyword, pagination, sort)
- **Search Products** - `GET /api/v1/products/search`
- **Product Detail** - `GET /api/v1/products/{id}`

### 4. Category Management
- **Customer: List Categories** - `GET /api/v1/categories`
- **Admin: Create Category** - `POST /api/v1/admin/categories`
- **Admin: Update Category** - `PUT /api/v1/admin/categories/{id}`
- **Admin: Deactivate Category** - `DELETE /api/v1/admin/categories/{id}`

### 5. Cart Management
- **Get Cart** - `GET /api/v1/cart`
- **Add Item** - `POST /api/v1/cart/items`
- **Update Item** - `PUT /api/v1/cart/items/{itemId}`
- **Remove Item** - `DELETE /api/v1/cart/items/{itemId}`
- **Clear Cart** - `DELETE /api/v1/cart`

### 6. Checkout
- **Checkout** - `POST /api/v1/checkout` (validates cart, deducts stock, creates order, clears cart)

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
- **Update Image** - `PATCH /api/v1/admin/products/{id}/image`
- **Import Products** - `POST /api/v1/admin/products/import` (CSV/Excel)
- **Export Products** - `GET /api/v1/admin/products/export` (CSV/Excel)

### 12. Admin - Order Management
- **List Orders** - `GET /api/v1/admin/orders` (filter by status, orderNumber)
- **Order Detail** - `GET /api/v1/admin/orders/{id}`
- **Update Status** - `PUT /api/v1/admin/orders/{id}/status`
- **Generate Invoice** - `GET /api/v1/admin/orders/{id}/invoice` (PDF)

### 13. Admin - Order Requests Management
- **List Requests** - `GET /api/v1/admin/order-requests` (filter by status, type)
- **Resolve Request** - `PUT /api/v1/admin/order-requests/{id}`

### 14. Admin - Warehouse Management
- **List Warehouses** - `GET /api/v1/admin/warehouses`
- **List Active Warehouses** - `GET /api/v1/admin/warehouses/active`
- **Create Warehouse** - `POST /api/v1/admin/warehouses`
- **Update Warehouse** - `PUT /api/v1/admin/warehouses/{id}`
- **Deactivate Warehouse** - `DELETE /api/v1/admin/warehouses/{id}`

### 15. Admin - Product Reviews Management
- **Search Reviews** - `GET /api/v1/admin/reviews` (filter by rating, keyword)
- **Delete Review** - `DELETE /api/v1/admin/reviews/{id}`

### 16. Admin - Reports & Dashboard
- **Summary Report** - `GET /api/v1/admin/reports/summary`
- **Dashboard** - `GET /api/v1/admin/reports/dashboard`
- **Export Report** - `GET /api/v1/admin/reports/export` (CSV/Excel/PDF)

### 17. Admin - Email
- **Send Email** - `POST /api/v1/admin/emails/send`

### 18. File Storage
- **Get Product Image** - `GET /api/v1/files/products/{filename}`

### Data Models (Entities)

| Entity | Description |
|--------|-------------|
| User | id, name, email, password, phone, role, status |
| Category | id, name, description, status |
| Product | id, category, name, description, price, stock, warehouse, minimumStockLevel, imageUrl, status |
| Cart | id, user, status |
| CartItem | id, cart, product, quantity, priceSnapshot |
| Order | id, user, orderNumber, recipientName, recipientPhone, shippingAddress, notes, totalAmount, status |
| OrderItem | id, order, product, productName, price, quantity, subtotal |
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
- Flyway untuk migrasi DB (`src/main/resources/db/migration`, V1..V6)
- Lombok, springdoc OpenAPI (Swagger UI di `/swagger-ui.html`)
- Apache POI + PDFBox untuk export report/invoice; Spring Mail (Resend) untuk email

Catatan: folder `.gradle/`, `build/`, `bin/` adalah artefak lama; build resmi memakai Maven (`pom.xml`).

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
- Operasi yang mengubah stok/order (mis. checkout) harus `@Transactional` dan atomic.
- Produk/kategori pakai soft delete (status), bukan hapus permanen; order item menyimpan
  snapshot harga & nama produk.

## Database

- Schema dikelola Flyway; `spring.jpa.hibernate.ddl-auto=validate` (jangan andalkan auto-DDL).
  Perubahan skema = tambah file migrasi `V{n}__deskripsi.sql` baru, jangan ubah yang lama.

## Configuration

Semua secret/config via environment variable (lihat `src/main/resources/application.properties`):
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `RESEND_API_KEY`,
`google.client-id`, `google.client-secret`, `OAUTH2_FRONTEND_REDIRECT_URI`,
`PRODUCT_IMAGES_DIR`. Jangan hardcode atau commit secret.

## Testing

Test di `src/test/java/com/ecommerce` (integration test berbasis `@SpringBootTest`, H2).
Tambah/ubah test saat menambah fitur atau memperbaiki bug.

## Git

- Branch utama `main`; jangan push langsung ke `main` tanpa izin.
- Commit hanya jika diminta. Stage file spesifik, hindari `git add .`.
