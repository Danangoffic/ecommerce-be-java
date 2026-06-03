# PRD — Ecommerce App

## 1. Ringkasan Produk

Ecommerce App adalah aplikasi jual-beli online yang memungkinkan user melihat produk, melakukan registrasi/login, menambahkan produk ke cart, checkout, membuat order, dan memantau status pesanan.

Backend dibangun menggunakan:

* Java 17
* Spring Boot
* MySQL
* JWT Authentication
* REST API

Target awal adalah membangun MVP yang stabil, aman, dan mudah dikembangkan menjadi platform ecommerce yang lebih besar.

---

## 2. Tujuan Produk

### Tujuan Utama

Menyediakan platform ecommerce sederhana yang memungkinkan customer melakukan pembelian produk secara online dengan alur yang jelas dan aman.

### Tujuan Bisnis

* Memungkinkan bisnis menjual produk secara digital.
* Mempermudah pengelolaan produk, stok, customer, dan order.
* Menjadi foundation untuk fitur lanjutan seperti payment gateway, promo, voucher, loyalty, dan shipment tracking.

---

## 3. Target User

### Customer

User yang ingin membeli produk melalui aplikasi.

Kebutuhan utama:

* Melihat daftar produk.
* Mencari produk.
* Melihat detail produk.
* Menambahkan produk ke cart.
* Checkout.
* Melihat status order.

### Admin

User internal yang mengelola ecommerce.

Kebutuhan utama:

* Mengelola produk.
* Mengelola kategori.
* Mengelola stok.
* Melihat order masuk.
* Mengubah status pesanan.

---

## 4. Scope MVP

### Included in MVP

Fitur yang masuk tahap awal:

1. Authentication & Authorization
2. User Management
3. Product Catalog
4. Category Management
5. Cart
6. Checkout
7. Order Management
8. Admin Product Management
9. Admin Order Management
10. Basic Reporting

### Excluded from MVP

Fitur yang tidak masuk MVP awal:

1. Payment gateway real-time
2. Shipment integration
3. Voucher/promo engine
4. Product review & rating
5. Wishlist
6. Recommendation system
7. Multi-seller marketplace
8. Loyalty point
9. Refund management
10. Chat customer service

Fitur excluded dapat masuk fase berikutnya setelah MVP stabil.

---

## 5. Role & Permission

### Customer

Customer dapat:

* Register
* Login
* Melihat produk
* Melihat detail produk
* Menambahkan produk ke cart
* Checkout
* Melihat order miliknya

### Admin

Admin dapat:

* Login
* Membuat produk
* Mengubah produk
* Menghapus produk
* Mengelola kategori
* Melihat semua order
* Mengubah status order
* Melihat ringkasan transaksi

---

## 6. User Journey

### Customer Journey

1. Customer membuka aplikasi.
2. Customer melihat daftar produk.
3. Customer membuka detail produk.
4. Customer register/login.
5. Customer menambahkan produk ke cart.
6. Customer membuka cart.
7. Customer checkout.
8. Sistem membuat order.
9. Customer melihat status order.

### Admin Journey

1. Admin login.
2. Admin membuat kategori.
3. Admin menambahkan produk.
4. Admin mengatur stok dan harga.
5. Admin melihat order masuk.
6. Admin memproses order.
7. Admin mengubah status order.

---

## 7. Functional Requirements

## 7.1 Authentication

### Register

Customer dapat membuat akun baru.

Input:

* Name
* Email
* Password
* Phone number

Rules:

* Email harus unique.
* Password harus di-hash.
* Default role adalah CUSTOMER.

Acceptance Criteria:

* User berhasil register jika email belum digunakan.
* Sistem menolak register jika email sudah terdaftar.
* Password tidak boleh disimpan dalam plain text.

---

### Login

User dapat login menggunakan email dan password.

Input:

* Email
* Password

Output:

* Access token JWT
* User profile
* Role

Acceptance Criteria:

* Login berhasil jika credential valid.
* Login gagal jika email/password salah.
* Token digunakan untuk akses endpoint protected.

---

### JWT Authorization

Sistem menggunakan JWT untuk validasi akses API.

Rules:

* Endpoint public dapat diakses tanpa token.
* Endpoint customer memerlukan role CUSTOMER.
* Endpoint admin memerlukan role ADMIN.
* Token expired harus ditolak.

---

## 7.2 User Management

Customer dapat melihat profilnya sendiri.

Data profil:

* User ID
* Name
* Email
* Phone number
* Role
* Created date

Acceptance Criteria:

* User hanya dapat melihat datanya sendiri.
* Admin dapat melihat daftar user jika diperlukan pada fase admin management.

---

## 7.3 Category Management

Admin dapat membuat, mengubah, melihat, dan menghapus kategori produk.

