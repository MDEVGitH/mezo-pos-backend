# Mezo POS Backend

**Vende mas. Piensa menos.**

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green?logo=springboot)
![License](https://img.shields.io/badge/License-Proprietary-blue)
![Build](https://img.shields.io/badge/Build-Maven-red?logo=apachemaven)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-purple)

---

## About

**Mezo POS** is a SaaS point-of-sale platform designed for restaurants, cafes, bakeries, bars, and food trucks in Colombia. It provides a complete backend for managing businesses, products, orders, sales, team members, reports, and analytics with a subscription-based pricing model.

Target market: Colombian small and medium food & beverage businesses that need a modern, mobile-first POS system with real-time reporting and multi-location support.

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 21** | Language (LTS, virtual threads ready) |
| **Spring Boot 3.4** | Framework |
| **Spring Security** | Authentication & Authorization (JWT + RBAC) |
| **Spring Data JPA / Hibernate** | ORM & Data Access |
| **H2 Database** | Development database (in-memory) |
| **PostgreSQL** | Production database (migration path) |
| **jjwt 0.12.6** | JWT token generation & validation |
| **Resend** | Transactional email (OTP verification) |
| **Wompi** | Payment gateway (Colombian payments) |
| **Cloudinary** | Product image upload & CDN |
| **MapStruct 1.6** | DTO mapping |
| **Lombok** | Boilerplate reduction |
| **Maven** | Build tool |
| **JUnit 5 + Mockito** | Testing |

---

## Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters) within a **modular monolith** structure.

```
+-----------------------------------------------------------+
|                     INFRASTRUCTURE                         |
|  (REST Controllers, JPA Repositories, Email, JWT, Wompi)  |
|                                                            |
|  +-----------------------------------------------------+  |
|  |                   APPLICATION                        |  |
|  |  (Use Cases / Services)                              |  |
|  |                                                      |  |
|  |  +-------------------------------------------------+ |  |
|  |  |                  DOMAIN                          | |  |
|  |  |  (Entities, Value Objects, Ports)                | |  |
|  |  |  No external dependencies                       | |  |
|  |  +-------------------------------------------------+ |  |
|  +-----------------------------------------------------+  |
+-----------------------------------------------------------+
```

**Dependency rule:**
- **Domain** depends on nothing external
- **Application** depends only on Domain
- **Infrastructure** depends on Application and Domain

**Ports & Adapters:**
- **Driving ports** (inbound): REST controllers invoke use cases
- **Driven ports** (outbound): Interfaces in domain (e.g., `OrderRepository`, `EmailService`, `PaymentGateway`)
- **Adapters**: JPA repositories, Resend email, Wompi payments, Cloudinary images

---

## Project Structure

```
com.mezo.pos/
|-- shared/                          # Shared code across modules
|   |-- domain/
|   |   |-- valueobject/             # Money, Email, PhoneNumber, Currency, TimeRange
|   |   |-- entity/                  # BaseEntity (id, createdAt, updatedAt, deleted)
|   |   +-- exception/               # DomainException, NotFoundException, etc.
|   |-- application/                 # EventPublisher, TimeRangeResolver
|   +-- infrastructure/
|       |-- security/                # JWT, SecurityConfig, BusinessAccessFilter
|       |-- config/                  # AppConfig, CorsConfig
|       +-- exception/               # GlobalExceptionHandler, ErrorResponse
|
|-- auth/                            # Authentication module
|   |-- domain/entity/               # User, OtpToken
|   |-- domain/port/                 # UserRepository, OtpRepository, EmailService
|   |-- domain/enums/                # Role (ADMIN, CASHIER, WAITER, KITCHEN)
|   |-- application/                 # Register, Login, VerifyOtp, RefreshToken
|   +-- infrastructure/              # JPA adapters, ResendEmailService, AuthController
|
|-- business/                        # Business management module
|   |-- domain/entity/               # Business
|   |-- domain/enums/                # BusinessType (RESTAURANT, CAFE, BAR, etc.)
|   |-- application/                 # CreateBusiness, UpdateBusiness, GetBusiness
|   +-- infrastructure/              # JPA adapters, BusinessController
|
|-- product/                         # Product & Category module
|   |-- domain/entity/               # Product, Category
|   |-- domain/port/                 # ProductRepository, CategoryRepository, ImageStorage
|   |-- domain/enums/                # ImageType (EMOJI, IMAGE)
|   |-- application/                 # CRUD products, CRUD categories, image upload
|   +-- infrastructure/              # JPA adapters, Cloudinary, Controllers
|
|-- table/                           # Restaurant tables module
|   |-- domain/entity/               # RestaurantTable
|   |-- application/                 # CreateTable, DeleteTable, ListTables
|   +-- infrastructure/              # JPA adapters, TableController
|
|-- order/                           # Orders module (aggregate root)
|   |-- domain/entity/               # Order, OrderLine
|   |-- domain/enums/                # OrderStatus, PaymentMethod
|   |-- application/                 # Create, AddLines, RemoveLine, UpdateStatus
|   +-- infrastructure/              # JPA adapters, OrderController
|
|-- sale/                            # Sales module
|   |-- domain/entity/               # Sale
|   |-- application/                 # CloseSale, GetSale, ListSales
|   +-- infrastructure/              # JPA adapters, SaleController
|
|-- report/                          # Reports module (PRO/ELITE only)
|   |-- domain/model/                # SalesTotal, SalesCount, PeakHour, TopProduct, PaymentMethodStats
|   |-- domain/port/                 # ReportRepository
|   |-- application/                 # GetSalesTotal, GetSalesCount, GetPeakHour, etc.
|   +-- infrastructure/              # JpaReportRepository, ReportController, DTOs
|
|-- analytics/                       # Analytics module (PRO/ELITE only)
|   |-- domain/model/                # TimeSeriesPoint
|   |-- domain/port/                 # AnalyticsRepository
|   |-- application/                 # GetAnalyticsUseCase
|   +-- infrastructure/              # JpaAnalyticsRepository, AnalyticsController, DTOs
|
|-- team/                            # Team management module
|   |-- domain/entity/               # TeamMember, Invitation
|   |-- domain/enums/                # InvitationStatus (PENDING, ACCEPTED, EXPIRED)
|   |-- application/                 # Invite, Remove, UpdateRole, ListTeam
|   +-- infrastructure/              # JPA adapters, TeamController
|
+-- plan/                            # Plans & Subscriptions module
    |-- domain/entity/               # Plan, Subscription
    |-- domain/enums/                # PlanType, SubscriptionStatus
    |-- domain/service/              # PlanEnforcer
    |-- application/                 # GetCurrentPlan, Subscribe, HandleWebhook
    +-- infrastructure/              # JPA adapters, WompiPaymentGateway, Controllers
```

---

## Getting Started

### Prerequisites

- **Java 21** (LTS)
- **Maven 3.9+** (or use the included Maven Wrapper `./mvnw`)

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | Yes (production) | `default-dev-secret-...` | HMAC secret for JWT signing (min 256 bits) |
| `RESEND_API_KEY` | Yes (production) | _(empty)_ | Resend.com API key for OTP emails |
| `WOMPI_PUBLIC_KEY` | Yes (production) | _(empty)_ | Wompi public key |
| `WOMPI_PRIVATE_KEY` | Yes (production) | _(empty)_ | Wompi private key |
| `WOMPI_EVENTS_SECRET` | Yes (production) | _(empty)_ | Wompi webhook HMAC secret |
| `WOMPI_REDIRECT_URL` | No | `http://localhost:3000/plans/success` | Post-payment redirect URL |
| `CLOUDINARY_CLOUD_NAME` | Yes (production) | _(empty)_ | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Yes (production) | _(empty)_ | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Yes (production) | _(empty)_ | Cloudinary API secret |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:3000` | Allowed CORS origins |

### Running Locally

```bash
# Clone the repository
git clone https://github.com/your-org/mezo-pos-backend.git
cd mezo-pos-backend

# Run with Maven Wrapper (no external Maven required)
./mvnw spring-boot:run

# Or build and run the JAR
./mvnw clean package -DskipTests
java -jar target/mezo-pos-0.0.1-SNAPSHOT.jar
```

The application starts on `http://localhost:8080` with an in-memory H2 database.

**H2 Console:** `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:mezo`
- Username: `sa`
- Password: _(empty)_

---

## API Documentation

Base URL: `http://localhost:8080/api/v1`

All timestamps use ISO 8601 format. All IDs are UUIDs. Pagination uses `?page=0&size=20`.

### Auth

#### POST /api/v1/auth/register

Register a new user. Creates a PRO plan with 30-day free trial.

- **Auth required:** No

```json
// Request
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

// Response 201
{
  "message": "OTP enviado a user@example.com",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### POST /api/v1/auth/verify-otp

Verify email with OTP code (6 digits, expires in 10 minutes).

- **Auth required:** No

```json
// Request
{
  "email": "user@example.com",
  "code": "482931"
}

// Response 200
{
  "message": "Email verificado correctamente"
}
```

#### POST /api/v1/auth/resend-otp

Resend OTP code via email.

- **Auth required:** No

```json
// Request
{
  "email": "user@example.com"
}

// Response 200
{
  "message": "OTP reenviado a user@example.com"
}
```

#### POST /api/v1/auth/login

Login with email and password. Returns JWT access token (8h) and refresh token (7d).

- **Auth required:** No

```json
// Request
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

// Response 200
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "role": "ADMIN",
    "plan": "PRO",
    "emailVerified": true,
    "businesses": []
  }
}
```

#### POST /api/v1/auth/refresh

Refresh an expired access token.

- **Auth required:** No (uses refresh token)

```json
// Request
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}

