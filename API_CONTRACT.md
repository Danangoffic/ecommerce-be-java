# API Contract

Base URL: `http://localhost:8080/api/v1`

Semua response dibungkus dalam envelope:
```json
{ "success": true, "message": "...", "data": { ... } }
```

Error response:
```json
{ "success": false, "message": "...", "errors": ["..."], "timestamp": "2026-01-01T00:00:00Z" }
```

List/paginated endpoint mengembalikan `data` berupa:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false
}
```

---

## Authentication

> Endpoint ini tidak memerlukan header Authorization.

### Register
`POST /auth/register`

**Request**
```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "password": "secret123",
  "phoneNumber": "08123456789"
}
```

**Response `200`**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "550e8400-e29b-41d4-a716-...",
  "refreshTokenExpiresAt": "2026-07-06T10:00:00Z",
  "user": {
    "id": 1,
    "name": "Alice",
    "email": "alice@example.com",
    "phoneNumber": "08123456789",
    "role": "CUSTOMER",
    "createdAt": "2026-06-06T10:00:00Z"
  }
}
```

---

### Login
`POST /auth/login`

**Request**
```json
{
  "email": "alice@example.com",
  "password": "secret123"
}
```

**Response `200`** — sama dengan Register

---

### Refresh Token
`POST /auth/refresh`

**Request**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-..."
}
```

**Response `200`** — sama dengan Register (token baru, token lama invalid)

**Error `400`** — token tidak ditemukan atau sudah expired

---

### Forgot Password
`POST /auth/forgot-password`

**Request**
```json
{ "email": "alice@example.com" }
```

**Response `200`** — `data: null`

---

### Reset Password
`POST /auth/reset-password`

**Request**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "newSecret123"
}
```

**Response `200`** — `data: null`

---

### Verify Email
`GET /auth/verify-email?token=<token>`

**Response `200`** — `data: null`

---

### Get Current User
`GET /auth/me` 🔒

**Response `200`**
```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "phoneNumber": "08123456789",
  "role": "CUSTOMER",
  "createdAt": "2026-06-06T10:00:00Z"
}
```

---

## User

### Update Profile
`PATCH /users/profile` 🔒

**Request**
```json
{
  "name": "Alice Updated",
  "phoneNumber": "08999999999"
}
```

**Response `200`** — `UserProfile` object

---

## Products (Customer)

### List Products
`GET /products` 🔓

**Query params**

| Param | Type | Description |
|-------|------|-------------|
| `categoryId` | Long | Filter by category |
| `keyword` | String | Search by name |
| `page` | int | Default `0` |
| `size` | int | Default `10`, max `100` |
| `sort` | String | `price` atau default (newest) |

**Response `200`** — `PageResponse<ProductResponse>`

```json
{
  "id": 1,
  "name": "Smart TV",
  "description": "55-inch 4K",
  "price": 4999000.00,
  "stock": 20,
  "minimumStockLevel": 5,
  "purchasable": true,
  "lowStock": false,
  "imageUrl": "/api/v1/files/products/tv.jpg",
  "status": "ACTIVE",
  "category": { "id": 1, "name": "Electronics", "description": null, "status": "ACTIVE" },
  "warehouse": { "id": 1, "code": "MAIN", "name": "Main Warehouse", "location": "Jakarta", "status": "ACTIVE" },
  "averageRating": 4.5,
  "reviewCount": 12,
  "isInWishlist": false,
  "hasVariants": true,
  "images": [
    { "id": 1, "imageUrl": "/api/v1/files/products/tv.jpg", "sortOrder": 0, "primary": true }
  ],
  "variants": [
    {
      "id": 1,
      "sku": "TV-55-BLK",
      "size": null,
      "color": "Black",
      "label": "Black",
      "price": 4999000.00,
      "stock": 10,
      "purchasable": true,
      "status": "ACTIVE"
    }
  ],
  "createdAt": "2026-06-01T00:00:00Z"
}
```

> `isInWishlist` bernilai `true/false` untuk user yang login, selalu `false` untuk guest.

---

### Product Detail
`GET /products/{id}` 🔓

**Response `200`** — `ProductResponse` (sama dengan di atas)

**Error `404`** — produk tidak ditemukan atau tidak aktif

---

### Product Reviews
`GET /products/{productId}/reviews` 🔓

**Query params**: `page`, `size`

**Response `200`** — `PageResponse<ProductReviewResponse>`
```json
{
  "id": 1,
  "product": { "...ProductResponse..." },
  "userId": 2,
  "userName": "Bob",
  "rating": 5,
  "comment": "Bagus banget!",
  "createdAt": "2026-06-05T00:00:00Z"
}
```

