# Ecommerce Backend API

Backend REST API untuk aplikasi ecommerce dengan fitur lengkap: autentikasi, katalog produk, keranjang belanja, checkout, manajemen pesanan, serta fitur admin untuk produk, pesanan, gudang, dan pelaporan.

## Tech Stack

- **Java 17** dengan Spring Boot 3.3.2
- **Build**: Maven
- **Database**: MySQL (production), H2 (test)
- **Authentication**: Spring Security + JWT (jjwt 0.12.6), OAuth2 Client (Google)
- **Database Migration**: Flyway
- **API Documentation**: springdoc OpenAPI (Swagger UI)
- **Email**: Spring Mail (Resend)
- **Export**: Apache POI (Excel), PDFBox (PDF)

## Features

### Customer Features
- **Authentication**: Register, Login, Forgot/Reset Password, Email Verification, OAuth2 Google
- **Product Catalog**: Browse, search, filter by category, pagination, sorting
- **Categories**: View all categories
- **Shopping Cart**: Add, update, remove items, clear cart
- **Checkout**: Validate cart, deduct stock, create order, clear cart
- **Orders**: View orders, order details
- **Order Requests**: Request refund or cancellation
- **Product Reviews**: Add reviews, view product reviews, my reviews
- **Wishlist**: Add/remove products, view wishlist
- **User Profile**: Update profile

### Admin Features
- **Product Management**: CRUD products, update stock/status, upload image, import/export (CSV/Excel)
- **Category Management**: CRUD categories
- **Order Management**: View orders, update status, generate invoice (PDF)
- **Order Requests**: View and resolve refund/cancellation requests
- **Warehouse Management**: CRUD warehouses
- **Product Reviews**: Search and delete reviews
- **Reports & Dashboard**: Summary reports, dashboard with analytics, export reports (CSV/Excel/PDF)
- **Email**: Send emails to users

## API Endpoints

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Login |
| POST | `/auth/forgot-password` | Request password reset |
| POST | `/auth/reset-password` | Reset password |
| GET | `/auth/verify-email` | Verify email |
| GET | `/auth/me` | Get current user |
| GET | `/auth/oauth2/success` | OAuth2 login success |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | List products (customer) - includes `isInWishlist` field for authenticated users |
| GET | `/products/search` | Search products - includes `isInWishlist` field for authenticated users |
| GET | `/products/{id}` | Product detail - includes `isInWishlist` field for authenticated users |
| GET | `/products/{productId}/reviews` | Product reviews |

**Note**: Product endpoints now return `isInWishlist` boolean field. For authenticated users, this indicates if the product is in their wishlist. For unauthenticated users, this field will always be `false`.

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/categories` | List categories (customer) |

### Cart
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/cart` | Get cart |
| POST | `/cart/items` | Add item |
| PUT | `/cart/items/{itemId}` | Update item |
| DELETE | `/cart/items/{itemId}` | Remove item |
| DELETE | `/cart` | Clear cart |

### Checkout
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/checkout` | Checkout |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/orders` | List orders |
| GET | `/orders/{id}` | Order detail |
| GET | `/orders/requests` | List order requests |
| GET | `/orders/requests/{id}` | Order request detail |
| POST | `/orders/requests` | Create order request |

### Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/reviews` | Add review |
| GET | `/reviews/me` | My reviews |

### Wishlist
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/wishlist` | List wishlist |
| POST | `/wishlist/products/{productId}` | Add to wishlist |
| DELETE | `/wishlist/products/{productId}` | Remove from wishlist |
| GET | `/wishlist/products/{productId}/check` | Check wishlist status |

### User
| Method | Endpoint | Description |
|--------|----------|-------------|
| PATCH | `/users/profile` | Update profile |

### Admin - Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/products` | List products |
| POST | `/admin/products` | Create product |
| PUT | `/admin/products/{id}` | Update product |
| DELETE | `/admin/products/{id}` | Deactivate product |
| PATCH | `/admin/products/{id}/stock` | Update stock |
| PATCH | `/admin/products/{id}/status` | Update status |
| PATCH | `/admin/products/{id}/image` | Update image |
| POST | `/admin/products/import` | Import products |
| GET | `/admin/products/export` | Export products |