// Response 200
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

---

### Business

#### POST /api/v1/businesses

Create a new business (onboarding). SEMILLA/PRO: max 1 business. ELITE: unlimited.

- **Auth required:** Yes (ADMIN)

```json
// Request
{
  "name": "Cafe Mezo",
  "type": "CAFE",
  "phone": "+573001234567",
  "address": "Calle 85 #15-20",
  "city": "Bogota",
  "country": "Colombia",
  "openAt": "07:00",
  "closeAt": "22:00",
  "tableCount": 4
}

// Response 201
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "name": "Cafe Mezo",
  "type": "CAFE",
  "phone": "+573001234567",
  "address": "Calle 85 #15-20",
  "city": "Bogota",
  "country": "Colombia",
  "openAt": "07:00",
  "closeAt": "22:00",
  "tableCount": 4,
  "open": false,
  "createdAt": "2026-04-23T10:30:00Z"
}
```

#### GET /api/v1/businesses

List all businesses owned by the authenticated user.

- **Auth required:** Yes

```json
// Response 200
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "name": "Cafe Mezo",
    "type": "CAFE",
    "open": true,
    "createdAt": "2026-04-23T10:30:00Z"
  }
]
```

#### GET /api/v1/businesses/{id}

Get business details by ID.

- **Auth required:** Yes

