# Mezo POS — Backend Specification v1.0

> Sistema SaaS de gestión de restaurantes/cafeterías
> "Vende más. Piensa menos."

---

## 1. Decisiones Técnicas

| Decisión | Elección |
|----------|----------|
| Arquitectura | Hexagonal (Ports & Adapters) |
| Estilo | Monolito modular |
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.x |
| Base de datos | H2 (desarrollo) → PostgreSQL (producción) |
| API | REST |
| Autenticación | JWT (access + refresh token) |
| ORM | JPA / Hibernate |
| Build | Maven |
| Testing | JUnit 5 + Mockito |

---

## 2. Arquitectura Hexagonal

### 2.1 Capas

```
┌─────────────────────────────────────────────────┐
│                 INFRASTRUCTURE                   │
│  (Controllers REST, JPA Repos, Email, JWT)       │
│                                                   │
│  ┌─────────────────────────────────────────────┐ │
│  │              APPLICATION                     │ │
│  │  (Use Cases / Services)                      │ │
│  │                                               │ │
│  │  ┌─────────────────────────────────────────┐ │ │
│  │  │             DOMAIN                       │ │ │
│  │  │  (Entities, Value Objects, Ports)        │ │ │
│  │  │  SIN dependencias externas              │ │ │
│  │  └─────────────────────────────────────────┘ │ │
│  └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### 2.2 Regla de dependencia

- **Domain** → no depende de nada
- **Application** → depende solo de Domain
- **Infrastructure** → depende de Application y Domain

### 2.3 Puertos y Adaptadores

**Puertos de entrada (driving ports):** interfaces que definen los casos de uso.
```java
// Puerto de entrada
public interface CreateOrderUseCase {
    OrderResponse execute(CreateOrderCommand command);
}
```

**Puertos de salida (driven ports):** interfaces que el dominio necesita del exterior.
```java
// Puerto de salida
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
}
```

**Adaptadores de entrada:** Controllers REST que invocan los casos de uso.
```java
// Adaptador de entrada
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    // ...
}
```

**Adaptadores de salida:** Implementaciones JPA de los repositorios.
```java
// Adaptador de salida
@Repository
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository springRepo;
    // ...
}
```

---

## 3. Estructura del Proyecto

```
com.mezo.pos/
├── shared/                          # Código compartido entre módulos
│   ├── domain/
│   │   ├── valueobject/
│   │   │   ├── Money.java
│   │   │   ├── Email.java
│   │   │   ├── PhoneNumber.java
│   │   │   └── Currency.java
│   │   ├── entity/
│   │   │   └── BaseEntity.java      # id, createdAt, updatedAt, deleted
│   │   └── exception/
│   │       ├── DomainException.java
│   │       ├── NotFoundException.java
│   │       ├── PlanLimitExceededException.java
│   │       ├── PlanExpiredException.java
│   │       └── UnauthorizedException.java
│   ├── application/
│   │   └── EventPublisher.java
│   └── infrastructure/
│       ├── security/
│       │   ├── JwtProvider.java
│       │   ├── JwtAuthFilter.java
│       │   └── SecurityConfig.java
│       ├── config/
│       │   ├── AppConfig.java
│       │   └── CorsConfig.java
│       └── exception/
│           ├── GlobalExceptionHandler.java
│           └── ErrorResponse.java
│
├── auth/                            # Módulo de autenticación
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   └── OtpToken.java
│   │   ├── port/
│   │   │   ├── UserRepository.java
│   │   │   ├── OtpRepository.java
│   │   │   └── EmailService.java
│   │   └── enums/
│   │       └── Role.java            # ADMIN, CASHIER, WAITER, KITCHEN
│   ├── application/
│   │   ├── RegisterUserUseCase.java
│   │   ├── LoginUseCase.java
│   │   ├── VerifyOtpUseCase.java
│   │   └── RefreshTokenUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   ├── JpaUserRepository.java
│       │   ├── JpaOtpRepository.java
│       │   ├── ResendEmailService.java
│       │   └── SpringDataUserRepository.java   # interface extends JpaRepository
│       └── web/
│           ├── AuthController.java
│           ├── dto/
│           │   ├── RegisterRequest.java
│           │   ├── LoginRequest.java
│           │   ├── VerifyOtpRequest.java
│           │   ├── AuthResponse.java
│           │   └── RefreshTokenRequest.java
│           └── mapper/
│               └── AuthMapper.java
│
├── business/                        # Módulo de negocio
│   ├── domain/
│   │   ├── entity/
│   │   │   └── Business.java
│   │   ├── port/
│   │   │   └── BusinessRepository.java
│   │   └── enums/
│   │       └── BusinessType.java    # RESTAURANT, CAFE, BAR, BAKERY, FOOD_TRUCK
│   ├── application/
│   │   ├── CreateBusinessUseCase.java
│   │   ├── UpdateBusinessUseCase.java
│   │   └── GetBusinessUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   ├── JpaBusinessRepository.java
│       │   └── SpringDataBusinessRepository.java
│       └── web/
│           ├── BusinessController.java
│           ├── dto/
│           │   ├── CreateBusinessRequest.java
│           │   ├── UpdateBusinessRequest.java
│           │   └── BusinessResponse.java
│           └── mapper/
│               └── BusinessMapper.java
│
├── product/                         # Módulo de productos
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── Product.java
│   │   │   └── Category.java
│   │   ├── port/
│   │   │   ├── ProductRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   └── ImageStorage.java    # Puerto para subir imágenes
│   │   └── enums/
│   │       └── ImageType.java       # EMOJI, IMAGE
│   ├── application/
│   │   ├── CreateProductUseCase.java
│   │   ├── UpdateProductUseCase.java
│   │   ├── DeleteProductUseCase.java
│   │   ├── ListProductsUseCase.java
│   │   ├── CreateCategoryUseCase.java
│   │   ├── UpdateCategoryUseCase.java
│   │   └── DeleteCategoryUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   ├── JpaProductRepository.java
│       │   ├── JpaCategoryRepository.java
│       │   ├── SpringDataProductRepository.java
│       │   ├── SpringDataCategoryRepository.java
│       │   └── CloudinaryImageStorage.java
│       └── web/
│           ├── ProductController.java
│           ├── CategoryController.java
│           ├── dto/
│           │   ├── CreateProductRequest.java
│           │   ├── UpdateProductRequest.java
│           │   ├── ProductResponse.java
│           │   ├── CreateCategoryRequest.java
│           │   ├── UpdateCategoryRequest.java
│           │   └── CategoryResponse.java
│           └── mapper/
│               ├── ProductMapper.java
│               └── CategoryMapper.java
│
├── table/                           # Módulo de mesas
│   ├── domain/
│   │   ├── entity/
│   │   │   └── Table.java
│   │   ├── port/
│   │   │   └── TableRepository.java
│   ├── application/
│   │   ├── CreateTableUseCase.java
│   │   ├── DeleteTableUseCase.java
│   │   └── ListTablesUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   ├── JpaTableRepository.java
│       │   └── SpringDataTableRepository.java
│       └── web/
│           ├── TableController.java
│           ├── dto/
│           │   ├── CreateTableRequest.java
│           │   └── TableResponse.java
│           └── mapper/
│               └── TableMapper.java
│
├── order/                           # Módulo de órdenes
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── Order.java           # Aggregate Root
│   │   │   └── OrderLine.java
│   │   ├── port/
│   │   │   └── OrderRepository.java
│   │   └── enums/
│   │       ├── OrderStatus.java     # OPEN, PREPARING, READY, DELIVERED, CANCELLED
│   │       └── PaymentMethod.java   # CASH, BOLD, NEQUI, DAVIPLATA, TRANSFER
│   ├── application/
│   │   ├── CreateOrderUseCase.java
│   │   ├── AddOrderLinesUseCase.java
│   │   ├── RemoveOrderLineUseCase.java
│   │   ├── UpdateOrderStatusUseCase.java
│   │   ├── GetOrderUseCase.java
│   │   └── ListOrdersUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   ├── JpaOrderRepository.java
│       │   ├── SpringDataOrderRepository.java
│       │   └── SpringDataOrderLineRepository.java
│       └── web/
│           ├── OrderController.java
│           ├── dto/
│           │   ├── CreateOrderRequest.java
│           │   ├── OrderLineRequest.java
│           │   ├── UpdateOrderStatusRequest.java
│           │   ├── OrderResponse.java
│           │   └── OrderLineResponse.java
│           └── mapper/
│               └── OrderMapper.java
│
├── sale/                            # Módulo de ventas
│   ├── domain/
│   │   ├── entity/
│   │   │   └── Sale.java
│   │   └── port/
│   │       └── SaleRepository.java
│   ├── application/
│   │   ├── CloseSaleUseCase.java
│   │   ├── GetSaleUseCase.java
│   │   └── ListSalesUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   ├── JpaSaleRepository.java
│       │   └── SpringDataSaleRepository.java
│       └── web/
│           ├── SaleController.java
│           ├── dto/
│           │   ├── CloseSaleRequest.java
│           │   └── SaleResponse.java
│           └── mapper/
│               └── SaleMapper.java
│
├── report/                          # Módulo de reportes
│   ├── domain/
│   │   ├── model/
│   │   │   ├── SalesReport.java
│   │   │   ├── TopProductReport.java
│   │   │   └── PaymentMethodReport.java
│   │   └── port/
│   │       └── ReportRepository.java
│   ├── application/
│   │   ├── GetSalesTotalUseCase.java
│   │   ├── GetSalesCountUseCase.java
│   │   ├── GetPeakHourUseCase.java
│   │   ├── GetTopProductsUseCase.java
│   │   └── GetPaymentMethodStatsUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   └── JpaReportRepository.java
│       └── web/
│           ├── ReportController.java
│           └── dto/
│               ├── SalesTotalResponse.java
│               ├── SalesCountResponse.java
│               ├── PeakHourResponse.java
│               ├── TopProductResponse.java
│               └── PaymentMethodStatsResponse.java
│
├── analytics/                       # Módulo de analítica
│   ├── domain/
│   │   ├── model/
│   │   │   └── TimeSeriesData.java
│   │   └── port/
│   │       └── AnalyticsRepository.java
│   ├── application/
│   │   └── GetAnalyticsUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   └── JpaAnalyticsRepository.java
│       └── web/
│           ├── AnalyticsController.java
│           └── dto/
│               ├── AnalyticsRequest.java
│               └── AnalyticsResponse.java
│
├── team/                            # Módulo de equipo
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── TeamMember.java
│   │   │   └── Invitation.java
│   │   ├── port/
│   │   │   ├── TeamRepository.java
│   │   │   └── InvitationRepository.java
│   │   └── enums/
│   │       └── InvitationStatus.java  # PENDING, ACCEPTED, EXPIRED
│   ├── application/
│   │   ├── InviteMemberUseCase.java
│   │   ├── RemoveMemberUseCase.java
│   │   ├── UpdateMemberRoleUseCase.java
│   │   └── ListTeamUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   ├── JpaTeamRepository.java
│       │   └── SpringDataTeamRepository.java
│       └── web/
│           ├── TeamController.java
│           ├── dto/
│           │   ├── InviteMemberRequest.java
│           │   ├── UpdateRoleRequest.java
│           │   └── TeamMemberResponse.java
│           └── mapper/
│               └── TeamMapper.java
│
└── plan/                            # Módulo de planes y suscripciones
    ├── domain/
    │   ├── entity/
    │   │   ├── Plan.java
    │   │   └── Subscription.java
    │   ├── port/
    │   │   ├── PlanRepository.java
    │   │   ├── SubscriptionRepository.java
    │   │   └── PaymentGateway.java       # Puerto de salida para Wompi
    │   ├── enums/
    │   │   ├── PlanType.java             # SEMILLA, PRO, ELITE
    │   │   └── SubscriptionStatus.java   # TRIAL, ACTIVE, PAST_DUE, CANCELLED
    │   └── service/
    │       └── PlanEnforcer.java
    ├── application/
    │   ├── GetCurrentPlanUseCase.java
    │   ├── SubscribePlanUseCase.java
    │   └── HandlePaymentWebhookUseCase.java
    └── infrastructure/
        ├── adapter/
        │   ├── JpaPlanRepository.java
        │   ├── JpaSubscriptionRepository.java
        │   ├── SpringDataPlanRepository.java
        │   ├── SpringDataSubscriptionRepository.java
        │   └── WompiPaymentGateway.java  # Adaptador Wompi
        └── web/
            ├── PlanController.java
            ├── WompiWebhookController.java
            └── dto/
                ├── PlanResponse.java
                ├── SubscribeRequest.java
                ├── SubscribeResponse.java
                └── WompiWebhookPayload.java