Data kategori:

* Category ID
* Name
* Description
* Status

Acceptance Criteria:

* Customer dapat melihat kategori aktif.
* Admin dapat mengelola kategori.
* Kategori yang masih memiliki produk sebaiknya tidak langsung dihapus, tetapi diubah menjadi inactive.

---

## 7.4 Product Catalog

Customer dapat melihat daftar produk.

Data produk:

* Product ID
* Name
* Description
* Price
* Stock
* Image URL
* Category
* Status

Fitur catalog:

* List product
* Product detail
* Search product by keyword
* Filter by category
* Pagination
* Sort by newest/price

Acceptance Criteria:

* Customer hanya melihat produk aktif.
* Produk dengan stok 0 tetap dapat ditampilkan, tetapi tidak dapat dibeli.
* API list product wajib mendukung pagination.

---

## 7.5 Product Management — Admin

Admin dapat melakukan CRUD produk.

Admin dapat:

* Create product
* Update product
* Delete/deactivate product
* Update price
* Update stock
* Assign category

Acceptance Criteria:

* Hanya admin yang dapat mengelola produk.
* Harga produk tidak boleh negatif.
* Stok produk tidak boleh negatif.
* Produk yang sudah pernah dibeli sebaiknya tidak dihapus permanen, melainkan dinonaktifkan.

---

## 7.6 Cart

Customer dapat menambahkan produk ke cart sebelum checkout.

Fitur cart:

* Add product to cart
* Update quantity
* Remove item
* View cart
* Clear cart

Rules:

* Quantity tidak boleh melebihi stok.
* Produk inactive tidak dapat ditambahkan ke cart.
* Cart terikat ke customer login.

Acceptance Criteria:

* Customer dapat memiliki satu active cart.
* Jika produk yang sama ditambahkan lagi, quantity bertambah.
* Sistem menghitung subtotal otomatis.

---

## 7.7 Checkout

Customer dapat checkout dari cart.

Input checkout:

* Shipping address
* Recipient name
* Recipient phone
* Notes

Process:

1. Sistem validasi cart.
2. Sistem validasi stok produk.
3. Sistem membuat order.
4. Sistem membuat order item.
5. Sistem mengurangi stok produk.
6. Sistem mengosongkan cart.

Acceptance Criteria:

* Checkout gagal jika cart kosong.
* Checkout gagal jika stok tidak cukup.
* Checkout berhasil menghasilkan order number.
* Total order dihitung dari harga produk saat checkout, bukan harga terbaru setelah order dibuat.

---

## 7.8 Order Management — Customer

Customer dapat melihat daftar order miliknya.

Data order:

* Order ID
* Order number
* Total amount
* Status
* Order date
* Items
* Shipping address

Order status:

* CREATED
* PAID
* PROCESSING
* SHIPPED
* COMPLETED
* CANCELLED

Untuk MVP tanpa payment gateway, status awal order dapat menggunakan CREATED atau PAID_MANUAL.

Acceptance Criteria:

* Customer hanya dapat melihat order miliknya sendiri.
* Customer tidak dapat melihat order milik user lain.
* Customer dapat melihat detail order.

---

## 7.9 Order Management — Admin

Admin dapat melihat dan mengelola semua order.

Admin dapat:

* Melihat order list
* Melihat order detail
* Mengubah status order
* Filter order by status
* Search order by order number

Acceptance Criteria:

* Hanya admin yang dapat melihat semua order.
* Perubahan status harus mengikuti flow yang valid.
* Order CANCELLED tidak bisa diproses lagi.

---

## 7.10 Basic Reporting

Admin dapat melihat ringkasan sederhana:

* Total products
* Total active products
* Total orders
* Total sales amount
* Total customers
* Latest orders

Acceptance Criteria:

* Report hanya dapat diakses admin.
* Data report dihitung dari order yang valid.

---

# 8. Non-Functional Requirements

## 8.1 Security

Requirement:

* Password harus di-hash menggunakan BCrypt.
* Authentication menggunakan JWT.
* Role-based access control wajib diterapkan.
* API admin harus protected.
* Input harus divalidasi.
* Hindari expose stack trace ke response API.
* Gunakan HTTPS di production.

---

## 8.2 Performance

Requirement:

* API list product wajib menggunakan pagination.
* Query product dan order harus memiliki index yang sesuai.
* Response API standar di bawah 500ms untuk data normal.
* Hindari N+1 query pada relasi product/order/order item.

---

## 8.3 Scalability

Requirement:

* Struktur backend harus modular.
* Pisahkan layer controller, service, repository, DTO, entity.
* Gunakan transaction management untuk checkout.
* Design database harus siap dikembangkan untuk payment, shipment, dan promo.

---

## 8.4 Reliability