#### PUT /api/v1/businesses/{id}

Update business details.

- **Auth required:** Yes (ADMIN)

```json
// Request
{
  "name": "Cafe Mezo - Sede Norte",
  "openAt": "06:00",
  "closeAt": "23:00"
}
```

#### DELETE /api/v1/businesses/{id}

Soft delete a business.

- **Auth required:** Yes (ADMIN)
- **Response:** 204 No Content

---

### Products

All product endpoints are under `/api/v1/businesses/{businessId}/products`.

#### POST /api/v1/businesses/{businessId}/products

Create a new product.

- **Auth required:** Yes (ADMIN)

```json
// Request
{
  "name": "Cappuccino",
  "price": 8500,
  "currency": "COP",
  "description": "Espresso con leche espumada",
  "ingredients": "cafe, leche",
  "imageType": "EMOJI",
  "image": "☕",
  "categoryId": "770e8400-e29b-41d4-a716-446655440000"
}

// Response 201
{
  "id": "880e8400-e29b-41d4-a716-446655440000",
  "name": "Cappuccino",
  "price": 8500,
  "currency": "COP",
  "description": "Espresso con leche espumada",
  "ingredients": "cafe, leche",
  "imageType": "EMOJI",
  "image": "☕",
  "available": true,
  "categoryId": "770e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-04-23T10:30:00Z"
}
```

#### GET /api/v1/businesses/{businessId}/products

List products. Optional filter: `?categoryId=xxx`.

- **Auth required:** Yes

#### GET /api/v1/businesses/{businessId}/products/{id}

Get product by ID.

- **Auth required:** Yes

#### PUT /api/v1/businesses/{businessId}/products/{id}

Update product.

- **Auth required:** Yes (ADMIN)

```json
// Request
{
  "name": "Cappuccino Grande",
  "price": 12000,
  "currency": "COP",
  "description": "Espresso doble con leche espumada",
  "ingredients": "cafe, leche",
  "imageType": "EMOJI",
  "image": "☕",
  "categoryId": "770e8400-e29b-41d4-a716-446655440000"
}
```

#### DELETE /api/v1/businesses/{businessId}/products/{id}

Soft delete a product.

- **Auth required:** Yes (ADMIN)
- **Response:** 204 No Content

#### PATCH /api/v1/businesses/{businessId}/products/{id}/toggle

Toggle product availability (available true/false).

- **Auth required:** Yes (ADMIN)

```json
// Response 200
{
  "id": "880e8400-e29b-41d4-a716-446655440000",
  "name": "Cappuccino",
  "available": false
}
```

#### POST /api/v1/businesses/{businessId}/products/upload-image

Upload a product image (multipart/form-data, max 3MB, jpg/png/webp).

- **Auth required:** Yes (ADMIN)

```
Content-Type: multipart/form-data
Body: file (max 3MB)
```

```json
// Response 200
{
  "imageUrl": "https://res.cloudinary.com/mezo/image/upload/v1/.../product.jpg"
}
```

---

### Categories

All category endpoints are under `/api/v1/businesses/{businessId}/categories`.

#### POST /api/v1/businesses/{businessId}/categories

Create a new category.

- **Auth required:** Yes (ADMIN)

```json
// Request
{
  "name": "Bebidas calientes",
  "icon": "☕"
}

// Response 201
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "name": "Bebidas calientes",
  "icon": "☕",
  "sortOrder": 0,
  "createdAt": "2026-04-23T10:30:00Z"
}
```

#### GET /api/v1/businesses/{businessId}/categories

List all categories for the business.

- **Auth required:** Yes

```json
// Response 200
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "name": "Bebidas calientes",
    "icon": "☕",
    "sortOrder": 0
  },
  {
    "id": "770e8401-e29b-41d4-a716-446655440001",
    "name": "Postres",
    "icon": "🍰",
    "sortOrder": 1
  }
]
```

#### PUT /api/v1/businesses/{businessId}/categories/{id}

Update a category.

- **Auth required:** Yes (ADMIN)

```json
// Request
{
  "name": "Bebidas frias",
  "icon": "🧊"
}
```

#### DELETE /api/v1/businesses/{businessId}/categories/{id}

Delete a category (fails if it has products).

- **Auth required:** Yes (ADMIN)
- **Response:** 204 No Content

---

### Tables

All table endpoints are under `/api/v1/businesses/{businessId}/tables`.

#### POST /api/v1/businesses/{businessId}/tables