### Admin - Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/categories` | List categories |
| POST | `/admin/categories` | Create category |
| PUT | `/admin/categories/{id}` | Update category |
| DELETE | `/admin/categories/{id}` | Deactivate category |

### Admin - Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/orders` | List orders |
| GET | `/admin/orders/{id}` | Order detail |
| PUT | `/admin/orders/{id}/status` | Update status |
| GET | `/admin/orders/{id}/invoice` | Generate invoice (PDF) |

### Admin - Order Requests
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/order-requests` | List requests |
| PUT | `/admin/order-requests/{id}` | Resolve request |

### Admin - Warehouses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/warehouses` | List warehouses |
| GET | `/admin/warehouses/active` | List active warehouses |
| POST | `/admin/warehouses` | Create warehouse |
| PUT | `/admin/warehouses/{id}` | Update warehouse |
| DELETE | `/admin/warehouses/{id}` | Deactivate warehouse |

### Admin - Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/reviews` | Search reviews |
| DELETE | `/admin/reviews/{id}` | Delete review |

### Admin - Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/reports/summary` | Summary report |
| GET | `/admin/reports/dashboard` | Dashboard |
| GET | `/admin/reports/export` | Export report |

### Admin - Email
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/admin/emails/send` | Send email |

### Files
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/files/products/{filename}` | Get product image |

## Getting Started

### Prerequisites
- Java 17
- MySQL 8.0+ (for production)
- Maven 3.8+

### Configuration

Set environment variables:
```bash
# Database
export DB_URL=jdbc:mysql://localhost:3306/ecommerce
export DB_USERNAME=root
export DB_PASSWORD=your_password

# JWT
export JWT_SECRET=your_jwt_secret_key

# Resend (Email)
export RESEND_API_KEY=re_xxxxx

# Google OAuth2
export google.client-id=your_google_client_id
export google.client-secret=your_google_client_secret
export OAUTH2_FRONTEND_REDIRECT_URI=http://localhost:3000/oauth2/redirect

# File Storage
export PRODUCT_IMAGES_DIR=/path/to/images
```

### Build & Run

```bash
# Build
mvn clean compile

# Run tests
mvn test

# Run locally
mvn spring-boot:run

# Build JAR
mvn clean package
```

### Access API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Project Structure

```
src/main/java/com/ecommerce/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── service/          # Business logic
├─�� repository/       # Data access
├── entity/           # JPA entities & enums
├── dto/              # Data Transfer Objects
│   ├── request/
│   └── response/
├── exception/        # Custom exceptions
├── mapper/           # Entity-DTO mappers
├── security/         # Security config, JWT, OAuth2
└── util/             # Utility classes
```

## Database Schema

Schema dikelola via Flyway migrations di `src/main/resources/db/migration/`.
Gunakan H2 untuk development/testing dan MySQL untuk production.

### Key Entities
- **User**: name, email, password, phone, role, status
- **Product**: name, description, price, stock, category, warehouse, imageUrl, status
- **Category**: name, description, status
- **Order**: orderNumber, recipientName, recipientPhone, shippingAddress, totalAmount, status
- **OrderItem**: productName (snapshot), price (snapshot), quantity
- **Cart** / **CartItem**: shopping cart functionality
- **OrderRequest**: type (REFUND/CANCELLATION), status, reason, notes
- **ProductReview**: rating, comment
- **Wishlist**: user-product relationship
- **Warehouse**: code, name, location, status

## Testing

Integration tests tersedia di `src/test/java/com/ecommerce` menggunakan H2 in-memory database.

```bash
# Run all tests
mvn test
```

## License

MIT License