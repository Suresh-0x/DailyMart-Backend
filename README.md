# 🛒 DailyMart E-Commerce Backend API

![Java 17](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot 3.2](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Stateless_Auth-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)
![Swagger UI](https://img.shields.io/badge/Swagger-OpenAPI_3.0-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Tests](https://img.shields.io/badge/Tests-100%25_Passing-brightgreen?style=for-the-badge)

A enterprise-grade, scalable Spring Boot RESTful API powering the **DailyMart** hyper-local grocery and supermarket platform. Built with security, high performance, and ease of integration in mind.

---

## 🌟 Key Features

- 🔐 **Authentication & Security**:
  - JWT Stateless Authentication with custom `JwtAuthFilter`.
  - Role-based authorization (`ROLE_USER`, `ROLE_ADMIN`, `ROLE_SELLER`).
  - Email verification tokens & password reset workflows.
  - Password encryption using `BCryptPasswordEncoder`.

- 🏬 **Product & Category Management**:
  - Hierarchical categories with display ordering and parent-child relations.
  - Auto-generated SEO-friendly slugs, SKU tracking, stock management.
  - Multi-image support integrated with **Cloudinary CDN**.
  - Advanced search and filtering by price range, category, and featured status.

- 🛒 **Shopping Cart & Wishlist**:
  - Real-time stock validation, automated item quantity capping, and delivery fee calculation.
  - Wishlist toggle and instant price summary breakdowns.

- 🎟️ **Coupon & Discount Engine**:
  - Percentage and Flat discount types.
  - Configurable minimum order values, maximum discount caps, usage limits, and expiration dates.
  - Live coupon validation API.

- 📦 **Order Management & Live Timeline Tracking**:
  - Multi-item checkout with automatic stock deduction and rollback on cancellation.
  - Granular lifecycle statuses (`PLACED` ➔ `CONFIRMED` ➔ `PROCESSING` ➔ `SHIPPED` ➔ `OUT_FOR_DELIVERY` ➔ `DELIVERED` / `CANCELLED`).
  - Real-time order tracking event history.
  - Automated HTML email order confirmation.

- 💳 **Payment Integration**:
  - **Razorpay** payment gateway integration for order generation and cryptographic HMAC-SHA256 signature verification.
  - Cash on Delivery (COD) and Online Payment options.

- 📊 **Admin Dashboard & Analytics**:
  - Total users, active inventory, cumulative revenue, and order distribution stats.
  - Recent orders timeline and top-selling product metrics.
  - User status activation / deactivation and order status updates.

- 🌱 **Automatic Data Initialization**:
  - Automatic seed of default roles, Super Admin account, starter product categories, sample inventory with imagery, and starter promotional coupons on first launch.

---

## 🏗️ Tech Stack & Architecture

- **Backend**: Java 17, Spring Boot 3.2.0 (Spring Data JPA, Spring Security, Spring Validation, Spring Mail, Spring Cache)
- **Database**: MySQL (Production/Dev), H2 In-Memory (Test Suite)
- **Image Cloud**: Cloudinary
- **Payment Processing**: Razorpay SDK
- **Documentation**: Springdoc OpenAPI 3.0 / Swagger UI
- **Build Tool**: Apache Maven 3.9+

---

## 🚀 Quick Start

### 1. Prerequisites
- **JDK 17** or higher
- **Maven 3.8+**
- **MySQL 8.0+**

### 2. Configuration
Update your database and external service credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dailymart_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

app.jwt.secret=YOUR_BASE64_256BIT_SECRET
app.jwt.access-token-expiry=86400000

cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET

razorpay.key-id=YOUR_RAZORPAY_KEY_ID
razorpay.key-secret=YOUR_RAZORPAY_SECRET
```

### 3. Run the Application

```bash
# Clone the repository
git clone https://github.com/Suresh-0x/DailyMart-Backend.git
cd DailyMart-Backend

# Build and run
mvn clean spring-boot:run
```

The application will start on **`http://localhost:8080/api`**.

### 4. Interactive API Documentation (Swagger)
Once the server is running, explore and test all APIs interactively via Swagger UI:
👉 **`http://localhost:8080/api/swagger-ui.html`**
Swagger API Docs JSON: **`http://localhost:8080/api/v3/api-docs`**

---

## 🔑 Default Administrator Credentials

Upon first startup, the database is automatically seeded with:

| Role | Email | Password |
| :--- | :--- | :--- |
| **Super Admin** | `admin@dailymart.com` | `Admin@12345` |

---

## 📡 API Reference Overview

### 🔐 Authentication (`/api/auth`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | Public | Register a new customer |
| `POST` | `/auth/login` | Public | Authenticate and get JWT Bearer Token |
| `GET` | `/auth/verify-email` | Public | Verify email address with token |
| `POST` | `/auth/forgot-password` | Public | Trigger password reset link email |
| `POST` | `/auth/reset-password` | Public | Reset password with token |

### 🏬 Products (`/api/products`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/products` | Public | Paginated product list (filter/sort) |
| `GET` | `/products/{id}` | Public | Get product details by ID |
| `GET` | `/products/slug/{slug}` | Public | Get product details by SEO slug |
| `GET` | `/products/category/{catId}` | Public | Get products in category |
| `GET` | `/products/search?q={query}` | Public | Search products by name/brand/description |
| `GET` | `/products/featured` | Public | Get featured catalog items |
| `POST` | `/products` | `ADMIN` | Create product with images |
| `PUT` | `/products/{id}` | `ADMIN` | Update product details |
| `DELETE` | `/products/{id}` | `ADMIN` | Soft delete product |

### 📂 Categories (`/api/categories`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/categories` | Public | List all active categories |
| `GET` | `/categories/root` | Public | List top-level root categories |
| `GET` | `/categories/{id}` | Public | Get category details with children |
| `POST` | `/categories` | `ADMIN` | Create new category |
| `PUT` | `/categories/{id}` | `ADMIN` | Update category details |
| `DELETE` | `/categories/{id}` | `ADMIN` | Soft delete category |

### 🛒 Cart & Wishlist (`/api/cart`, `/api/wishlist`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/cart` | User | Get current user's shopping cart |
| `POST` | `/cart/add` | User | Add item to cart with quantity validation |
| `PUT` | `/cart/update/{itemId}` | User | Update quantity for item in cart |
| `DELETE` | `/cart/remove/{itemId}` | User | Remove specific item from cart |
| `DELETE` | `/cart/clear` | User | Empty shopping cart |
| `GET` | `/wishlist` | User | Get user's saved wishlist |
| `POST` | `/wishlist/toggle/{productId}` | User | Toggle product in/out of wishlist |

### 🎟️ Coupons (`/api/coupons`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/coupons/active` | Public | List active public promotional coupons |
| `POST` | `/coupons/validate` | Public | Validate code and calculate discount amount |
| `GET` | `/coupons` | `ADMIN` | List all coupons |
| `POST` | `/coupons` | `ADMIN` | Create promotional coupon |
| `PUT` | `/coupons/{id}` | `ADMIN` | Update coupon rules |
| `DELETE` | `/coupons/{id}` | `ADMIN` | Deactivate coupon |

### 📦 Orders & Tracking (`/api/orders`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/orders/place` | User | Checkout and place order with address & coupon |
| `GET` | `/orders` | User | Get user's past order history |
| `GET` | `/orders/{id}` | User/Admin | Get detailed order invoice & breakdown |
| `GET` | `/orders/by-number/{orderNumber}` | User/Admin | Lookup order by order number string |
| `GET` | `/orders/{id}/tracking` | User/Admin | Get real-time event tracking timeline |
| `POST` | `/orders/{orderId}/cancel` | User | Cancel order and restore stock |

### 💳 Payments (`/api/payments`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/payments/razorpay/create/{orderNo}` | User | Initialize Razorpay payment order |
| `POST` | `/payments/razorpay/verify` | User | Verify cryptographic HMAC signature |

### 👤 Users & Addresses (`/api/users`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/users/profile` | User | Get current profile details |
| `PUT` | `/users/profile` | User | Update personal details & avatar |
| `POST` | `/users/change-password` | User | Change user password |
| `GET` | `/users/addresses` | User | Get saved shipping addresses |
| `POST` | `/users/addresses` | User | Add new address with default selector |
| `DELETE` | `/users/addresses/{id}` | User | Remove saved address |

### 👑 Admin Management (`/api/admin`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/admin/dashboard` | `ADMIN` | Dashboard metrics, revenue & order stats |
| `GET` | `/admin/users` | `ADMIN` | Paginated customer and seller directory |
| `PUT` | `/admin/users/{id}/toggle-status` | `ADMIN` | Lock / unlock user account |
| `GET` | `/admin/orders` | `ADMIN` | View and manage all platform orders |
| `PUT` | `/admin/orders/{id}/status` | `ADMIN` | Update order processing/delivery status |

---

## 🧪 Testing

Run the automated test suite with an embedded H2 database:

```bash
mvn clean test
```

All **26 test cases** cover authentication, cart operations, coupons, order placement, stock updates, and product catalog operations.

---

## 🐳 Docker Deployment

To build and run DailyMart Backend in a Docker container:

```bash
# Build Docker image
docker build -t dailymart-backend:1.0.0 .

# Run container
docker run -d -p 8080:8080 --name dailymart-api dailymart-backend:1.0.0
```

---

## 📄 License
This project is licensed under the Apache 2.0 License.