Create a new table (auto-numbered).

- **Auth required:** Yes (ADMIN)

```json
// Response 201
{
  "id": "990e8400-e29b-41d4-a716-446655440000",
  "number": 5,
  "businessId": "660e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-04-23T10:30:00Z"
}
```

#### GET /api/v1/businesses/{businessId}/tables

List all tables for the business.

- **Auth required:** Yes

```json
// Response 200
[
  { "id": "990e8400-...", "number": 1 },
  { "id": "990e8401-...", "number": 2 },
  { "id": "990e8402-...", "number": 3 },
  { "id": "990e8403-...", "number": 4 }
]
```

#### DELETE /api/v1/businesses/{businessId}/tables/{id}

Soft delete a table.

- **Auth required:** Yes (ADMIN)
- **Response:** 204 No Content

---

### Orders

All order endpoints are under `/api/v1/businesses/{businessId}/orders`.

#### POST /api/v1/businesses/{businessId}/orders

Create a new order. `tableId` is optional (null = direct POS sale without a table).

- **Auth required:** Yes (ADMIN, CASHIER, WAITER)

```json
// Request - order from a table
{
  "tableId": "990e8400-e29b-41d4-a716-446655440000",
  "paymentMethod": "NEQUI",
  "tip": 2000,
  "lines": [
    { "productId": "880e8400-...", "quantity": 2 },
    { "productId": "880e8401-...", "quantity": 1 }
  ]
}

// Request - direct POS order (no table)
{
  "tableId": null,
  "paymentMethod": "CASH",
  "tip": 0,
  "lines": [
    { "productId": "880e8400-...", "quantity": 1 }
  ]
}

// Response 201
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "tableId": "990e8400-e29b-41d4-a716-446655440000",
  "status": "OPEN",
  "paymentMethod": "NEQUI",
  "tip": 2000,
  "total": 27000,
  "currency": "COP",
  "lines": [
    {
      "id": "line-01",
      "productId": "880e8400-...",
      "productName": "Cappuccino",
      "unitPrice": 8500,
      "quantity": 2,
      "subtotal": 17000
    },
    {
      "id": "line-02",
      "productId": "880e8401-...",
      "productName": "Brownie",
      "unitPrice": 8000,
      "quantity": 1,
      "subtotal": 8000
    }
  ],
  "createdBy": "550e8400-...",
  "createdAt": "2026-04-24T14:30:00Z"
}
```

#### GET /api/v1/businesses/{businessId}/orders

List orders. Optional filters: `?status=OPEN&tableId=xxx`.

- **Auth required:** Yes (ADMIN, CASHIER, WAITER, KITCHEN)

#### GET /api/v1/businesses/{businessId}/orders/{id}

Get order details.

- **Auth required:** Yes (ADMIN, CASHIER, WAITER, KITCHEN)

#### POST /api/v1/businesses/{businessId}/orders/{id}/lines

Add lines to an existing order (must be OPEN).

- **Auth required:** Yes (ADMIN, CASHIER, WAITER)

```json
// Request
{
  "lines": [
    { "productId": "880e8402-...", "quantity": 1 },
    { "productId": "880e8400-...", "quantity": 1 }
  ]
}

// Response 200 - returns updated order with all lines
{
  "id": "aa0e8400-...",
  "status": "OPEN",
  "total": 42500,
  "currency": "COP",
  "lines": [
    { "id": "line-01", "productName": "Cappuccino", "unitPrice": 8500, "quantity": 2, "subtotal": 17000 },
    { "id": "line-02", "productName": "Brownie", "unitPrice": 8000, "quantity": 1, "subtotal": 8000 },
    { "id": "line-03", "productName": "Limonada", "unitPrice": 7000, "quantity": 1, "subtotal": 7000 },
    { "id": "line-04", "productName": "Cappuccino", "unitPrice": 8500, "quantity": 1, "subtotal": 8500 }
  ]
}
```

> Adding the same product twice creates separate lines (does not merge quantity).

#### DELETE /api/v1/businesses/{businessId}/orders/{id}/lines/{lineId}

Remove a line from an order (must be OPEN).

- **Auth required:** Yes (ADMIN, CASHIER, WAITER)
- **Response:** 200 with updated order

#### PATCH /api/v1/businesses/{businessId}/orders/{id}/status

Update order status.

- **Auth required:** Yes (ADMIN, CASHIER for close; ADMIN, KITCHEN for status changes)

```json
// Request
{
  "status": "PREPARING"
}

// Response 200
{
  "id": "aa0e8400-...",
  "status": "PREPARING"
}
```

**Order status flow:** `OPEN` -> `PREPARING` -> `READY` -> `DELIVERED` -> (closed as Sale)

---

### Sales

All sale endpoints are under `/api/v1/businesses/{businessId}/sales`.

#### POST /api/v1/businesses/{businessId}/sales

Close a sale. Supports two flows:

**Flow A - Close existing order (from table):**

- **Auth required:** Yes (ADMIN, CASHIER)

