# Mezo POS Backend

## Proyecto

Backend del sistema SaaS **Mezo POS** — gestión de restaurantes/cafeterías para Colombia.

### Documentación clave
- `SPEC.md` — especificación técnica completa (arquitectura, entidades, reglas de negocio)
- `API_GUIDE.md` — guía de endpoints para el frontend (request/response, flujos, permisos)
- `README.md` — setup, estructura del proyecto, cómo levantar
- `Mezo_POS.postman_collection.json` — colección Postman con todos los endpoints

Cualquier implementación debe seguir estrictamente el SPEC.

## Stack

- Java 21
- Spring Boot 3.x
- JPA / Hibernate
- H2 (desarrollo) → PostgreSQL (producción)
- JWT (jjwt) — access token 8h, refresh token 7d
- Resend (email transaccional para OTP y bienvenida)
- Wompi (pagos y suscripciones, sandbox por defecto)
- Local filesystem (imágenes de productos en `./uploads/`)
- Maven
- JUnit 5 + Mockito

## Arquitectura

**Hexagonal (Ports & Adapters)** en monolito modular.

Regla de dependencia estricta:
- `domain` → no depende de nada externo
- `application` → depende solo de `domain`
- `infrastructure` → depende de `application` y `domain`

Cada módulo sigue la estructura:
```
modulo/
├── domain/
│   ├── entity/
│   ├── port/        ← interfaces (driven ports)
│   └── enums/
├── application/     ← use cases
└── infrastructure/
    ├── adapter/     ← implementaciones JPA, Wompi, Resend, LocalStorage
    └── web/
        ├── Controller.java
        ├── dto/
        └── mapper/
```

## Módulos

`auth` `business` `product` `table` `order` `sale` `report` `analytics` `team` `plan` `shared`

## Reglas importantes

- El **plan pertenece al usuario** (admin), no al negocio. Un admin ELITE puede tener N negocios.
- Usuarios invitados (CASHIER, WAITER, KITCHEN) no tienen plan propio — heredan del owner.
- **Multi-tenant via URL**: todos los recursos van bajo `/api/v1/businesses/{businessId}/...`
- `BusinessAccessFilter` valida que el usuario pertenece al negocio en cada request.
- `PlanEnforcer` valida límites y expiración del plan. Se llama en TODOS los use cases de escritura (productos, categorías, mesas, reportes, analytics, equipo).
- Plan expirado → HTTP 402. Límite de plan excedido → HTTP 422.
- Las reglas de negocio viven en el **dominio**, no en controllers.
- Value Objects (`Money`, `Email`, `PhoneNumber`) validan en construcción.
- Soft delete en todas las entidades (`deleted` boolean).
- `businessId` viene del `@PathVariable`, nunca del request body.
- `tableId` es opcional en órdenes (null = venta directa POS sin mesa).
- El backend NO gestiona estado de mesas (ocupada/libre) — eso es frontend.
- Imágenes de productos: si `imageType=EMOJI` → campo `image` es el emoji string. Si `imageType=IMAGE` → se sube a `./uploads/` y `image` es la URL. Endpoint: `POST /products/upload-image` (multipart, max 3MB).
- CORS configurado via `mezo.cors.allowed-origins` (dev: `localhost:3000`, prod: `app.mezo.co`).
- Mensajes de error en **español** para el usuario final.

## Convenciones

- API base: `/api/v1`
- URLs: kebab-case
- JSON: camelCase
- IDs: UUID
- Timestamps: ISO 8601
- Paginación: `?page=0&size=20`
- Reportes y analytics: `?time=DAY|WEEK|MONTH|QUARTER|YEAR` (no from/to)

## Registro y auth

- Usuario nuevo → plan PRO con 30 días gratis
- OTP de 6 dígitos via Resend para verificar email
- `POST /auth/verify-otp` retorna tokens (auto-login al verificar)
- Si el email ya existe pero no está verificado → reenvía OTP en vez de dar error
- Invitaciones de equipo: si el invitado no tiene cuenta, se crea `Invitation` PENDING → al registrarse y verificar OTP, se auto-aceptan las invitaciones
- Email de bienvenida: `POST /api/v1/email/welcome` via Resend

## Entidad Business

Campos: name, type, phone, nit, email, address, city, country, openAt, closeAt, ownerId.
`tableCount` no se persiste — se calcula en tiempo real desde `restaurant_tables`.

## Ventas (tres flujos)

- **Con orderId** (orden existente): cierra la orden en cualquier estado excepto CLOSED/CANCELLED
- **Con tableId** (mesa): cierra TODAS las órdenes activas de la mesa
- **Con lines[] sin orderId** (POS directo): crea orden + venta en un solo request
- Siempre retorna un **array** de ventas

## Mesas

- `GET /tables` retorna `activeOrders[]` con id, status, total de cada orden activa
- `GET /tables/{id}/summary` retorna productos consolidados de todas las órdenes activas
- Una mesa puede tener múltiples órdenes simultáneas

## Pagos (Wompi)

- `POST /plans/subscribe` → crea link de pago Wompi → frontend redirige
- `POST /webhooks/wompi` → webhook público, validado por firma HMAC
- Cobro recurrente mensual automático via Wompi
- `mezo.wompi.sandbox=true` por defecto → usa `sandbox.wompi.co`. Para producción: `WOMPI_SANDBOX=false`

## Frontend

- Repo: `JuanesLizcano/mezo-pos` (rama `feat/configuracion-mejoras`)
- Ya integrado con este backend via `src/services/api.js`
- `src/services/index.js` enruta a `api.js` (backend real) o `mockApi.js` según `REACT_APP_USE_MOCK`
- Mappers EN↔ES en `api.js` (backend usa inglés, frontend usa español)

## Levantar en local

```bash
# Backend (terminal 1)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd mezo-pos-backend
mvn spring-boot:run
# → http://localhost:8080

# Frontend (terminal 2)
cd mezo-pos
npm start
# → http://localhost:3000

# H2 Console
# → http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:mezo, user: sa, sin password)
```

## Commits

Siempre hacer amend al commit anterior (`git commit --amend`) excepto el primer commit de una branch nueva.