Requirement:

* Checkout harus atomic.
* Jika proses order gagal, stok tidak boleh berkurang.
* Jika stok tidak cukup, order tidak boleh dibuat.
* Semua perubahan penting harus dilakukan dalam database transaction.

---

## 8.5 Maintainability

Requirement:

* Gunakan clean package structure.
* Gunakan DTO untuk request/response.
* Jangan expose entity langsung ke API response.
* Gunakan global exception handler.
* Gunakan validation annotation.
* Dokumentasi API menggunakan Swagger/OpenAPI.

---

# 9. Recommended Backend Architecture

## Tech Stack

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* MySQL
* JWT
* Maven/Gradle
* Swagger/OpenAPI
* Lombok
* Flyway/Liquibase untuk database migration

---

## Package Structure

```text
com.company.ecommerce
├── config
├── security
├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── exception
├── mapper
└── util
```

---

## Recommended Modules

```text
auth
user
category
product
cart
order
admin
report
```

---

# 10. Database Design

## users

```text
id
name
email
password
phone_number
role
status
created_at
updated_at
```

## categories

```text
id
name
description
status
created_at
updated_at
```

## products

```text
id
category_id
name
description
price
stock
image_url
status
created_at
updated_at
```

## carts

```text
id
user_id
status
created_at
updated_at
```

## cart_items

```text
id
cart_id
product_id
quantity
price_snapshot
created_at
updated_at
```

## orders

```text
id
user_id
order_number
recipient_name
recipient_phone
shipping_address
notes
total_amount
status
created_at
updated_at
```

## order_items

```text
id
order_id
product_id
product_name
price
quantity
subtotal
created_at
updated_at
```

---

# 11. API Endpoint Requirement

## Auth API

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
```

---

## Product API — Public/Customer

```text
GET /api/v1/products
GET /api/v1/products/{id}
GET /api/v1/products/search
```

Query parameters:

```text
keyword
categoryId
page
size
sort
```

---

## Category API

```text
GET  /api/v1/categories
POST /api/v1/admin/categories
PUT  /api/v1/admin/categories/{id}
DELETE /api/v1/admin/categories/{id}
```

---

## Cart API — Customer

```text
GET    /api/v1/cart
POST   /api/v1/cart/items
PUT    /api/v1/cart/items/{itemId}
DELETE /api/v1/cart/items/{itemId}
DELETE /api/v1/cart
```

---

## Checkout API — Customer

```text
POST /api/v1/checkout
```

---

## Order API — Customer

```text
GET /api/v1/orders
GET /api/v1/orders/{id}
```

---

## Order API — Admin

```text
GET /api/v1/admin/orders
GET /api/v1/admin/orders/{id}
PUT /api/v1/admin/orders/{id}/status
```

---

## Product API — Admin

```text
POST   /api/v1/admin/products
PUT    /api/v1/admin/products/{id}
DELETE /api/v1/admin/products/{id}
PATCH  /api/v1/admin/products/{id}/stock
PATCH  /api/v1/admin/products/{id}/status
```

---

## Report API — Admin

```text
GET /api/v1/admin/reports/summary
```

---

# 12. Standard API Response

Gunakan response format konsisten.

## Success Response

```json
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

## Error Response