```json
// Request
{
  "orderId": "aa0e8400-e29b-41d4-a716-446655440000"
}

// Response 201
{
  "id": "bb0e8400-e29b-41d4-a716-446655440000",
  "orderId": "aa0e8400-...",
  "total": 27000,
  "tip": 2000,
  "currency": "COP",
  "paymentMethod": "NEQUI",
  "tableId": "990e8400-...",
  "closedBy": "550e8400-...",
  "createdAt": "2026-04-24T15:15:00Z"
}
```

**Flow B - Direct POS sale (no prior order):**

```json
// Request
{
  "paymentMethod": "CASH",
  "tip": 0,
  "lines": [
    { "productId": "880e8400-...", "quantity": 2 },
    { "productId": "880e8401-...", "quantity": 1 }
  ]
}

// Response 201
{
  "id": "bb0e8401-...",
  "orderId": "aa0e8401-...",
  "total": 25000,
  "tip": 0,
  "currency": "COP",
  "paymentMethod": "CASH",
  "tableId": null,
  "closedBy": "550e8400-...",
  "createdAt": "2026-04-24T15:20:00Z"
}
```

#### GET /api/v1/businesses/{businessId}/sales

List sales. Optional filters: `?from=2026-04-01&to=2026-04-23`.

- **Auth required:** Yes (ADMIN, CASHIER)

#### GET /api/v1/businesses/{businessId}/sales/{id}

Get sale details.

- **Auth required:** Yes (ADMIN, CASHIER)

---

### Reports

All report endpoints are under `/api/v1/businesses/{businessId}/reports`. Requires **PRO** or **ELITE** plan.

All endpoints accept a `?time=` query parameter:

| Value | Period |
|-------|--------|
| `DAY` | Today (00:00 to now) |
| `WEEK` | Last 7 days |
| `MONTH` | Last 4 weeks |
| `QUARTER` | Last 3 months |
| `YEAR` | Last 12 months |

#### GET /api/v1/businesses/{businessId}/reports/sales?time=MONTH

Total sales amount, tips, and count for the period.

- **Auth required:** Yes (ADMIN only)

```json
// Response 200
{
  "time": "MONTH",
  "from": "2026-03-27",
  "to": "2026-04-24",
  "totalSales": 2450000,
  "totalTips": 180000,
  "currency": "COP",
  "salesCount": 342
}
```

#### GET /api/v1/businesses/{businessId}/reports/count?time=WEEK

Number of sales in the period.

- **Auth required:** Yes (ADMIN only)

```json
// Response 200
{
  "time": "WEEK",
  "from": "2026-04-17",
  "to": "2026-04-24",
  "salesCount": 89
}
```

#### GET /api/v1/businesses/{businessId}/reports/peak-hour?time=MONTH

The busiest hour by sales count.

- **Auth required:** Yes (ADMIN only)

```json
// Response 200
{
  "time": "MONTH",
  "from": "2026-03-27",
  "to": "2026-04-24",
  "hour": 12,
  "label": "12:00 - 13:00",
  "salesCount": 89,
  "total": 680000,
  "currency": "COP"
}
```

#### GET /api/v1/businesses/{businessId}/reports/top-products?time=WEEK&limit=5

Top-selling products ranked by quantity sold.

- **Auth required:** Yes (ADMIN only)

```json
// Response 200
{
  "time": "WEEK",
  "from": "2026-04-17",
  "to": "2026-04-24",
  "products": [
    { "productId": "880e8400-...", "name": "Cappuccino", "totalSold": 156, "revenue": 1326000 },
    { "productId": "880e8401-...", "name": "Brownie", "totalSold": 98, "revenue": 784000 },
    { "productId": "880e8402-...", "name": "Limonada", "totalSold": 75, "revenue": 525000 },
    { "productId": "880e8403-...", "name": "Sandwich Club", "totalSold": 62, "revenue": 744000 },
    { "productId": "880e8404-...", "name": "Latte", "totalSold": 58, "revenue": 522000 }
  ]
}
```

#### GET /api/v1/businesses/{businessId}/reports/payment-methods?time=QUARTER

Payment method breakdown with percentages.

- **Auth required:** Yes (ADMIN only)

```json
// Response 200
{
  "time": "QUARTER",
  "from": "2026-01-24",
  "to": "2026-04-24",
  "methods": [
    { "method": "NEQUI", "count": 180, "total": 1520000, "percentage": 42.3 },
    { "method": "CASH", "count": 120, "total": 980000, "percentage": 28.2 },
    { "method": "DAVIPLATA", "count": 65, "total": 530000, "percentage": 15.3 },
    { "method": "BOLD", "count": 40, "total": 350000, "percentage": 9.4 },
    { "method": "TRANSFER", "count": 20, "total": 170000, "percentage": 4.7 }
  ]
}
```

---

### Analytics

Time series data for frontend charts. Requires **PRO** or **ELITE** plan.

#### GET /api/v1/businesses/{businessId}/analytics?time=DAY

- **Auth required:** Yes (ADMIN only)

| `time` | Grouping | Points |
|--------|----------|--------|
| `DAY` | By hour | Up to 24 |
| `WEEK` | By day | 7 |
| `MONTH` | By week | 4 |
| `QUARTER` | By month | 3 |
| `YEAR` | By month | 12 |

**DAY - grouped by hour:**