```

---

## 4. Modelo de Dominio

### 4.1 Value Objects

```java
// --- Money ---
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Amount must be >= 0");
        }
        if (currency == null) {
            throw new DomainException("Currency is required");
        }
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }
}

// --- Email ---
public record Email(String value) {
    private static final Pattern EMAIL_REGEX =
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public Email {
        if (value == null || !EMAIL_REGEX.matcher(value).matches()) {
            throw new DomainException("Invalid email: " + value);
        }
        value = value.toLowerCase().trim();
    }
}

// --- PhoneNumber ---
public record PhoneNumber(String value) {
    public PhoneNumber {
        if (value == null || value.length() < 7 || value.length() > 15) {
            throw new DomainException("Invalid phone number");
        }
    }
}

// --- Currency (enum) ---
public enum Currency {
    COP, USD, MXN
}
```

### 4.2 BaseEntity

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deleted = true;
    }
}
```

### 4.3 Entidades principales

#### User
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", unique = true, nullable = false))
    private Email email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean emailVerified = false;

    // El plan pertenece al USUARIO (admin), no al negocio.
    // Un admin con plan ELITE puede crear múltiples negocios.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan = PlanType.PRO;     // nuevo usuario = PRO con 30 días gratis

    @Column(name = "plan_started_at")
    private LocalDateTime planStartedAt;    // para calcular trial de 30 días (PRO)

    @Column(name = "plan_expires_at")
    private LocalDateTime planExpiresAt;    // null = sin expiración (pagado)
}
```

> **Nota:** Un usuario ADMIN es dueño de 0..N negocios. Los usuarios con rol
> CASHIER, WAITER o KITCHEN no tienen plan propio — heredan los permisos del
> plan del admin que los invitó (a través de `TeamMember.businessId → Business.ownerId → User.plan`).

#### OtpToken
```java
@Entity
@Table(name = "otp_tokens")
public class OtpToken extends BaseEntity {
    @Column(nullable = false)
    private String code;        // 6 dígitos

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiresAt;  // creación + 10 min

    @Column(nullable = false)
    private boolean used = false;
}
```

#### Business
```java
@Entity
@Table(name = "businesses")
public class Business extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessType type;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone"))
    private PhoneNumber phone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(name = "open_at")
    private LocalTime openAt;

    @Column(name = "close_at")
    private LocalTime closeAt;

    @Column(name = "table_count", nullable = false)
    private int tableCount = 0;

    @Column(nullable = false)
    private boolean open = false;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    // El plan NO vive aquí. Se resuelve via: ownerId → User.plan
    // Esto permite que un admin ELITE tenga múltiples negocios
    // bajo un solo plan.
}
```

#### Product
```java
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false))
    })
    private Money price;

    private String description;

    private String ingredients;   // texto libre, opcional (ej: "café, leche, azúcar")

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType;   // EMOJI → image es el emoji string, IMAGE → image es URL de Cloudinary

    private String image;          // emoji ("☕") o URL Cloudinary ("https://res.cloudinary.com/mezo/...")

    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;
}
```

#### ImageStorage (puerto + adaptador Cloudinary)
```java
// Puerto (domain)
public interface ImageStorage {
    String upload(byte[] file, String fileName, UUID businessId);
    void delete(String imageUrl);
}

// Adaptador (infrastructure)
@Component
public class CloudinaryImageStorage implements ImageStorage {
    private final Cloudinary cloudinary;  // SDK de Cloudinary

    @Override
    public String upload(byte[] file, String fileName, UUID businessId) {
        // Sube a folder: mezo/{businessId}/products/{fileName}
        // Retorna URL pública: https://res.cloudinary.com/mezo/image/upload/...
        // Aplica transformación automática: resize 400x400, format auto, quality auto
    }

    @Override
    public void delete(String imageUrl) {
        // Extrae public_id de la URL y llama cloudinary.uploader().destroy()
    }
}
```

**Endpoint de upload (multipart):**
```
POST /api/v1/businesses/{businessId}/products/upload-image
Content-Type: multipart/form-data
Body: file (max 3MB, jpg/png/webp)