---

## Categories (Customer)

### List Categories
`GET /categories` 🔓

**Response `200`** — `List<CategoryResponse>`
```json
[
  { "id": 1, "name": "Electronics", "description": "...", "status": "ACTIVE" }
]
```

---

## Cart 🔒

### Get Cart
`GET /cart`

**Response `200`**
```json
{
  "id": 1,
  "status": "ACTIVE",
  "items": [
    {
      "id": 1,
      "productId": 10,
      "productName": "Smart TV",
      "variantId": 1,
      "variantLabel": "Black",
      "quantity": 2,
      "priceSnapshot": 4999000.00,
      "subtotal": 9998000.00
    }
  ],
  "subtotal": 9998000.00
}
```

---

### Add Item
`POST /cart/items`

**Request**
```json
{
  "productId": 10,
  "variantId": 1,
  "quantity": 2
}
```
> `variantId` wajib diisi jika produk memiliki varian (`hasVariants: true`), opsional jika tidak.

**Response `200`** — `CartResponse`

---

### Update Item
`PUT /cart/items/{itemId}`

**Request**
```json
{ "quantity": 3 }
```

**Response `200`** — `CartResponse`

---

### Remove Item
`DELETE /cart/items/{itemId}`

**Response `200`** — `CartResponse`

---

### Clear Cart
`DELETE /cart`

**Response `200`** — `CartResponse` (items kosong)

---

## Checkout 🔒

### Checkout
`POST /checkout`

**Request**
```json
{
  "shippingAddress": "Jl. Sudirman No. 1, Jakarta",
  "recipientName": "Alice",
  "recipientPhone": "08123456789",
  "notes": "Tolong bubble wrap"
}
```

**Response `200`**
```json
{
  "orderId": 42,
  "orderNumber": "ORD-1234567890"
}
```

**Error `400`** — cart kosong  
**Error `409`** — stok tidak cukup

---

## Orders 🔒

### List Orders
`GET /orders`

**Query params**: `page`, `size`

**Response `200`** — `PageResponse<OrderResponse>`
```json
{
  "id": 42,
  "orderNumber": "ORD-1234567890",
  "totalAmount": 9998000.00,
  "status": "CREATED",
  "orderDate": "2026-06-06T10:00:00Z",
  "recipientName": "Alice",
  "recipientPhone": "08123456789",
  "shippingAddress": "Jl. Sudirman No. 1, Jakarta",
  "notes": "Tolong bubble wrap",
  "items": null
}
```

> `items` hanya terisi pada endpoint detail.

**`status` values**: `CREATED` → `PROCESSING` → `SHIPPED` → `COMPLETED` | `CANCELLED`

---

### Order Detail
`GET /orders/{id}`

**Response `200`** — `OrderResponse` dengan `items`:
```json
{
  "items": [
    {
      "id": 1,
      "productId": 10,
      "productName": "Smart TV",
      "variantId": 1,
      "variantSku": "TV-55-BLK",
      "variantLabel": "Black",
      "price": 4999000.00,
      "quantity": 2,
      "subtotal": 9998000.00
    }
  ]
}
```

---

## Order Requests 🔒

### List My Requests
`GET /orders/requests`

**Query params**: `page`, `size`

**Response `200`** — `PageResponse<OrderRequestResponse>`
```json
{
  "id": 1,
  "orderId": 42,
  "orderNumber": "ORD-1234567890",
  "requestType": "CANCELLATION",
  "status": "PENDING",
  "reason": "Salah pesan",
  "notes": null,
  "requestedAmount": 9998000.00,
  "adminNotes": null,
  "resolvedAt": null,
  "createdAt": "2026-06-06T11:00:00Z"
}
```

---

### Request Detail
`GET /orders/requests/{id}`

**Response `200`** — `OrderRequestResponse`

---

### Create Request
`POST /orders/requests`

**Request**
```json
{
  "orderId": 42,
  "type": "CANCELLATION",
  "reason": "Salah pesan",
  "notes": "Mohon segera diproses",
  "requestedAmount": 9998000.00
}
```

> `type`: `CANCELLATION` (untuk order status `CREATED`/`PROCESSING`) atau `REFUND` (untuk `SHIPPED`/`COMPLETED`)  
> `requestedAmount` opsional, default = `totalAmount` order

**Response `200`** — `OrderRequestResponse`

---

## Reviews 🔒

### Add Review
`POST /reviews`

**Request**
```json
{
  "productId": 10,
  "rating": 5,
  "comment": "Produk sangat bagus!"
}
```

> Hanya bisa review produk yang sudah pernah dibeli (order status `COMPLETED`).