```json
// Response 200
{
  "time": "DAY",
  "currency": "COP",
  "data": [
    { "label": "7:00", "date": null, "total": 85000, "count": 12 },
    { "label": "8:00", "date": null, "total": 120000, "count": 18 },
    { "label": "9:00", "date": null, "total": 95000, "count": 14 },
    { "label": "10:00", "date": null, "total": 60000, "count": 8 },
    { "label": "11:00", "date": null, "total": 45000, "count": 6 },
    { "label": "12:00", "date": null, "total": 180000, "count": 25 },
    { "label": "13:00", "date": null, "total": 150000, "count": 20 }
  ]
}
```

**WEEK - grouped by day:**

```json
// Response 200
{
  "time": "WEEK",
  "currency": "COP",
  "data": [
    { "label": "Lun", "date": "2026-04-20", "total": 350000, "count": 48 },
    { "label": "Mar", "date": "2026-04-21", "total": 420000, "count": 56 },
    { "label": "Mie", "date": "2026-04-22", "total": 380000, "count": 51 },
    { "label": "Jue", "date": "2026-04-23", "total": 290000, "count": 39 },
    { "label": "Vie", "date": "2026-04-24", "total": 180000, "count": 22 }
  ]
}
```

**MONTH - grouped by week:**

```json
// Response 200
{
  "time": "MONTH",
  "currency": "COP",
  "data": [
    { "label": "Sem 1", "date": "2026-03-30", "total": 890000, "count": 120 },
    { "label": "Sem 2", "date": "2026-04-06", "total": 920000, "count": 128 },
    { "label": "Sem 3", "date": "2026-04-13", "total": 850000, "count": 115 },
    { "label": "Sem 4", "date": "2026-04-20", "total": 780000, "count": 105 }
  ]
}
```

**YEAR - grouped by month:**

```json
// Response 200
{
  "time": "YEAR",
  "currency": "COP",
  "data": [
    { "label": "May 2025", "date": null, "total": 3200000, "count": 410 },
    { "label": "Jun 2025", "date": null, "total": 2900000, "count": 380 },
    { "label": "Jul 2025", "date": null, "total": 3100000, "count": 405 },
    { "label": "Ago 2025", "date": null, "total": 2800000, "count": 360 },
    { "label": "Sep 2025", "date": null, "total": 3000000, "count": 390 },
    { "label": "Oct 2025", "date": null, "total": 3300000, "count": 420 },
    { "label": "Nov 2025", "date": null, "total": 3500000, "count": 445 },
    { "label": "Dic 2025", "date": null, "total": 4200000, "count": 530 },
    { "label": "Ene 2026", "date": null, "total": 2600000, "count": 340 },
    { "label": "Feb 2026", "date": null, "total": 2700000, "count": 355 },
    { "label": "Mar 2026", "date": null, "total": 2900000, "count": 375 },
    { "label": "Abr 2026", "date": null, "total": 2800000, "count": 350 }
  ]
}
```

---

### Team

All team endpoints are under `/api/v1/businesses/{businessId}/team`.

#### POST /api/v1/businesses/{businessId}/team/invite

Invite a member to the business. If the user exists, they are added immediately. If not, a pending invitation is created.

- **Auth required:** Yes (ADMIN only)

```json
// Request
{
  "email": "cashier@example.com",
  "role": "CASHIER"
}

// Response 201 - user exists
{
  "userId": "cc0e8400-...",
  "email": "cashier@example.com",
  "role": "CASHIER",
  "businessId": "660e8400-...",
  "invitedBy": "550e8400-...",
  "createdAt": "2026-04-23T10:30:00Z"
}

// Response 201 - user does not exist (invitation created)
{
  "email": "newuser@example.com",
  "role": "WAITER",
  "businessId": "660e8400-...",
  "status": "PENDING",
  "invitedBy": "550e8400-...",
  "createdAt": "2026-04-23T10:30:00Z"
}
```

#### GET /api/v1/businesses/{businessId}/team

List all team members.

- **Auth required:** Yes (ADMIN only)

```json
// Response 200
[
  {
    "userId": "550e8400-...",
    "email": "user@example.com",
    "role": "ADMIN",
    "createdAt": "2026-04-23T10:30:00Z"
  },
  {
    "userId": "cc0e8400-...",
    "email": "cashier@example.com",
    "role": "CASHIER",
    "createdAt": "2026-04-23T11:00:00Z"
  }
]
```

#### PUT /api/v1/businesses/{businessId}/team/{userId}/role

Change a team member's role. Cannot change own role.

- **Auth required:** Yes (ADMIN only)

```json
// Request
{
  "role": "WAITER"
}

// Response 200
{
  "userId": "cc0e8400-...",
  "email": "cashier@example.com",
  "role": "WAITER"
}
```

#### DELETE /api/v1/businesses/{businessId}/team/{userId}

Remove a team member. Cannot remove self or the business owner.

- **Auth required:** Yes (ADMIN only)
- **Response:** 204 No Content

---

### Plans & Subscriptions

#### GET /api/v1/plans

List all available plans.

- **Auth required:** No