Response 200:
{ "imageUrl": "https://res.cloudinary.com/mezo/image/upload/v1/.../product.jpg" }
```

> El frontend sube la imagen primero, obtiene la URL, y la envía en el
> `POST /products` o `PUT /products/{id}` como campo `image`.
> Si `imageType = EMOJI`, no se sube imagen — el campo `image` es el emoji string directo.

#### Category
```java
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String icon;     // emoji

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;
}
```

#### RestaurantTable
```java
@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable extends BaseEntity {
    @Column(nullable = false)
    private int number;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;
}
```

#### Order (Aggregate Root)
```java
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderLine> lines = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "tip_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "tip_currency"))
    })
    private Money tip;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_currency", nullable = false))
    })
    private Money total;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.OPEN;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "table_id")
    private UUID tableId;           // null = orden directa desde POS (sin mesa)

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    // --- Reglas de negocio en el dominio ---
    public void addLine(OrderLine line) {
        assertOpen();
        this.lines.add(line);
        recalculateTotal();
    }

    public void removeLine(UUID lineId) {
        assertOpen();
        boolean removed = this.lines.removeIf(l -> l.getId().equals(lineId));
        if (!removed) {
            throw new NotFoundException("OrderLine not found: " + lineId);
        }
        recalculateTotal();
    }

    public void cancel() {
        assertOpen();
        this.status = OrderStatus.CANCELLED;
    }

    private void assertOpen() {
        if (this.status != OrderStatus.OPEN) {
            throw new DomainException("Order must be OPEN to modify. Current status: " + this.status);
        }
    }

    private void recalculateTotal() {
        BigDecimal sum = lines.stream()
            .map(l -> l.getSubtotal().amount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tipAmount = tip != null ? tip.amount() : BigDecimal.ZERO;
        this.total = new Money(sum.add(tipAmount), Currency.COP);
    }
}
```

#### OrderLine
```java
@Entity
@Table(name = "order_lines")
public class OrderLine extends BaseEntity {
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "unit_currency", nullable = false))
    })
    private Money unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency", nullable = false))
    })
    private Money subtotal;

    public OrderLine(UUID productId, String productName, Money unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(quantity);
    }
}
```

#### Sale
```java
@Entity
@Table(name = "sales")
public class Sale extends BaseEntity {
    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_currency", nullable = false))
    })
    private Money total;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "tip_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "tip_currency"))
    })
    private Money tip;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "table_id")
    private UUID tableId;           // null si fue orden directa desde POS

    @Column(name = "closed_by", nullable = false)
    private UUID closedBy;
}
```

#### TeamMember
```java
@Entity
@Table(name = "team_members")
public class TeamMember extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;
}
```

#### Plan
```java
@Entity
@Table(name = "plans")
public class Plan extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType type;

    @Column(name = "max_tables", nullable = false)
    private int maxTables;

    @Column(name = "max_employees", nullable = false)
    private int maxEmployees;

    @Column(name = "max_categories", nullable = false)
    private int maxCategories;

    @Column(name = "max_products", nullable = false)
    private int maxProducts;

    @Column(name = "max_businesses", nullable = false)
    private int maxBusinesses;

    @Column(name = "reports_enabled", nullable = false)
    private boolean reportsEnabled;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false))
    })
    private Money price;

    @Column(name = "trial_days", nullable = false)
    private int trialDays = 0;
}
```

#### Subscription
```java
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    @Column(name = "wompi_subscription_id")
    private String wompiSubscriptionId;      // ID de suscripción en Wompi

    @Column(name = "wompi_transaction_id")
    private String wompiTransactionId;       // último pago

    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;  // se sincroniza con user.planExpiresAt

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
```

```java
public enum SubscriptionStatus {
    TRIAL,       // período de prueba (30 días gratis)
    ACTIVE,      // pago confirmado, suscripción activa
    PAST_DUE,    // pago fallido, gracia de N días antes de bloquear
    CANCELLED    // usuario canceló, activo hasta fin del período pagado
}
```

#### PaymentGateway (puerto de salida)
```java
// Puerto — en domain
public interface PaymentGateway {
    PaymentLink createPaymentLink(UUID userId, PlanType planType, Money price);
    boolean validateWebhookSignature(String payload, String signature);
}

// Adaptador — en infrastructure
@Component
public class WompiPaymentGateway implements PaymentGateway {
    private final String publicKey;      // WOMPI_PUBLIC_KEY
    private final String privateKey;     // WOMPI_PRIVATE_KEY
    private final String eventsSecret;   // WOMPI_EVENTS_SECRET (para validar firma webhook)
    private final String redirectUrl;    // URL del frontend post-pago

    @Override
    public PaymentLink createPaymentLink(UUID userId, PlanType planType, Money price) {
        // POST https://production.wompi.co/v1/payment_links
        // { name, description, single_use: false, collect_shipping: false,
        //   currency: "COP", amount_in_cents: price * 100,
        //   redirect_url, reference: "sub_{userId}_{planType}" }
        // Retorna { id, url } → el frontend redirige a url
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        // Validar HMAC SHA256 con eventsSecret
        // Wompi envía header x-event-checksum
    }
}
```

---

## 5. Modelo de Datos (Tablas)

### 5.1 Diagrama de relaciones

```
users (plan vive aquí) ──┬── team_members ──── businesses
  │                      │                        │
  │                      │               ┌────────┼────────┬──────────┐
  │                      │               │        │        │          │
  │                      │            products  categories  tables   orders
  │                      │                                           │
  │                      │                                      order_lines
  │                      │                                           │
  │                      └── businesses (owner_id → users.id)      sales
  │                           un admin ELITE puede tener N negocios
  │
otp_tokens
subscriptions (user_id → users.id)

plans (standalone config/reference table)
```

### 5.2 Tabla de relaciones

| Relación | Tipo | FK |
|----------|------|-----|
| Business → Products | OneToMany | `products.business_id` |
| Business → Categories | OneToMany | `categories.business_id` |
| Business → Tables | OneToMany | `restaurant_tables.business_id` |
| Business → Orders | OneToMany | `orders.business_id` |
| Business → Sales | OneToMany | `sales.business_id` |
| Business → TeamMembers | OneToMany | `team_members.business_id` |
| Order → OrderLines | OneToMany (cascade) | `order_lines.order_id` |
| Product.ingredients | String column | campo de texto libre, sin tabla separada |
| Order → Sale | OneToOne | `sales.order_id` |
| User → TeamMembers | OneToMany | `team_members.user_id` |
| User → Businesses | OneToMany | `businesses.owner_id` (admin ELITE = N negocios) |
| Business → Invitations | OneToMany | `invitations.business_id` |
| User → Subscription | OneToOne | `subscriptions.user_id` |
| User → OtpTokens | implícita por email | `otp_tokens.email` |

### 5.3 Campos comunes en todas las tablas

| Campo | Tipo | Nota |
|-------|------|------|
| `id` | UUID | PK, auto-generado |
| `created_at` | TIMESTAMP | NOT NULL, inmutable |
| `updated_at` | TIMESTAMP | se actualiza automáticamente |
| `deleted` | BOOLEAN | soft delete, default false |

---

## 6. API REST

### 6.1 Convenciones

- Base URL: `/api/v1`
- Formato: JSON
- Naming: kebab-case en URLs, camelCase en JSON
- Paginación: `?page=0&size=20`
- Filtros: query params
- Timestamps: ISO 8601

### 6.2 Status codes

| Código | Uso |
|--------|-----|
| 200 | OK — GET, PUT exitoso |
| 201 | Created — POST exitoso |
| 204 | No Content — DELETE exitoso |
| 400 | Bad Request — validación fallida |
| 401 | Unauthorized — no autenticado |
| 403 | Forbidden — sin permisos |
| 404 | Not Found |
| 409 | Conflict — duplicado (email existente, etc.) |
| 402 | Payment Required — plan expirado |
| 422 | Unprocessable Entity — regla de negocio violada (límite de plan) |
| 500 | Internal Server Error |

### 6.3 Error Response estándar

```json
{
  "status": 422,
  "error": "PLAN_LIMIT_EXCEEDED",
  "message": "El plan Semilla permite máximo 25 productos",
  "timestamp": "2026-04-24T10:30:00Z"
}
```

```json
{
  "status": 402,
  "error": "PLAN_EXPIRED",
  "message": "Tu plan PRO ha expirado. Renueva tu suscripción para continuar.",
  "timestamp": "2026-04-24T10:30:00Z"
}
```

### 6.4 Endpoints

#### Auth
```
POST   /api/v1/auth/register          → Registrar usuario
POST   /api/v1/auth/verify-otp        → Verificar OTP
POST   /api/v1/auth/resend-otp        → Reenviar OTP (via Resend)
POST   /api/v1/auth/login             → Login (retorna JWT 8h + refresh token)
POST   /api/v1/auth/refresh           → Refresh token
POST   /api/v1/auth/forgot-password   → Solicitar reset (OTP via Resend)
POST   /api/v1/auth/reset-password    → Resetear contraseña
```

**POST /api/v1/auth/register**
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

**POST /api/v1/auth/verify-otp**
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

**POST /api/v1/auth/login**
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
    "plan": "SEMILLA",
    "emailVerified": true,
    "businesses": []
  }
}
```

#### Business
```
POST   /api/v1/businesses              → Crear negocio (onboarding). ELITE puede crear varios.
GET    /api/v1/businesses               → Listar mis negocios (admin ve todos los suyos)
GET    /api/v1/businesses/{id}          → Obtener negocio por ID
PUT    /api/v1/businesses/{id}          → Actualizar negocio
PATCH  /api/v1/businesses/{id}/status   → Abrir/cerrar negocio
DELETE /api/v1/businesses/{id}          → Eliminar negocio (soft delete)
```

**POST /api/v1/businesses**
```json
// Request
{
  "name": "Café Mezo",
  "type": "CAFE",
  "phone": "+573001234567",
  "address": "Calle 85 #15-20",
  "city": "Bogotá",
  "country": "Colombia",
  "openAt": "07:00",
  "closeAt": "22:00",
  "tableCount": 4
}

// Response 201
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "name": "Café Mezo",
  "type": "CAFE",
  "phone": "+573001234567",
  "address": "Calle 85 #15-20",
  "city": "Bogotá",
  "country": "Colombia",
  "openAt": "07:00",
  "closeAt": "22:00",
  "tableCount": 4,
  "open": false,
  "createdAt": "2026-04-23T10:30:00Z"
}
```

> **Nota:** El response ya no incluye `plan` porque el plan pertenece al usuario, no al negocio.
> El plan se obtiene via `GET /api/v1/plans/current`.

#### Products
```
POST   /api/v1/businesses/{businessId}/products                → Crear producto
GET    /api/v1/businesses/{businessId}/products                → Listar productos (?categoryId=xxx)
GET    /api/v1/businesses/{businessId}/products/{id}           → Obtener producto
PUT    /api/v1/businesses/{businessId}/products/{id}           → Actualizar producto
DELETE /api/v1/businesses/{businessId}/products/{id}           → Eliminar producto (soft delete)
PATCH  /api/v1/businesses/{businessId}/products/{id}/toggle    → Toggle disponibilidad
POST   /api/v1/businesses/{businessId}/products/upload-image  → Subir imagen (multipart, max 3MB)
```

