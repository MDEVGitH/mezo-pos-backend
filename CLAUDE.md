# Mezo POS Backend

## Proyecto

Backend del sistema SaaS **Mezo POS** — gestión de restaurantes/cafeterías para Colombia.

### Documentación clave
- `SPEC.md` — especificación técnica completa (arquitectura, entidades, reglas de negocio)
- `API_GUIDE.md` — guía de endpoints para el frontend (request/response, flujos, permisos)
- `README.md` — setup, estructura del proyecto, cómo levantar

Cualquier implementación debe seguir estrictamente el SPEC.

## Stack

- Java 21
- Spring Boot 3.x
- JPA / Hibernate
- H2 (desarrollo) → PostgreSQL (producción)
- JWT (jjwt) — access token 8h, refresh token 7d
- Resend (email transaccional para OTP)
- Wompi (pagos y suscripciones)
- Cloudinary (imágenes de productos)
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
    ├── adapter/     ← implementaciones JPA, Wompi, Resend
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
- `PlanEnforcer` valida límites y expiración del plan. Se llama en TODOS los use cases de escritura.
- Plan expirado → HTTP 402. Límite de plan excedido → HTTP 422.
- Las reglas de negocio viven en el **dominio**, no en controllers.
- Value Objects (`Money`, `Email`, `PhoneNumber`) validan en construcción.
- Soft delete en todas las entidades (`deleted` boolean).
- `businessId` viene del `@PathVariable`, nunca del request body.
- `tableId` es opcional en órdenes (null = venta directa POS sin mesa).
- El backend NO gestiona estado de mesas (ocupada/libre) — eso es frontend.
- Imágenes de productos: si `imageType=EMOJI` → campo `image` es el emoji string. Si `imageType=IMAGE` → se sube a Cloudinary y `image` es la URL.
- CORS configurado via `mezo.cors.allowed-origins` (dev: `localhost:3000`, prod: `app.mezo.co`).

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
- Invitaciones de equipo: si el invitado no tiene cuenta, se crea `Invitation` PENDING → al registrarse y verificar OTP, se auto-aceptan las invitaciones

## Pagos (Wompi)

- `POST /plans/subscribe` → crea link de pago Wompi → frontend redirige
- `POST /webhooks/wompi` → webhook público, validado por firma HMAC
- Cobro recurrente mensual automático via Wompi

## Ventas (dos flujos)

- **Con orderId** (mesa): cierra orden existente con status DELIVERED
- **Con lines[] sin orderId** (POS directo): crea orden + venta en un solo request

## Compilación

Requiere **Java 21** (Maven usa la versión de JAVA_HOME, si tienes Java 25 instalado forzar con):
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn compile
```

## Commits

Siempre hacer amend al commit anterior (`git commit --amend`) excepto el primer commit de una branch nueva.