**Response `200`** — `ProductReviewResponse`

---

### My Reviews
`GET /reviews/me`

**Query params**: `page`, `size`

**Response `200`** — `PageResponse<ProductReviewResponse>`

---

## Wishlist 🔒

### List Wishlist
`GET /wishlist`

**Query params**: `page`, `size`

**Response `200`** — `PageResponse<WishlistResponse>`
```json
{
  "id": 1,
  "product": { "...ProductResponse..." },
  "createdAt": "2026-06-05T00:00:00Z"
}
```

---

### Add to Wishlist
`POST /wishlist/products/{productId}`

**Response `200`** — `WishlistResponse`

**Error `409`** — sudah ada di wishlist

---

### Remove from Wishlist
`DELETE /wishlist/products/{productId}`

**Response `200`** — `data: null`

---

### Check Wishlist Status
`GET /wishlist/products/{productId}/check`

**Response `200`**
```json
{ "inWishlist": true }
```

---

## Files

### Get Product Image
`GET /files/products/{filename}` 🔓

**Response** — binary image file (`image/jpeg`, `image/png`, dll.)

---

---

## Admin Endpoints 🔒🛡️

> Semua endpoint di bawah ini memerlukan role `ADMIN`.

---

## Admin — Categories

### List Categories
`GET /admin/categories`

### Create Category
`POST /admin/categories`
```json
{ "name": "Fashion", "description": "Pakaian & aksesoris", "status": "ACTIVE" }
```

### Update Category
`PUT /admin/categories/{id}`
```json
{ "name": "Fashion Updated", "description": "...", "status": "ACTIVE" }
```

### Deactivate Category
`DELETE /admin/categories/{id}`

**Response semua** — `CategoryResponse`

---

## Admin — Products

### List Products
`GET /admin/products`

**Query params**: `categoryId`, `keyword`, `page`, `size`, `sort`

### Create Product
`POST /admin/products`
```json
{
  "categoryId": 1,
  "name": "Smart TV 55\"",
  "description": "4K HDR Smart TV",
  "price": 4999000.00,
  "stock": 50,
  "minimumStockLevel": 5,
  "warehouseId": 1,
  "imageUrl": null,
  "status": "ACTIVE"
}
```

### Update Product
`PUT /admin/products/{id}` — body sama dengan Create

### Deactivate Product
`DELETE /admin/products/{id}`

### Update Stock
`PATCH /admin/products/{id}/stock`
```json
{ "stock": 100 }
```

### Update Status
`PATCH /admin/products/{id}/status`
```json
{ "status": "INACTIVE" }
```

### Update Image (legacy — single)
`PATCH /admin/products/{id}/image`  
`Content-Type: multipart/form-data`  
Field: `file` (image file)

### Import Products
`POST /admin/products/import`  
`Content-Type: multipart/form-data`  
Field: `file` (`.csv` atau `.xlsx`)

**Response `200`**
```json
{
  "processed": 10,
  "created": 8,
  "updated": 2,
  "skipped": 0,
  "errors": []
}
```

### Export Products
`GET /admin/products/export?format=csv` — returns file download  
`format`: `csv` (default) atau `xlsx`

---

## Admin — Product Images (Multi-image)

### Add Image
`POST /admin/products/{productId}/images`  
`Content-Type: multipart/form-data`

| Field | Type | Description |
|-------|------|-------------|
| `file` | File | Image file |
| `primary` | boolean | Set as primary (default `false`) |

**Response `200`** — `ProductResponse` (dengan `images` terupdate)

---

### Set Primary Image
`PATCH /admin/products/{productId}/images/{imageId}/primary`

**Response `200`** — `ProductResponse`

---

### Delete Image
`DELETE /admin/products/{productId}/images/{imageId}`

**Response `200`** — `ProductResponse`

---

## Admin — Product Variants

### Add Variant
`POST /admin/products/{productId}/variants`
```json
{
  "sku": "TV-55-BLK",
  "size": null,
  "color": "Black",
  "price": null,
  "stock": 20,
  "status": "ACTIVE"
}
```
> `price` opsional — jika null, pakai harga produk.  
> `size` dan `color` keduanya opsional namun setidaknya satu harus diisi.

**Response `200`** — `ProductResponse` (dengan `variants` terupdate, `stock` produk ter-sync)

**Error `409`** — SKU sudah terdaftar

---

### Update Variant
`PUT /admin/products/{productId}/variants/{variantId}` — body sama dengan Add

**Response `200`** — `ProductResponse`

---

### Delete Variant
`DELETE /admin/products/{productId}/variants/{variantId}`