**POST /api/v1/products**
```json
// Request
{
  "name": "Cappuccino",
  "price": 8500,
  "currency": "COP",
  "description": "Espresso con leche espumada",
  "ingredients": "café, leche",
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
  "ingredients": "café, leche",
  "imageType": "EMOJI",
  "image": "☕",
  "available": true,
  "categoryId": "770e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-04-23T10:30:00Z"
}
```

#### Categories
```
POST   /api/v1/businesses/{businessId}/categories              → Crear categoría
GET    /api/v1/businesses/{businessId}/categories              → Listar categorías
PUT    /api/v1/businesses/{businessId}/categories/{id}         → Actualizar categoría
DELETE /api/v1/businesses/{businessId}/categories/{id}         → Eliminar categoría
```

**POST /api/v1/categories**
```json
// Request
{ "name": "Bebidas calientes", "icon": "☕" }

// Response 201
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "name": "Bebidas calientes",
  "icon": "☕",
  "sortOrder": 0,
  "createdAt": "2026-04-23T10:30:00Z"
}
```

#### Tables
```
POST   /api/v1/businesses/{businessId}/tables                  → Crear mesa
GET    /api/v1/businesses/{businessId}/tables                  → Listar mesas
DELETE /api/v1/businesses/{businessId}/tables/{id}             → Eliminar mesa
```

#### Orders
```
POST   /api/v1/businesses/{businessId}/orders                        → Crear orden
GET    /api/v1/businesses/{businessId}/orders                        → Listar órdenes (?status=OPEN&tableId=xxx)
GET    /api/v1/businesses/{businessId}/orders/{id}                   → Obtener orden
POST   /api/v1/businesses/{businessId}/orders/{id}/lines             → Agregar líneas a orden existente
DELETE /api/v1/businesses/{businessId}/orders/{id}/lines/{lineId}    → Eliminar línea de orden
PATCH  /api/v1/businesses/{businessId}/orders/{id}/status            → Cambiar estado
```

**POST /api/v1/businesses/{businessId}/orders**
```json
// Request — orden desde mesa
{
  "tableId": "990e8400-e29b-41d4-a716-446655440000",
  "paymentMethod": "NEQUI",
  "tip": 2000,
  "lines": [
    { "productId": "880e8400-...", "quantity": 2 },
    { "productId": "880e8401-...", "quantity": 1 }
  ]
}

// Request — orden directa desde POS (sin mesa)
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
      "productId": "880e8400-...",
      "productName": "Cappuccino",
      "unitPrice": 8500,
      "quantity": 2,
      "subtotal": 17000
    },
    {
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

**POST /api/v1/businesses/{businessId}/orders/{id}/lines** — Agregar productos a orden existente
```json
// Request — una o varias líneas nuevas
{
  "lines": [
    { "productId": "880e8402-...", "quantity": 1 },
    { "productId": "880e8400-...", "quantity": 1 }
  ]
}

// Response 200 — retorna la orden completa actualizada
{
  "id": "aa0e8400-...",
  "tableId": "990e8400-...",
  "status": "OPEN",
  "paymentMethod": "NEQUI",
  "tip": 2000,
  "total": 42500,
  "currency": "COP",
  "lines": [
    { "id": "line-01", "productId": "880e8400-...", "productName": "Cappuccino", "unitPrice": 8500, "quantity": 2, "subtotal": 17000 },
    { "id": "line-02", "productId": "880e8401-...", "productName": "Brownie", "unitPrice": 8000, "quantity": 1, "subtotal": 8000 },
    { "id": "line-03", "productId": "880e8402-...", "productName": "Limonada", "unitPrice": 7000, "quantity": 1, "subtotal": 7000 },
    { "id": "line-04", "productId": "880e8400-...", "productName": "Cappuccino", "unitPrice": 8500, "quantity": 1, "subtotal": 8500 }
  ],
  "createdBy": "550e8400-...",
  "createdAt": "2026-04-24T14:30:00Z"
}
```

> **Nota:** Agregar el mismo producto dos veces crea líneas separadas (no suma quantity).
> Esto permite distinguir pedidos en diferentes momentos. Solo se pueden agregar líneas
> a órdenes con status `OPEN`. Si la orden ya fue entregada o cancelada → 422.

**DELETE /api/v1/businesses/{businessId}/orders/{id}/lines/{lineId}** — Eliminar línea
```json
// Response 200 — retorna la orden actualizada (sin la línea eliminada)
// Solo permitido en órdenes con status OPEN → 422 si no
```

#### Sales
```
POST   /api/v1/businesses/{businessId}/sales                   → Cerrar venta (desde orden)
GET    /api/v1/businesses/{businessId}/sales                   → Listar ventas (?from=2026-04-01&to=2026-04-23)
GET    /api/v1/businesses/{businessId}/sales/{id}              → Obtener venta
```

**POST /api/v1/businesses/{businessId}/sales** — Cerrar orden existente
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

**POST /api/v1/businesses/{businessId}/sales** — Venta directa POS (sin orden previa)
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

// Response 201 — crea orden + venta en un solo paso
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

#### Reports (requiere plan PRO o ELITE)

Todos los reportes usan un único query param `?time=` que determina el rango de tiempo.
El backend calcula `from` y `to` automáticamente según el valor.

| `time` | Periodo calculado |
|--------|-------------------|
| `DAY` | hoy (00:00 → ahora) |
| `WEEK` | últimos 7 días |
| `MONTH` | últimas 4 semanas |
| `QUARTER` | últimos 3 meses |
| `YEAR` | últimos 12 meses |

```
GET    /api/v1/businesses/{businessId}/reports/sales           → Total vendido (?time=DAY|WEEK|MONTH|QUARTER|YEAR)
GET    /api/v1/businesses/{businessId}/reports/count           → Cantidad de ventas (?time=)
GET    /api/v1/businesses/{businessId}/reports/peak-hour       → Mejor hora (?time=)
GET    /api/v1/businesses/{businessId}/reports/top-products    → Producto más vendido (?time=&limit=10)
GET    /api/v1/businesses/{businessId}/reports/payment-methods → Método de pago más usado (?time=)
```

**GET /api/v1/businesses/{businessId}/reports/sales?time=MONTH**
```json
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

**GET /api/v1/businesses/{businessId}/reports/count?time=WEEK**
```json
{
  "time": "WEEK",
  "from": "2026-04-17",
  "to": "2026-04-24",
  "salesCount": 89
}
```

**GET /api/v1/businesses/{businessId}/reports/peak-hour?time=MONTH**
```json
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

**GET /api/v1/businesses/{businessId}/reports/top-products?time=WEEK&limit=5**
```json
{
  "time": "WEEK",
  "from": "2026-04-17",
  "to": "2026-04-24",
  "products": [
    { "productId": "...", "name": "Cappuccino", "totalSold": 156, "revenue": 1326000 },
    { "productId": "...", "name": "Brownie", "totalSold": 98, "revenue": 784000 }
  ]
}
```

**GET /api/v1/businesses/{businessId}/reports/payment-methods?time=QUARTER**
```json
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

#### Analytics / Gráficas (requiere plan PRO o ELITE)

Endpoint dedicado para alimentar las gráficas del frontend. Usa el mismo `?time=`
pero retorna datos agrupados en serie de tiempo.

```
GET    /api/v1/businesses/{businessId}/analytics    → Serie de tiempo (?time=DAY|WEEK|MONTH|QUARTER|YEAR)
```

| `time` | Agrupación | Periodo |
|--------|-----------|---------|
| `DAY` | por hora (24 puntos) | hoy |
| `WEEK` | por día (7 puntos) | últimos 7 días |
| `MONTH` | por semana (4 puntos) | últimas 4 semanas |
| `QUARTER` | por mes (3 puntos) | últimos 3 meses |
| `YEAR` | por mes (12 puntos) | últimos 12 meses |

**GET /api/v1/businesses/{businessId}/analytics?time=DAY**
```json
{
  "time": "DAY",
  "currency": "COP",
  "data": [
    { "label": "7:00", "total": 85000, "count": 12 },
    { "label": "8:00", "total": 120000, "count": 18 },
    { "label": "9:00", "total": 95000, "count": 14 },
    { "label": "10:00", "total": 60000, "count": 8 },
    { "label": "11:00", "total": 45000, "count": 6 },
    { "label": "12:00", "total": 180000, "count": 25 },
    { "label": "13:00", "total": 150000, "count": 20 }
  ]
}
```

**GET /api/v1/businesses/{businessId}/analytics?time=WEEK**
```json
{
  "time": "WEEK",
  "currency": "COP",
  "data": [
    { "label": "Lun", "date": "2026-04-20", "total": 350000, "count": 48 },
    { "label": "Mar", "date": "2026-04-21", "total": 420000, "count": 56 },
    { "label": "Mié", "date": "2026-04-22", "total": 380000, "count": 51 },
    { "label": "Jue", "date": "2026-04-23", "total": 290000, "count": 39 },
    { "label": "Vie", "date": "2026-04-24", "total": 180000, "count": 22 }
  ]
}
```