```json
{
  "success": false,
  "message": "Validation error",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

---

# 13. Key Business Rules

## Product

* Produk hanya bisa dibeli jika status ACTIVE.
* Produk tidak bisa dibeli jika stok 0.
* Harga produk saat checkout harus disimpan ke order item sebagai price snapshot.

## Cart

* Satu customer hanya memiliki satu active cart.
* Quantity cart tidak boleh melebihi stok produk.
* Cart dikosongkan setelah checkout berhasil.

## Order

* Order number harus unique.
* Order tidak boleh dibuat jika stok tidak cukup.
* Status order hanya bisa berubah sesuai flow yang valid.

## Stock

* Stok berkurang saat checkout berhasil.
* Checkout harus menggunakan database transaction.
* Perlu handling race condition untuk pembelian produk dengan stok terbatas.

---

# 14. Order Status Flow

```text
CREATED → PAID → PROCESSING → SHIPPED → COMPLETED
CREATED → CANCELLED
PAID → CANCELLED
PROCESSING → CANCELLED
```

Rules:

* COMPLETED tidak bisa diubah lagi.
* CANCELLED tidak bisa diubah lagi.
* SHIPPED tidak boleh kembali ke PROCESSING.
* Untuk MVP tanpa payment gateway, admin dapat mengubah CREATED menjadi PAID secara manual.

---

# 15. Acceptance Criteria MVP

MVP dianggap selesai jika:

1. Customer dapat register dan login.
2. Customer dapat melihat produk aktif.
3. Customer dapat melihat detail produk.
4. Customer dapat menambahkan produk ke cart.
5. Customer dapat checkout.
6. Sistem membuat order dan order item.
7. Sistem mengurangi stok setelah checkout berhasil.
8. Customer dapat melihat order miliknya.
9. Admin dapat login.
10. Admin dapat CRUD produk.
11. Admin dapat CRUD kategori.
12. Admin dapat melihat semua order.
13. Admin dapat mengubah status order.
14. Endpoint protected menggunakan JWT.
15. Role admin dan customer terpisah.
16. API memiliki validasi request.
17. API memiliki error response yang konsisten.
18. API terdokumentasi di Swagger/OpenAPI.

---

# 16. Success Metrics

## Product Metrics

* Jumlah registered customer
* Jumlah produk aktif
* Jumlah cart created
* Jumlah checkout berhasil
* Jumlah order completed
* Conversion rate dari cart ke checkout

## Technical Metrics

* API uptime
* Error rate
* Average response time
* Failed checkout rate
* Database query performance
* Security issue count

---

# 17. Risiko & Mitigasi

## Risiko 1: Race Condition pada Stok

Saat banyak user checkout produk yang sama, stok bisa menjadi tidak konsisten.

Mitigasi:

* Gunakan transaction.
* Gunakan database locking atau optimistic locking.
* Validasi stok ulang saat checkout.

---

## Risiko 2: JWT Token Disalahgunakan

Token yang bocor dapat digunakan oleh pihak lain.

Mitigasi:

* Gunakan expiry time pendek.
* Gunakan HTTPS.
* Jangan simpan data sensitif di payload JWT.
* Pertimbangkan refresh token pada fase berikutnya.

---

## Risiko 3: Order Dibuat Tapi Stok Tidak Konsisten

Jika proses checkout gagal di tengah jalan, data bisa tidak sinkron.

Mitigasi:

* Checkout wajib atomic.
* Gunakan @Transactional.
* Rollback jika ada error.

---

## Risiko 4: Produk Dihapus Setelah Pernah Dibeli

Order history bisa rusak jika produk dihapus permanen.

Mitigasi:

* Gunakan soft delete.
* Simpan product_name dan price di order_items.

---

## Risiko 5: Scope Terlalu Besar

Ecommerce mudah melebar ke payment, shipment, promo, loyalty, review, dan marketplace.

Mitigasi:

* Fokus MVP pada catalog, cart, checkout, dan order.
* Payment gateway masuk fase berikutnya.
* Shipment tracking masuk fase berikutnya.

---

# 18. Development Milestone

## Phase 1 — Foundation

Scope:

* Project setup
* Database setup
* Spring Security setup
* JWT authentication
* User register/login
* Role authorization
* Swagger setup

Output:

* Backend skeleton siap dikembangkan.
* Auth API berjalan.

---

## Phase 2 — Product & Category

Scope:

* Category CRUD
* Product CRUD
* Product list
* Product detail
* Search/filter/pagination

Output:

* Customer dapat melihat catalog.
* Admin dapat mengelola produk.

---

## Phase 3 — Cart & Checkout

Scope:

* Cart management
* Add/update/remove cart item
* Checkout
* Stock deduction
* Transaction handling

Output:

* Customer dapat checkout produk.

---

## Phase 4 — Order Management

Scope:

* Customer order history
* Customer order detail
* Admin order list
* Admin update order status

Output:

* Order flow dapat diproses end-to-end.

---

## Phase 5 — Hardening

Scope:

* Validation improvement
* Error handling
* Security review
* Query optimization
* Unit test
* Integration test
* Deployment preparation

Output:

* MVP siap untuk staging/UAT.

---

# 19. Testing Requirement

## Unit Test

Minimal test untuk:

* Auth service
* Product service
* Cart service
* Checkout service
* Order service

## Integration Test

Minimal test untuk:

* Register/login flow
* Add to cart flow
* Checkout flow
* Admin update order status flow

## Security Test

Minimal test untuk:

* Endpoint customer tanpa token
* Endpoint admin dengan role customer
* Invalid JWT
* Expired JWT
* Unauthorized access order milik user lain

---

# 20. Recommendation for MVP Decision

Prioritas MVP paling disarankan:

1. Authentication + JWT
2. Product catalog
3. Cart
4. Checkout
5. Order management
6. Admin product/order management

Jangan masukkan payment gateway di awal kecuali sudah ada kebutuhan bisnis yang jelas. Untuk MVP, gunakan status order manual terlebih dahulu agar core ecommerce flow bisa selesai lebih cepat dan stabil.

Setelah MVP stabil, fase berikutnya yang paling bernilai adalah:

1. Payment gateway
2. Shipment integration
3. Voucher/promo
4. Product review
5. Dashboard analytics