**Response `200`** — `ProductResponse`

---

## Admin — Orders

### List Orders
`GET /admin/orders`

**Query params**: `status`, `orderNumber`, `page`, `size`

### Order Detail
`GET /admin/orders/{id}`

**Response** — `OrderResponse` (dengan `items`)

### Update Order Status
`PUT /admin/orders/{id}/status`
```json
{ "status": "PROCESSING" }
```

**Valid transitions**:
- `CREATED` → `PROCESSING` atau `CANCELLED`
- `PROCESSING` → `SHIPPED` atau `CANCELLED`
- `SHIPPED` → `COMPLETED`

> Saat transisi ke `CANCELLED`, stok produk & varian dikembalikan secara atomik.

### Generate Invoice (PDF)
`GET /admin/orders/{id}/invoice`

**Response** — `application/pdf` file download

---

## Admin — Order Requests

### List Requests
`GET /admin/order-requests`

**Query params**: `status` (`PENDING`/`APPROVED`/`REJECTED`/`COMPLETED`), `type` (`CANCELLATION`/`REFUND`), `page`, `size`

### Resolve Request
`PUT /admin/order-requests/{id}`
```json
{
  "status": "APPROVED",
  "adminNotes": "Permintaan disetujui"
}
```

> `status`: `APPROVED` atau `REJECTED`  
> Saat approve `CANCELLATION`, order dibatalkan dan stok dikembalikan.

**Response `200`** — `OrderRequestResponse`

---

## Admin — Warehouses

### List All Warehouses
`GET /admin/warehouses`

### List Active Warehouses
`GET /admin/warehouses/active`

### Create Warehouse
`POST /admin/warehouses`
```json
{
  "code": "WH-JKT",
  "name": "Jakarta Warehouse",
  "location": "Jakarta Selatan",
  "status": "ACTIVE"
}
```

### Update Warehouse
`PUT /admin/warehouses/{id}` — body sama dengan Create

### Deactivate Warehouse
`DELETE /admin/warehouses/{id}`

**Response semua** — `WarehouseResponse`
```json
{
  "id": 1,
  "code": "WH-JKT",
  "name": "Jakarta Warehouse",
  "location": "Jakarta Selatan",
  "status": "ACTIVE"
}
```

---

## Admin — Reviews

### Search Reviews
`GET /admin/reviews`

**Query params**: `rating` (1–5), `keyword`, `page`, `size`

**Response `200`** — `PageResponse<ProductReviewResponse>`

### Delete Review
`DELETE /admin/reviews/{id}`

**Response `200`** — `data: null`

---

## Admin — Reports

### Summary Report
`GET /admin/reports/summary`

**Response `200`**
```json
{
  "totalRevenue": 99980000.00,
  "totalOrders": 42,
  "totalProducts": 150,
  "totalCustomers": 30,
  "lowStockProducts": [ { "...ProductResponse..." } ]
}
```

### Dashboard
`GET /admin/reports/dashboard`

**Response `200`**
```json
{
  "statusBreakdown": [
    { "status": "CREATED", "count": 5 }
  ],
  "salesSeries": [
    { "date": "2026-06-01", "total": 5000000.00, "orderCount": 3 }
  ],
  "latestOrders": [ { "...OrderResponse..." } ],
  "warehouseStock": [ { "...WarehouseStockResponse..." } ],
  "lowStockProducts": [ { "...ProductResponse..." } ]
}
```

### Export Report
`GET /admin/reports/export?format=csv`

`format`: `csv`, `xlsx`, `pdf`

**Response** — file download

---

## Admin — Email

### Send Email
`POST /admin/emails/send`
```json
{
  "to": "user@example.com",
  "subject": "Pesanan Anda",
  "body": "Pesanan ORD-001 telah diproses."
}
```

**Response `200`** — `data: null`

---

## Authentication Header

Semua endpoint berlabel 🔒 memerlukan:
```
Authorization: Bearer <accessToken>
```

Saat `accessToken` expired (2 jam), gunakan `POST /auth/refresh` untuk mendapatkan token baru tanpa login ulang.

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | OK |
| `400` | Bad request / validasi gagal / business rule violation |
| `401` | Token tidak ada atau invalid |
| `403` | Role tidak memiliki akses |
| `404` | Resource tidak ditemukan |
| `409` | Conflict (email duplikat, SKU duplikat, sudah di wishlist) |

---

## Legend

| Symbol | Meaning |
|--------|---------|
| 🔓 | Public — tidak perlu auth |
| 🔒 | Perlu login (`Authorization: Bearer`) |
| 🛡️ | Perlu role ADMIN |