**GET /api/v1/businesses/{businessId}/analytics?time=YEAR**
```json
{
  "time": "YEAR",
  "currency": "COP",
  "data": [
    { "label": "May 2025", "total": 3200000, "count": 410 },
    { "label": "Jun 2025", "total": 2900000, "count": 380 },
    { "label": "Jul 2025", "total": 3100000, "count": 405 },
    { "label": "...", "total": 0, "count": 0 },
    { "label": "Abr 2026", "total": 2800000, "count": 350 }
  ]
}
```

#### Team
```
POST   /api/v1/businesses/{businessId}/team/invite             → Invitar miembro
GET    /api/v1/businesses/{businessId}/team                     → Listar equipo
PUT    /api/v1/businesses/{businessId}/team/{userId}/role       → Cambiar rol
DELETE /api/v1/businesses/{businessId}/team/{userId}            → Remover miembro
```

**POST /api/v1/team/invite**
```json
// Request
{
  "email": "member@example.com",
  "role": "CASHIER"
}

// Response 201
{
  "userId": "cc0e8400-...",
  "email": "member@example.com",
  "role": "CASHIER",
  "businessId": "660e8400-...",
  "invitedBy": "550e8400-...",
  "createdAt": "2026-04-23T10:30:00Z"
}
```

#### Plans & Subscriptions (Wompi)
```
GET    /api/v1/plans                          → Listar planes disponibles
GET    /api/v1/plans/current                  → Plan actual del usuario + estado de suscripción
POST   /api/v1/plans/subscribe                → Iniciar suscripción (crea link de pago Wompi)
POST   /api/v1/webhooks/wompi                 → Webhook de Wompi (público, validado por firma)
```

**GET /api/v1/plans/current**
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

**POST /api/v1/plans/subscribe** — Iniciar pago / cambiar plan
```json
// Request
{
  "planType": "ELITE"
}

// Response 200 — retorna URL de pago de Wompi
{
  "paymentUrl": "https://checkout.wompi.co/l/abc123",
  "planType": "ELITE",
  "price": 199900,
  "currency": "COP"
}
```

> El frontend redirige al usuario a `paymentUrl`. Wompi maneja el cobro
> (PSE, Nequi, tarjeta, Bancolombia). Cuando el pago se confirma, Wompi
> envía un webhook al backend.

---

## 7. Seguridad

### 7.1 JWT

**Estructura del token:**
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "role": "ADMIN",
  "plan": "PRO",
  "iat": 1714000000,
  "exp": 1714003600
}
```

> **Nota:** El JWT ya NO incluye `businessId` porque un admin ELITE puede tener
> múltiples negocios. El `businessId` se envía como header `X-Business-Id` en cada
> request que opera sobre un negocio específico (productos, órdenes, mesas, etc.).
> Para usuarios con rol CASHIER/WAITER/KITCHEN, el `businessId` se resuelve
> automáticamente desde su `TeamMember` record.

| Token | Duración | Uso |
|-------|----------|-----|
| Access Token | 8 horas | Autenticación en cada request |
| Refresh Token | 7 días | Obtener nuevo access token |

**Flujo:**
1. Login exitoso → retorna `accessToken` + `refreshToken`
2. Cada request envía `Authorization: Bearer {accessToken}`
3. `JwtAuthFilter` intercepta, valida, extrae claims y setea `SecurityContext`
4. Token expirado → cliente usa `/auth/refresh` con el refresh token

### 7.2 Password

- Hashing: **BCrypt** con strength 12
- Validación: mínimo 8 caracteres, al menos 1 mayúscula, 1 número

### 7.3 OTP por correo (Resend)

**Proveedor:** [Resend](https://resend.com) — API transaccional de email.

**Flujo completo:**
1. `POST /auth/register` → crea usuario con `emailVerified = false`
2. Genera código OTP de 6 dígitos, guarda en `otp_tokens` con expiración 10 min
3. Envía email con el código via **Resend API** (`POST https://api.resend.com/emails`)
4. `POST /auth/verify-otp` → valida código + expiración, marca `emailVerified = true`
   → Busca invitaciones PENDING para ese email → crea TeamMember por cada una y marca como ACCEPTED
5. Si OTP expira → el usuario puede pedir reenvío (`POST /auth/resend-otp`)
6. Máximo 3 intentos fallidos → bloqueo temporal 15 min
7. Login solo permitido si `emailVerified = true`

**Integración con Resend:**
```java
// Puerto (domain)
public interface EmailService {
    void sendOtp(String to, String code);
}

// Adaptador (infrastructure)
@Component
public class ResendEmailService implements EmailService {
    private final String apiKey;          // RESEND_API_KEY desde env
    private final String fromEmail;       // "noreply@example.com"
    private final RestClient restClient;

    @Override
    public void sendOtp(String to, String code) {
        // POST https://api.resend.com/emails
        // { "from": "Mezo <noreply@example.com>", "to": [to],
        //   "subject": "Tu código de verificación",
        //   "html": "<p>Tu código es: <strong>{code}</strong></p>" }
    }
}
```

### 7.4 CORS

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "${mezo.cors.allowed-origins}"   // ej: http://localhost:3000, https://app.mezo.co
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

> En desarrollo: `http://localhost:3000`. En producción: `https://app.mezo.co`.
> Se configura via `mezo.cors.allowed-origins` en `application.yml`.

---

## 8. Autorización (RBAC)

### 8.1 Roles y permisos

```java
public enum Role {
    ADMIN,      // acceso total
    CASHIER,    // órdenes + ventas
    WAITER,     // órdenes
    KITCHEN     // ver órdenes (solo lectura)
}
```

| Recurso | ADMIN | CASHIER | WAITER | KITCHEN |
|---------|-------|---------|--------|---------|
| Business (CRUD) | ✅ | ❌ | ❌ | ❌ |
| Products (CRUD) | ✅ | ❌ | ❌ | ❌ |
| Categories (CRUD) | ✅ | ❌ | ❌ | ❌ |
| Tables (CRUD) | ✅ | ❌ | ❌ | ❌ |
| Orders (crear) | ✅ | ✅ | ✅ | ❌ |
| Orders (ver) | ✅ | ✅ | ✅ | ✅ |
| Orders (cambiar estado) | ✅ | ✅ | ❌ | ✅ |
| Sales (cerrar) | ✅ | ✅ | ❌ | ❌ |
| Sales (ver) | ✅ | ✅ | ❌ | ❌ |
| Reports | ✅ | ❌ | ❌ | ❌ |
| Analytics | ✅ | ❌ | ❌ | ❌ |
| Team (gestionar) | ✅ | ❌ | ❌ | ❌ |
| Plans | ✅ | ❌ | ❌ | ❌ |