```json
// Response 200
[
  {
    "type": "SEMILLA",
    "maxTables": 4,
    "maxProducts": 25,
    "maxCategories": 3,
    "maxEmployees": 3,
    "maxBusinesses": 1,
    "reportsEnabled": false,
    "price": 39900,
    "currency": "COP",
    "trialDays": 0
  },
  {
    "type": "PRO",
    "maxTables": -1,
    "maxProducts": -1,
    "maxCategories": -1,
    "maxEmployees": -1,
    "maxBusinesses": 1,
    "reportsEnabled": true,
    "price": 99900,
    "currency": "COP",
    "trialDays": 30
  },
  {
    "type": "ELITE",
    "maxTables": -1,
    "maxProducts": -1,
    "maxCategories": -1,
    "maxEmployees": -1,
    "maxBusinesses": -1,
    "reportsEnabled": true,
    "price": 199900,
    "currency": "COP",
    "trialDays": 0
  }
]
```

#### GET /api/v1/plans/current

Get the current plan and subscription status for the authenticated user.

- **Auth required:** Yes (ADMIN)

```json
// Response 200
{
  "type": "PRO",
  "maxTables": -1,
  "maxProducts": -1,
  "maxCategories": -1,
  "maxEmployees": -1,
  "maxBusinesses": 1,
  "reportsEnabled": true,
  "price": 99900,
  "currency": "COP",
  "trialDays": 30,
  "planStartedAt": "2026-04-01T00:00:00Z",
  "planExpiresAt": "2026-05-01T00:00:00Z",
  "isTrialActive": true,
  "subscription": {
    "status": "TRIAL",
    "wompiSubscriptionId": null
  }
}
```

#### POST /api/v1/plans/subscribe

Start a subscription or change plan. Returns a Wompi payment URL.

- **Auth required:** Yes (ADMIN)

```json
// Request
{
  "planType": "ELITE"
}

// Response 200
{
  "paymentUrl": "https://checkout.wompi.co/l/abc123",
  "planType": "ELITE",
  "price": 199900,
  "currency": "COP"
}
```

---

### Webhooks

#### POST /api/v1/webhooks/wompi

Wompi payment webhook. Validates HMAC SHA256 signature.

- **Auth required:** No (validated by signature)

```json
// Wompi sends this payload
{
  "event": "transaction.updated",
  "data": {
    "transaction": {
      "id": "txn_123",
      "status": "APPROVED",
      "reference": "sub_550e8400_ELITE_1714000000",
      "amount_in_cents": 19990000
    }
  },
  "signature": {
    "checksum": "abc123..."
  }
}

// Response 200
{ "status": "ok" }
```

---

## Authentication

### JWT Flow

1. User calls `POST /auth/login` with email and password
2. Server returns `accessToken` (8h) + `refreshToken` (7d)
3. Every request includes `Authorization: Bearer {accessToken}`
4. `JwtAuthFilter` intercepts, validates token, and sets `SecurityContext`
5. When access token expires, client calls `POST /auth/refresh` with the refresh token

**JWT Claims:**

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "role": "ADMIN",
  "plan": "PRO",
  "iat": 1714000000,
  "exp": 1714028800
}
```

### OTP Verification Flow (Resend)

1. `POST /auth/register` creates user with `emailVerified = false`
2. Generates 6-digit OTP, stores in `otp_tokens` (expires in 10 min)
3. Sends email via **Resend API** (`POST https://api.resend.com/emails`)
4. `POST /auth/verify-otp` validates code + expiration, sets `emailVerified = true`
5. If pending team invitations exist for this email, they are auto-accepted
6. If OTP expires, user can request resend via `POST /auth/resend-otp`
7. Maximum 3 failed attempts triggers 15-minute lockout
8. Login is only allowed when `emailVerified = true`

---

## Authorization (RBAC)

### Roles

| Role | Description |
|------|-------------|
| `ADMIN` | Business owner. Full access to all resources. |
| `CASHIER` | Can create orders and close sales. |
| `WAITER` | Can create orders and view them. |
| `KITCHEN` | Read-only access to orders. |

### Permissions Matrix

| Resource | ADMIN | CASHIER | WAITER | KITCHEN |
|----------|:-----:|:-------:|:------:|:-------:|
| Business (CRUD) | Yes | - | - | - |
| Products (CRUD) | Yes | - | - | - |
| Categories (CRUD) | Yes | - | - | - |
| Tables (CRUD) | Yes | - | - | - |
| Orders (create) | Yes | Yes | Yes | - |
| Orders (view) | Yes | Yes | Yes | Yes |
| Orders (change status) | Yes | Yes | - | Yes |
| Sales (close) | Yes | Yes | - | - |
| Sales (view) | Yes | Yes | - | - |
| Reports | Yes | - | - | - |
| Analytics | Yes | - | - | - |
| Team (manage) | Yes | - | - | - |
| Plans | Yes | - | - | - |

### Implementation

Spring Security with `@PreAuthorize` annotations on controllers:

```java
@PreAuthorize("hasRole('ADMIN')")           // Admin only
@PreAuthorize("hasAnyRole('ADMIN','CASHIER','WAITER')") // Multiple roles
@PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN')") // Admin or Kitchen
```

---

## Plans & Limits