### 8.2 Implementación en Spring Security

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/plans").permitAll()
                .requestMatchers("/api/v1/webhooks/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

```java
// En controllers se usa @PreAuthorize
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/products")
public ResponseEntity<ProductResponse> createProduct(...) { ... }

@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAITER')")
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(...) { ... }

@PreAuthorize("hasRole('KITCHEN') or hasRole('ADMIN')")
@GetMapping("/orders")
public ResponseEntity<List<OrderResponse>> listOrders(...) { ... }
```

### 8.3 Multi-tenancy por negocio

El `businessId` viaja **en la URL** de cada endpoint que opera sobre un negocio.
El backend valida que el usuario pertenece a ese negocio antes de responder.

**Patrón de URL:**
```
/api/v1/businesses/{businessId}/products
/api/v1/businesses/{businessId}/categories
/api/v1/businesses/{businessId}/tables
/api/v1/businesses/{businessId}/orders
/api/v1/businesses/{businessId}/sales
/api/v1/businesses/{businessId}/reports/...
/api/v1/businesses/{businessId}/analytics
/api/v1/businesses/{businessId}/team
```

**Validación de pertenencia (filter):**
```java
@Component
public class BusinessAccessFilter extends OncePerRequestFilter {

    // Se ejecuta en CADA request que tenga {businessId} en la URL.
    // Valida que el usuario autenticado pertenece al negocio:
    //   1. Es owner (business.ownerId == user.id)  → OK
    //   2. Es miembro del equipo (team_members.userId == user.id
    //      AND team_members.businessId == businessId)  → OK
    //   3. No pertenece → 403 Forbidden

    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        UUID businessId = extractBusinessIdFromUrl(request);
        if (businessId == null) {
            // Request que no opera sobre un negocio (ej: /auth/login, /plans)
            filterChain.doFilter(request, response);
            return;
        }

        UUID userId = getAuthenticatedUserId();
        if (!belongsToBusiness(userId, businessId)) {
            response.sendError(403, "No tienes acceso a este negocio");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean belongsToBusiness(UUID userId, UUID businessId) {
        // Owner check
        if (businessRepository.existsByIdAndOwnerId(businessId, userId)) return true;
        // Team member check
        if (teamRepository.existsByUserIdAndBusinessId(userId, businessId)) return true;
        return false;
    }
}
```

**En los controllers:**
```java
@RestController
@RequestMapping("/api/v1/businesses/{businessId}/products")
public class ProductController {
    // businessId ya está validado por BusinessAccessFilter
    // Se extrae directo del @PathVariable
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @PathVariable UUID businessId,
            @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(201)
            .body(createProductUseCase.execute(request, businessId));
    }
}
```

---

## 9. Casos de Uso Clave (paso a paso)

### 9.1 Registro de usuario

```
1. Cliente → POST /auth/register { email, password }
2. RegisterUserUseCase:
   a. Validar formato email (Value Object)
   b. Verificar email no existe → 409 si duplicado
   c. Validar password (min 8 chars, 1 mayúscula, 1 número)
   d. Hash password con BCrypt
   e. Verificar si hay invitaciones PENDING para este email:
      - Si hay → crear User con role de la invitación (no ADMIN), plan=null (hereda del owner)
      - Si no hay → crear User(role=ADMIN, plan=PRO, planStartedAt=now, planExpiresAt=now+30days)
   f. Generar OTP 6 dígitos
   g. Guardar OtpToken(code, email, expiresAt=now+10min)
   h. Enviar email con código
   i. Retornar 201 { message, userId }
```

### 9.2 Login

```
1. Cliente → POST /auth/login { email, password }
2. LoginUseCase:
   a. Buscar usuario por email → 401 si no existe
   b. Verificar emailVerified == true → 401 si no verificado
   c. Comparar password con BCrypt → 401 si no coincide
   d. Generar accessToken (8h) con claims (sub, email, role, plan)
   e. Generar refreshToken (7d)
   f. Cargar lista de negocios del usuario (si ADMIN: por owner_id, si empleado: por team_members)
   g. Retornar 200 { accessToken, refreshToken, expiresIn, user { ..., plan, businesses[] } }
```

### 9.3 Crear negocio (onboarding)

```
1. Cliente → POST /businesses { name, type, phone, address, city, country, openAt, closeAt, tableCount }
2. CreateBusinessUseCase:
   a. Obtener usuario autenticado (del JWT)
   b. Contar negocios actuales del usuario (owner_id = user.id)
   c. PlanEnforcer.validateCanCreateBusiness(user, currentCount)
      → SEMILLA/PRO: max 1 negocio → 422 si ya tiene uno
      → ELITE: ilimitado
   d. Validar campos requeridos
   e. Validar tableCount según plan del usuario
      → SEMILLA: max 4 mesas
   f. Crear Business con ownerId = user.id
   g. Crear RestaurantTable × tableCount (numeradas 1..N)
   h. Retornar 201 { business }
```

### 9.4 Crear mesa

```
1. Cliente → POST /businesses/{businessId}/tables   (sin body)
2. BusinessAccessFilter valida pertenencia → 403 si no
3. CreateTableUseCase:
   a. Resolver owner del negocio → obtener user.plan
   b. PlanEnforcer.validatePlanNotExpired(owner) → 402 si expiró
   c. Contar mesas actuales del negocio
   d. PlanEnforcer.validateCanCreateTable(owner, currentCount) → 422 si excede límite
   e. Obtener número máximo actual: maxNumber = tableRepository.findMaxNumberByBusinessId(businessId) ?? 0
   f. Crear RestaurantTable(number = maxNumber + 1, businessId)
   g. Retornar 201 { table }
```

### 9.5 Eliminar mesa

```
1. Cliente → DELETE /businesses/{businessId}/tables/{id}
2. BusinessAccessFilter valida pertenencia → 403 si no
3. DeleteTableUseCase:
   a. Buscar mesa → 404 si no existe
   b. Soft delete
   c. Retornar 204
```

### 9.6 Crear categoría

```
1. Cliente → POST /businesses/{businessId}/categories { name, icon }
2. BusinessAccessFilter valida pertenencia → 403 si no
3. CreateCategoryUseCase:
   a. Resolver owner → obtener user.plan
   b. PlanEnforcer.validatePlanNotExpired(owner) → 402 si expiró
   c. Contar categorías actuales del negocio
   d. PlanEnforcer.validateCanCreateCategory(owner, currentCount) → 422 si excede límite
   e. Validar nombre no duplicado en el negocio → 409 si existe
   f. Asignar sortOrder = max actual + 1
   g. Crear Category(name, icon, sortOrder, businessId)
   h. Retornar 201 { category }
```

### 9.7 Actualizar categoría

```
1. Cliente → PUT /businesses/{businessId}/categories/{id} { name, icon }
2. BusinessAccessFilter valida pertenencia → 403 si no
3. UpdateCategoryUseCase:
   a. Buscar categoría → 404 si no existe
   b. Validar nombre no duplicado (excluyendo la actual) → 409 si existe
   c. Actualizar campos
   d. Retornar 200 { category }
```

### 9.8 Eliminar categoría

```
1. Cliente → DELETE /businesses/{businessId}/categories/{id}
2. BusinessAccessFilter valida pertenencia → 403 si no
3. DeleteCategoryUseCase:
   a. Buscar categoría → 404 si no existe
   b. Verificar que no tenga productos asociados → 422 si tiene
   c. Soft delete
   d. Retornar 204
```

### 9.9 Crear producto

```
1. Cliente → POST /businesses/{businessId}/products { name, price, currency, description?, ingredients?, imageType, image, categoryId }
2. BusinessAccessFilter valida pertenencia → 403 si no
3. CreateProductUseCase:
   a. Resolver owner → obtener user.plan
   b. PlanEnforcer.validatePlanNotExpired(owner) → 402 si expiró
   c. Contar productos actuales del negocio
   d. PlanEnforcer.validateCanCreateProduct(owner, currentCount) → 422 si excede límite
   e. Validar que categoryId existe y pertenece al negocio → 404 si no
   f. Validar price > 0
   g. Crear Product(name, price, currency, description, ingredients, imageType, image, categoryId, businessId)
   h. Retornar 201 { product }
```

### 9.10 Actualizar producto

```
1. Cliente → PUT /businesses/{businessId}/products/{id} { name, price, currency, description?, ingredients?, imageType, image, categoryId }
2. BusinessAccessFilter valida pertenencia → 403 si no
3. UpdateProductUseCase:
   a. Buscar producto → 404 si no existe
   b. Si cambia categoryId → validar que la nueva categoría existe y pertenece al negocio
   c. Validar price > 0
   d. Actualizar campos
   e. Retornar 200 { product }
```

### 9.11 Eliminar producto

```
1. Cliente → DELETE /businesses/{businessId}/products/{id}
2. BusinessAccessFilter valida pertenencia → 403 si no
3. DeleteProductUseCase:
   a. Buscar producto → 404 si no existe
   b. Soft delete (no se elimina fisicamente, las órdenes históricas lo referencian)
   c. Retornar 204
```

### 9.12 Toggle disponibilidad de producto

```
1. Cliente → PATCH /businesses/{businessId}/products/{id}/toggle
2. BusinessAccessFilter valida pertenencia → 403 si no
3. ToggleProductUseCase:
   a. Buscar producto → 404 si no existe
   b. Invertir available (true ↔ false)
   c. Retornar 200 { product }
```

### 9.13 Invitar miembro al equipo

El admin invita con email y rol. Se crea una invitación pendiente.
Si el usuario ya tiene cuenta, se vincula directo. Si no, se le envía un email
para que se registre — al completar el registro, las invitaciones pendientes
se aceptan automáticamente.

```
1. Cliente → POST /businesses/{businessId}/team/invite { email, role }
   role: ADMIN | CASHIER | WAITER | KITCHEN
2. BusinessAccessFilter valida pertenencia → 403 si no
3. @PreAuthorize("hasRole('ADMIN')")
4. InviteMemberUseCase:
   a. Resolver owner → obtener user.plan
   b. PlanEnforcer.validatePlanNotExpired(owner) → 402 si expiró
   c. Contar miembros actuales del negocio
   d. PlanEnforcer.validateCanInviteMember(owner, currentCount) → 422 si excede límite
   e. Verificar que no exista invitación pendiente o membresía activa con ese email → 409 si duplicado
   f. Buscar usuario por email:
      CASO A — Usuario ya existe y tiene emailVerified=true:
        - Crear TeamMember(userId, businessId, role, invitedBy) directo
        - Enviar email notificando que fue agregado al negocio
        - Retornar 201 { teamMember, status: "ACTIVE" }
      CASO B — Usuario no existe:
        - Crear Invitation(email, businessId, role, invitedBy, status=PENDING)
        - Enviar email invitación via Resend: "Te invitaron a {negocio}, regístrate en mezo.co"
        - Retornar 201 { invitation, status: "PENDING" }
```

**Entidad Invitation:**
```java
@Entity
@Table(name = "invitations")
public class Invitation extends BaseEntity {
    @Column(nullable = false)
    private String email;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;  // PENDING, ACCEPTED, EXPIRED
}
```

**Auto-aceptar al registrarse (hook en RegisterUserUseCase):**
```
Después del paso de verificar OTP (emailVerified=true):
   - Buscar invitaciones pendientes por email
   - Para cada invitación PENDING:
     - Crear TeamMember(userId, businessId, role, invitedBy)
     - Marcar invitación como ACCEPTED
   - El usuario queda vinculado automáticamente a los negocios que lo invitaron
```

### 9.14 Cambiar rol de miembro

```
1. Cliente → PUT /businesses/{businessId}/team/{userId}/role { role }
2. BusinessAccessFilter valida pertenencia → 403 si no
3. @PreAuthorize("hasRole('ADMIN')")
4. UpdateMemberRoleUseCase:
   a. Buscar TeamMember por userId + businessId → 404 si no existe
   b. No puede cambiar su propio rol → 422
   c. Actualizar role
   d. Retornar 200 { teamMember }
```

### 9.15 Eliminar miembro del equipo

```
1. Cliente → DELETE /businesses/{businessId}/team/{userId}
2. BusinessAccessFilter valida pertenencia → 403 si no
3. @PreAuthorize("hasRole('ADMIN')")
4. RemoveMemberUseCase:
   a. Buscar TeamMember por userId + businessId → 404 si no existe
   b. No puede eliminarse a sí mismo → 422
   c. No puede eliminar al owner del negocio → 422
   d. Eliminar TeamMember record
   e. Retornar 204
```

### 9.16 Listar equipo

```
1. Cliente → GET /businesses/{businessId}/team
2. BusinessAccessFilter valida pertenencia → 403 si no
3. @PreAuthorize("hasRole('ADMIN')")
4. ListTeamUseCase:
   a. Consultar teamRepository.findAllByBusinessId(businessId)
   b. Para cada miembro, incluir: userId, email, role, createdAt
   c. Retornar 200 [ teamMember, ... ]
```

### 9.17 Suscribirse a un plan (Wompi)

```
1. Cliente → POST /plans/subscribe { planType: "ELITE" }
2. SubscribePlanUseCase:
   a. Verificar usuario autenticado es ADMIN
   b. Obtener plan solicitado → 404 si no existe
   c. Validar que no esté suscribiéndose al plan que ya tiene → 422
   d. Crear PaymentLink via PaymentGateway (Wompi):
      - reference: "sub_{userId}_{planType}_{timestamp}"
      - amount: plan.price en centavos
      - redirect_url: frontend URL post-pago
   e. Crear/actualizar Subscription(userId, planType, status=TRIAL o PENDING)
   f. Retornar 200 { paymentUrl, planType, price, currency }
3. Frontend redirige al usuario a paymentUrl (checkout Wompi)
4. Usuario paga (PSE, Nequi, tarjeta, etc.)
5. Wompi envía webhook al backend
```

### 9.18 Procesar webhook de Wompi

```
1. Wompi → POST /webhooks/wompi { event, data, signature, ... }
2. HandlePaymentWebhookUseCase:
   a. Validar firma del webhook (HMAC SHA256 con WOMPI_EVENTS_SECRET) → 401 si inválida
   b. Extraer evento:

   EVENTO: transaction.updated (status = APPROVED)
     - Extraer reference → parsear userId y planType
     - Buscar usuario → 404 si no existe
     - Actualizar user.plan = planType
     - Actualizar user.planStartedAt = now
     - Actualizar user.planExpiresAt = now + 30 días
     - Actualizar Subscription:
       - status = ACTIVE
       - wompiTransactionId = transaction.id
       - currentPeriodStart = now
       - currentPeriodEnd = now + 30 días
     - Retornar 200

   EVENTO: transaction.updated (status = DECLINED/ERROR)
     - Log del fallo
     - No cambiar plan del usuario
     - Retornar 200

   EVENTO: recurring_payment (pago recurrente mensual)
     - Si APPROVED:
       - Extender user.planExpiresAt += 30 días
       - Actualizar Subscription.currentPeriodEnd
       - status = ACTIVE
     - Si DECLINED:
       - Marcar Subscription.status = PAST_DUE
       - (el PlanEnforcer bloqueará cuando planExpiresAt pase)
     - Retornar 200

3. Siempre retornar 200 (Wompi reintenta si recibe error)
```

### 9.19 Crear orden

```
1. Cliente → POST /businesses/{businessId}/orders { tableId?, paymentMethod, tip, lines[] }
   tableId es opcional — si es null, es una orden directa desde POS (sin mesa)
2. CreateOrderUseCase:
   a. Verificar businessId (ya validado por BusinessAccessFilter)
   b. Si tableId != null:
      - Validar que la mesa existe y pertenece al negocio
   c. Para cada line:
      - Buscar producto por ID → 404 si no existe
      - Validar producto.available == true
      - Validar producto pertenece al mismo negocio
      - Crear OrderLine(productId, productName, unitPrice, quantity)
   d. Crear Order con status=OPEN, tableId=null si no se envió
   e. Order.recalculateTotal() (lógica en el dominio)
   f. Guardar orden
   g. Retornar 201 { order }
```

### 9.20 Cerrar venta

Soporta dos flujos:
- **Desde mesa/orden existente:** envía `orderId` → cierra la orden y crea la venta.
- **Venta directa desde POS:** envía `lines[]` sin `orderId` → crea la orden Y la venta en un solo request.

```
1. Cliente → POST /businesses/{businessId}/sales { orderId?, tableId?, paymentMethod, tip, lines[]? }
2. CloseSaleUseCase:

   FLUJO A — Con orderId (orden existente, viene de mesa):
   a. Buscar orden → 404 si no existe
   b. Validar orden.status == DELIVERED → 422 si no
   c. Validar orden pertenece al negocio
   d. Cambiar orden.status = CLOSED
   e. Crear Sale(orderId, total, tip, paymentMethod, tableId, closedBy)
   f. Retornar 201 { sale }

   FLUJO B — Sin orderId (venta directa POS):
   a. Validar que lines[] no esté vacío → 400 si vacío
   b. Validar cada producto (existe, disponible, pertenece al negocio)
   c. Crear Order(lines, paymentMethod, tip, tableId=null, status=CLOSED)
   d. Crear Sale(orderId, total, tip, paymentMethod, tableId=null, closedBy)
   e. Retornar 201 { sale }
```

### 9.21 Generar reportes

```
1. Cliente → GET /businesses/{businessId}/reports/sales?time=MONTH
2. BusinessAccessFilter valida que el usuario pertenece al negocio → 403 si no
3. GetSalesTotalUseCase:
   a. Verificar usuario tiene rol ADMIN
   b. Resolver owner del negocio → obtener user.plan
   c. PlanEnforcer.validateCanAccessReports(owner) → 422 si plan expirado, 402 si no tiene reportes
   d. Calcular from/to a partir de time:
      - DAY    → from = hoy 00:00, to = ahora
      - WEEK   → from = hace 7 días, to = ahora
      - MONTH  → from = hace 4 semanas, to = ahora
      - QUARTER→ from = hace 3 meses, to = ahora
      - YEAR   → from = hace 12 meses, to = ahora
   e. Consultar SaleRepository.sumByBusinessAndDateRange(businessId, from, to)
   f. Retornar { time, from, to, totalSales, totalTips, salesCount }
```

> La misma lógica de resolución de `time → from/to` aplica para todos los
> endpoints de reportes y analytics. Se centraliza en un `TimeRangeResolver` compartido.

---

## 10. Reglas de Negocio (Enforced en Domain)

### 10.1 PlanEnforcer

```java
@Component
public class PlanEnforcer {

    private final PlanRepository planRepository;
    private final BusinessRepository businessRepository;

    // El plan se resuelve desde el USUARIO (admin/owner), no desde el negocio.
    // Para empleados (CASHIER, WAITER, KITCHEN), se resuelve:
    //   teamMember.businessId → business.ownerId → owner.plan

    // =============================================
    // VALIDACIÓN DE EXPIRACIÓN — se llama ANTES de cualquier operación.
    // Si el plan expiró, el usuario queda bloqueado hasta que pague o haga downgrade.
    // =============================================
    public void validatePlanNotExpired(User owner) {
        if (owner.getPlanExpiresAt() != null
                && LocalDateTime.now().isAfter(owner.getPlanExpiresAt())) {
            throw new PlanExpiredException(
                "Tu plan " + owner.getPlan() + " ha expirado. "
                + "Renueva tu suscripción para continuar."
            );
        }
    }

    public void validateCanCreateBusiness(User owner, int currentBusinessCount) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner.getPlan());
        if (plan.getMaxBusinesses() != -1 && currentBusinessCount >= plan.getMaxBusinesses()) {
            throw new PlanLimitExceededException(
                "El plan " + plan.getType() + " permite máximo " + plan.getMaxBusinesses() + " negocio(s)"
            );
        }
    }

    public void validateCanCreateTable(User owner, int currentTableCount) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner.getPlan());
        if (plan.getMaxTables() != -1 && currentTableCount >= plan.getMaxTables()) {
            throw new PlanLimitExceededException(
                "El plan " + plan.getType() + " permite máximo " + plan.getMaxTables() + " mesas"
            );
        }
    }

    public void validateCanCreateProduct(User owner, int currentProductCount) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner.getPlan());
        if (plan.getMaxProducts() != -1 && currentProductCount >= plan.getMaxProducts()) {
            throw new PlanLimitExceededException(
                "El plan " + plan.getType() + " permite máximo " + plan.getMaxProducts() + " productos"
            );
        }
    }

    public void validateCanCreateCategory(User owner, int currentCategoryCount) {
        validatePlanNotExpired(owner);
        // ... misma lógica de límite
    }

    public void validateCanInviteMember(User owner, int currentMemberCount) {
        validatePlanNotExpired(owner);
        // ... misma lógica de límite
    }

    public void validateCanAccessReports(User owner) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner.getPlan());
        if (!plan.isReportsEnabled()) {
            throw new PlanLimitExceededException(
                "El plan " + plan.getType() + " no incluye reportes. Actualiza a PRO o ELITE."
            );
        }
    }

    public void validateCanAccessAnalytics(User owner) {
        validatePlanNotExpired(owner);
        // ... misma lógica que reports
    }

    // Helper: resuelve el owner (admin) de un negocio para empleados
    public User resolveOwner(UUID businessId) {
        Business business = businessRepository.findById(businessId).orElseThrow();
        return userRepository.findById(business.getOwnerId()).orElseThrow();
    }
}
```

### 10.2 Límites por plan

| Recurso | SEMILLA | PRO | ELITE |
|---------|---------|-----|-------|
| **Negocios** | **1** | **1** | **ilimitado (-1)** |
| Mesas (por negocio) | 4 | ilimitado (-1) | ilimitado (-1) |
| Productos (por negocio) | 25 | ilimitado (-1) | ilimitado (-1) |
| Categorías (por negocio) | 3 | ilimitado (-1) | ilimitado (-1) |
| Empleados (por negocio) | 3 | ilimitado (-1) | ilimitado (-1) |
| Reportes | ❌ | ✅ | ✅ |
| Analítica | ❌ | ✅ | ✅ |
| Precio/mes (COP) | $39.900 | $99.900 | $199.900 |
| Trial | — | **30 días gratis (plan inicial al registrarse)** | — |

> **Importante:** Los límites de mesas, productos, categorías y empleados se
> aplican **por negocio**, no globalmente. Un admin ELITE con 3 negocios tiene
> los límites ilimitados en cada uno de ellos independientemente.

### 10.3 Validaciones de dominio clave

| Regla | Dónde se enforce |
|-------|-----------------|
| Email válido | Value Object `Email` |
| Money >= 0 | Value Object `Money` |
| Solo órdenes OPEN se pueden modificar (agregar/eliminar líneas, cancelar) | `Order.assertOpen()` |
| Solo órdenes DELIVERED se pueden cerrar como venta | `CloseSaleUseCase` |
| No se puede eliminar categoría con productos | `DeleteCategoryUseCase` |
| Producto debe pertenecer al negocio del usuario | `CreateOrderUseCase` |
| Mesa debe pertenecer al negocio del usuario | `CreateOrderUseCase` |
| businessId viene de la URL (`@PathVariable`), nunca del body | Todos los use cases |
| Usuario debe pertenecer al negocio (owner o team member) | `BusinessAccessFilter` |
| Reportes solo disponibles en plan PRO/ELITE | `PlanEnforcer` (valida contra `user.plan`) |
| SEMILLA y PRO: máximo 1 negocio por usuario | `PlanEnforcer.validateCanCreateBusiness()` |
| ELITE: negocios ilimitados por usuario | `PlanEnforcer.validateCanCreateBusiness()` |
| Límites de recursos se aplican por negocio, no global | `PlanEnforcer` |
| Plan expirado bloquea TODAS las operaciones (excepto login, ver plan, upgrade) | `PlanEnforcer.validatePlanNotExpired()` → 402 |
| Usuario nuevo recibe plan PRO con 30 días gratis | `RegisterUserUseCase` |
| `planExpiresAt = null` significa plan pagado (sin expiración) | `PlanEnforcer` |
| Máximo 3 intentos OTP fallidos → bloqueo 15 min | `VerifyOtpUseCase` |
| OTP expira en 10 minutos | `VerifyOtpUseCase` |

### 10.4 Data Seeding (planes iniciales)

```java
@Component
public class DataSeeder implements CommandLineRunner {
    @Override
    public void run(String... args) {
        if (planRepository.count() == 0) {
            //                    type    tables empl cats prods businesses reports  price              trial
            planRepository.save(new Plan(SEMILLA, 4,    3,   3,   25,   1,         false,   new Money(39900, COP), 0));
            planRepository.save(new Plan(PRO,    -1,   -1,  -1,  -1,   1,         true,    new Money(99900, COP), 30));
            planRepository.save(new Plan(ELITE,  -1,   -1,  -1,  -1,  -1,         true,    new Money(199900, COP), 0));
        }
    }
}
```

---

## 11. Escalabilidad

### 11.1 Monolito → Microservicios

La estructura modular permite extraer módulos a microservicios sin refactor mayor:

```
Fase 1 (actual):    Monolito modular con paquetes separados
Fase 2:             Extraer auth como servicio independiente
Fase 3:             Extraer order+sale como servicio independiente
Fase 4:             Extraer report+analytics como servicio independiente
```

### 11.2 Eventos internos (preparación)

```java
// Spring Application Events para comunicación entre módulos
public record OrderCreatedEvent(UUID orderId, UUID businessId, UUID tableId) {}
public record SaleClosedEvent(UUID saleId, UUID orderId, UUID businessId) {}
public record UserRegisteredEvent(UUID userId, String email) {}

// Publicar
@Component
public class SpringEventPublisher implements EventPublisher {
    @Autowired
    private ApplicationEventPublisher publisher;

    public void publish(Object event) {
        publisher.publishEvent(event);
    }
}

// Escuchar
@Component
public class OrderEventListener {
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        // Notificar cocina, etc.
    }
}
```

### 11.3 Separación de lectura/escritura (futuro)

Los reportes y analítica pueden migrar a un modelo CQRS ligero:
- **Escritura**: operaciones normales JPA
- **Lectura**: vistas materializadas o queries optimizadas con projections

---

## 12. Consideraciones Futuras

### 12.1 Migración H2 → PostgreSQL

Solo requiere:
1. Cambiar dependencia en `pom.xml` (`h2` → `postgresql`)
2. Cambiar `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/mezo
       username: mezo
       password: ${DB_PASSWORD}
     jpa:
       database-platform: org.hibernate.dialect.PostgreSQLDialect
   ```
3. Migrar schema con **Flyway** (agregar scripts SQL versionados)

### 12.2 Multi-tenant

Ya implementado desde el inicio:
- El `businessId` viaja en la URL de cada request (`/businesses/{businessId}/...`)
- `BusinessAccessFilter` valida pertenencia (owner o team member) antes de procesar
- Cada entidad tiene `businessId` como FK
- Un admin ELITE puede crear N negocios independientes
- Cada negocio tiene sus propios productos, categorías, mesas, órdenes y equipo
- Los límites de plan se aplican por negocio (no globalmente)

### 12.3 Integración con pagos

Ya implementado con **Wompi** en el módulo `plan/`:
- `PaymentGateway` (puerto) → `WompiPaymentGateway` (adaptador)
- Suscripciones recurrentes via webhooks
- Soporta PSE, Nequi, tarjetas, Bancolombia
- Si en el futuro se necesita otro proveedor, solo se crea un nuevo adaptador que implemente `PaymentGateway`

### 12.4 Features de IA (plan ELITE — propuestas)

| Feature | Descripción | Modelo sugerido |
|---------|-------------|-----------------|
| Predicción de demanda | Estimar ventas por hora/día para planificar inventario | Claude / time-series model |
| Sugerencias de menú | Recomendar combos basados en patrones de compra | Claude |
| Resumen diario | Generar texto ejecutivo del día de ventas | Claude |
| Detección de anomalías | Alertar ventas inusuales (muy altas/bajas) | Statistical model |
| Chatbot de reportes | "¿Cuánto vendí la semana pasada?" en lenguaje natural | Claude |

---

## 13. Configuración (application.yml)

```yaml
spring:
  application:
    name: mezo-pos

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
    show-sql: true
    properties:
      hibernate:
        format_sql: true

mezo:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiration: 28800000    # 8 horas
    refresh-token-expiration: 604800000  # 7 días
  resend:
    api-key: ${RESEND_API_KEY}
    from-email: "Mezo <noreply@example.com>"
  wompi:
    public-key: ${WOMPI_PUBLIC_KEY}
    private-key: ${WOMPI_PRIVATE_KEY}
    events-secret: ${WOMPI_EVENTS_SECRET}
    redirect-url: ${WOMPI_REDIRECT_URL:https://app.mezo.co/plans/success}
  cloudinary:
    cloud-name: ${CLOUDINARY_CLOUD_NAME}
    api-key: ${CLOUDINARY_API_KEY}
    api-secret: ${CLOUDINARY_API_SECRET}
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
  otp:
    expiration-minutes: 10
    max-attempts: 3
    block-duration-minutes: 15
```

---

## 14. Dependencias (pom.xml)

```xml
<dependencies>
    <!-- Core -->
    <dependency>spring-boot-starter-web</dependency>
    <dependency>spring-boot-starter-data-jpa</dependency>
    <dependency>spring-boot-starter-security</dependency>
    <dependency>spring-boot-starter-validation</dependency>
    <!-- Email via Resend (HTTP API, no SMTP) -->
    <!-- Se usa RestClient de Spring Web, no requiere dependencia extra -->

    <!-- Database -->
    <dependency>com.h2database:h2 (runtime)</dependency>

    <!-- JWT -->
    <dependency>io.jsonwebtoken:jjwt-api:0.12.x</dependency>
    <dependency>io.jsonwebtoken:jjwt-impl:0.12.x (runtime)</dependency>
    <dependency>io.jsonwebtoken:jjwt-jackson:0.12.x (runtime)</dependency>

    <!-- Image upload -->
    <dependency>com.cloudinary:cloudinary-http44:1.x</dependency>

    <!-- Utils -->
    <dependency>org.projectlombok:lombok (optional)</dependency>
    <dependency>org.mapstruct:mapstruct:1.5.x</dependency>

    <!-- Testing -->
    <dependency>spring-boot-starter-test</dependency>
    <dependency>spring-security-test</dependency>
</dependencies>
```