| Feature | SEMILLA | PRO | ELITE |
|---------|:-------:|:---:|:-----:|
| **Price/month (COP)** | $39,900 | $99,900 | $199,900 |
| **Free trial** | - | 30 days | - |
| **Businesses** | 1 | 1 | Unlimited |
| **Tables (per business)** | 4 | Unlimited | Unlimited |
| **Products (per business)** | 25 | Unlimited | Unlimited |
| **Categories (per business)** | 3 | Unlimited | Unlimited |
| **Employees (per business)** | 3 | Unlimited | Unlimited |
| **Reports** | No | Yes | Yes |
| **Analytics** | No | Yes | Yes |

- New users start with **PRO plan + 30-day free trial**
- `-1` in the database means "unlimited"
- Limits are enforced **per business**, not globally
- Expired plan blocks ALL write operations (402 Payment Required)
- Exceeding a plan limit returns 422 Unprocessable Entity
- `PlanEnforcer` validates every write operation against the user's plan

---

## Multi-tenancy

Mezo POS uses **URL-based multi-tenancy**. All business-specific resources are scoped under:

```
/api/v1/businesses/{businessId}/...
```

### BusinessAccessFilter

A servlet filter (`OncePerRequestFilter`) that runs on every request containing a `{businessId}` path variable:

1. Extracts `businessId` from the URL
2. Gets the authenticated user from SecurityContext
3. Checks if user belongs to the business:
   - **Owner check:** `business.ownerId == user.id`
   - **Team member check:** `team_members` has a record with matching `userId` and `businessId`
4. If neither matches, returns **403 Forbidden**

This ensures no user can access data from a business they do not belong to, regardless of their role.

---

## Payments

### Wompi Integration

[Wompi](https://wompi.com) is Colombia's leading payment gateway, supporting PSE (bank transfer), Nequi, Daviplata, credit/debit cards, and Bancolombia.

**Subscription flow:**

```
1. Admin calls POST /plans/subscribe { planType: "ELITE" }
2. Backend creates a Wompi payment link via API
3. Backend returns { paymentUrl, planType, price, currency }
4. Frontend redirects user to paymentUrl (Wompi checkout)
5. User completes payment (PSE, Nequi, card, etc.)
6. Wompi sends webhook to POST /webhooks/wompi
7. Backend validates HMAC SHA256 signature
8. If transaction APPROVED:
   - Updates user plan and expiration
   - Updates subscription status to ACTIVE
   - Sets period end to +30 days
9. If transaction DECLINED:
   - Logs failure, no plan change
10. Monthly recurring payments handled via Wompi subscriptions
```

**Webhook validation:**
- Wompi signs webhooks with HMAC SHA256 using the events secret
- The `x-event-checksum` header contains the signature
- Backend validates before processing any event

---

## Database

### Development: H2 (In-Memory)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:mezo
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
```

Access the H2 console at `http://localhost:8080/h2-console`.

### Production: PostgreSQL

Migration path from H2 to PostgreSQL requires:

1. Change the datasource configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mezo
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

2. Add PostgreSQL driver to `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

3. Use Flyway or Liquibase for schema migrations in production.

### Entity Relationship Diagram

```
users ----+---- team_members ---- businesses
  |       |                          |
  |       |               +----+----+----+----+
  |       |               |    |    |    |    |
  |       |           products cats tables orders
  |       |                                 |
  |       +--- businesses              order_lines
  |            (owner_id)                   |
  |                                       sales
otp_tokens
subscriptions (user_id)
plans (reference table)
```

### Common Fields (All Tables)

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | Primary key, auto-generated |
| `created_at` | TIMESTAMP | NOT NULL, immutable |
| `updated_at` | TIMESTAMP | Auto-updated on changes |
| `deleted` | BOOLEAN | Soft delete flag, default false |

---

## Error Handling

All errors follow a standard format:

```json
{
  "status": 422,
  "error": "PLAN_LIMIT_EXCEEDED",
  "message": "El plan Semilla permite maximo 25 productos",
  "timestamp": "2026-04-24T10:30:00Z"
}
```

| Status | Error Code | Description |
|--------|-----------|-------------|
| 400 | `BAD_REQUEST` | Validation failed |
| 401 | `UNAUTHORIZED` | Not authenticated |
| 402 | `PLAN_EXPIRED` | Plan has expired, needs renewal |
| 403 | `FORBIDDEN` | No permission for this resource |
| 404 | `NOT_FOUND` | Resource not found |
| 409 | `CONFLICT` | Duplicate resource (e.g., email exists) |
| 422 | `PLAN_LIMIT_EXCEEDED` | Plan limit exceeded |
| 422 | `DOMAIN_ERROR` | Business rule violation |
| 500 | `INTERNAL_ERROR` | Unexpected server error |

---

## Scalability

The modular monolith architecture is designed for incremental extraction to microservices:

```
Phase 1 (current):  Modular monolith with separated packages
Phase 2:            Extract auth as independent service
Phase 3:            Extract order+sale as independent service
Phase 4:            Extract report+analytics as independent service
```

Internal Spring Application Events (`OrderCreatedEvent`, `SaleClosedEvent`, etc.) prepare the codebase for eventual message-broker migration.
