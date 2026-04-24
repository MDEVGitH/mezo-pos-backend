# Mezo POS — Guía de API para Frontend

> Documento de referencia para integrar el frontend con el backend.
> Todos los endpoints, request/response, flujos y reglas de negocio.

---

## Tabla de Contenidos

- [Autenticación](#1-autenticación)
- [Negocios](#2-negocios)
- [Categorías](#3-categorías)
- [Productos](#4-productos)
- [Mesas](#5-mesas)
- [Órdenes](#6-órdenes)
- [Ventas](#7-ventas)
- [Reportes](#8-reportes)
- [Analítica (Gráficas)](#9-analítica-gráficas)
- [Equipo](#10-equipo)
- [Planes y Suscripciones](#11-planes-y-suscripciones)
- [Referencia Rápida](#referencia-rápida)

---

## Convenciones

| Concepto | Valor |
|----------|-------|
| Base URL | `/api/v1` |
| Formato | JSON |
| Auth header | `Authorization: Bearer {accessToken}` |
| IDs | UUID |
| Timestamps | ISO 8601 |
| Paginación | `?page=0&size=20` |
| Moneda | Valores en enteros (COP sin decimales). Ej: `8500` = $8.500 COP |

### Códigos de error

| Código | Significado |
|--------|------------|
| 400 | Validación fallida (campos requeridos, formato) |
| 401 | No autenticado (token inválido/expirado, email no verificado) |
| 402 | **Plan expirado** — redirigir a pantalla de upgrade |
| 403 | Sin permisos (rol insuficiente o no pertenece al negocio) |
| 404 | Recurso no encontrado |
| 409 | Conflicto (email duplicado, nombre duplicado) |
| 422 | Regla de negocio violada (límite de plan, estado inválido) |

### Formato de error

```json
{
  "status": 422,
  "error": "PLAN_LIMIT_EXCEEDED",
  "message": "El plan Semilla permite máximo 25 productos",
  "timestamp": "2026-04-29T10:30:00Z"
}
```

---

## 1. Autenticación

Todos los endpoints de auth son **públicos** (no requieren token).

### 1.1 Registrar usuario

```
POST /api/v1/auth/register
```

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

| Campo | Tipo | Validación |
|-------|------|-----------|
| email | string | Requerido, formato email válido |
| password | string | Requerido, mín 8 chars, al menos 1 mayúscula y 1 número |

**Response 201:**
```json
{
  "message": "OTP enviado a user@example.com",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Flujo:**
1. Valida email único → 409 si ya existe
2. Hash password con BCrypt
3. Crea usuario con `role=ADMIN`, `plan=PRO`, trial de 30 días
4. Genera OTP de 6 dígitos, lo envía por email (Resend)
5. El usuario **no puede hacer login** hasta verificar el OTP

**Errores posibles:** 409 (email ya existe), 400 (validación)

---

### 1.2 Verificar OTP

```
POST /api/v1/auth/verify-otp
```

**Request:**
```json
{
  "email": "user@example.com",
  "code": "482931"
}
```

**Response 200:**
```json
{
  "message": "Email verificado correctamente"
}
```

**Flujo:**
1. Busca OTP válido para ese email
2. Verifica que no haya expirado (10 min de vida)
3. Máximo 3 intentos fallidos → bloqueo 15 min
4. Marca `emailVerified = true`
5. Si el usuario fue invitado a equipos, auto-acepta las invitaciones pendientes

**Errores posibles:** 422 (OTP expirado o inválido), 429 (bloqueado por intentos)

---

### 1.3 Reenviar OTP

```
POST /api/v1/auth/resend-otp
```

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response 200:**
```json
{
  "message": "OTP reenviado a user@example.com"
}
```

---

### 1.4 Login

```
POST /api/v1/auth/login
```

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response 200:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 28800,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "role": "ADMIN",
    "plan": "PRO",
    "emailVerified": true,
    "businesses": [
      { "id": "660e8400-...", "name": "Café Mezo" }
    ]
  }
}
```

**Notas para el frontend:**
- `accessToken` → guardar en memoria, enviar en header `Authorization: Bearer {token}`
- `refreshToken` → guardar en localStorage/cookie seguro
- `expiresIn` → 28800 segundos (8 horas)
- `businesses` → lista de negocios. Si está vacía, redirigir a onboarding
- `plan` → plan actual del usuario

**Errores posibles:** 401 (credenciales inválidas o email no verificado)

---

### 1.5 Refresh token

```
POST /api/v1/auth/refresh
```

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response 200:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 28800
}
```

**Cuándo usarlo:** Cuando el `accessToken` expire (401 en cualquier request), llamar este endpoint con el `refreshToken` para obtener uno nuevo sin re-login.

---

## 2. Negocios

> Todos requieren autenticación. Solo ADMIN puede crear/editar/eliminar.

### 2.1 Crear negocio (onboarding)

```
POST /api/v1/businesses
Auth: Bearer {token}
Rol: ADMIN
```

**Request:**
```json
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
```

| Campo | Tipo | Validación |
|-------|------|-----------|
| name | string | Requerido |
| type | string | Requerido. Valores: `RESTAURANT`, `CAFE`, `BAR`, `BAKERY`, `FOOD_TRUCK` |
| phone | string | Opcional, 7-15 caracteres |
| address | string | Requerido |
| city | string | Requerido |
| country | string | Requerido |
| openAt | string | Opcional, formato `HH:mm` |
| closeAt | string | Opcional, formato `HH:mm` |
| tableCount | int | Opcional, default 0 |

**Response 201:**
```json
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
  "createdAt": "2026-04-29T10:30:00Z"
}
```

**Reglas:**
- SEMILLA/PRO: máximo 1 negocio → 422 si ya tiene uno
- ELITE: ilimitados
- `tableCount` se valida contra el plan (SEMILLA: max 4)
- Se crean las mesas automáticamente numeradas del 1 al N

---

### 2.2 Listar mis negocios

```
GET /api/v1/businesses
Auth: Bearer {token}
```

**Response 200:** Array de negocios del usuario.

---

### 2.3 Obtener negocio

```
GET /api/v1/businesses/{id}
Auth: Bearer {token}
```

---

### 2.4 Actualizar negocio

```
PUT /api/v1/businesses/{id}
Auth: Bearer {token}
Rol: ADMIN
```

**Request:** Mismos campos que crear (todos opcionales para update parcial).

---

### 2.5 Abrir/cerrar negocio

```
PATCH /api/v1/businesses/{id}/status
Auth: Bearer {token}
Rol: ADMIN
```

Alterna el campo `open` entre `true` y `false`. Sin request body.

---

### 2.6 Eliminar negocio

```
DELETE /api/v1/businesses/{id}
Auth: Bearer {token}
Rol: ADMIN
```

**Response 204** (sin body). Soft delete.

---

## 3. Categorías

> Base URL: `/api/v1/businesses/{businessId}/categories`
> Todos los endpoints validan que el usuario pertenece al negocio.

### 3.1 Crear categoría

```
POST /api/v1/businesses/{businessId}/categories
Rol: ADMIN
```

**Request:**
```json
{
  "name": "Bebidas calientes",
  "icon": "☕"
}
```

**Response 201:**
```json
{
  "id": "770e8400-...",
  "name": "Bebidas calientes",
  "icon": "☕",
  "sortOrder": 0,
  "businessId": "660e8400-...",
  "createdAt": "2026-04-29T10:30:00Z"
}
```

**Reglas:**
- SEMILLA: max 3 categorías → 422
- Nombre no puede repetirse en el mismo negocio → 409
- `sortOrder` se asigna automáticamente (max actual + 1)

---

### 3.2 Listar categorías

```
GET /api/v1/businesses/{businessId}/categories
Rol: cualquier usuario autenticado del negocio
```

Retorna array ordenado por `sortOrder`.

---

### 3.3 Actualizar categoría

```
PUT /api/v1/businesses/{businessId}/categories/{id}
Rol: ADMIN
```

---

### 3.4 Eliminar categoría

```
DELETE /api/v1/businesses/{businessId}/categories/{id}
Rol: ADMIN
```

**Restricción:** No se puede eliminar si tiene productos asociados → 422.

---

## 4. Productos

> Base URL: `/api/v1/businesses/{businessId}/products`

### 4.1 Crear producto

```
POST /api/v1/businesses/{businessId}/products
Rol: ADMIN
```

**Request:**
```json
{
  "name": "Cappuccino",
  "price": 8500,
  "currency": "COP",
  "description": "Espresso con leche espumada",
  "ingredients": "café, leche",
  "imageType": "EMOJI",
  "image": "☕",
  "categoryId": "770e8400-..."
}
```

| Campo | Tipo | Validación |
|-------|------|-----------|
| name | string | Requerido |
| price | number | Requerido, > 0 |
| currency | string | Requerido. `COP`, `USD`, `MXN` |
| description | string | Opcional |
| ingredients | string | Opcional, texto libre |
| imageType | string | Requerido. `EMOJI` o `IMAGE` |
| image | string | Requerido. Emoji si `EMOJI`, URL de Cloudinary si `IMAGE` |
| categoryId | UUID | Requerido, debe existir en el negocio |

**Response 201:**
```json
{
  "id": "880e8400-...",
  "name": "Cappuccino",
  "price": 8500,
  "currency": "COP",
  "description": "Espresso con leche espumada",
  "ingredients": "café, leche",
  "imageType": "EMOJI",
  "image": "☕",
  "available": true,
  "categoryId": "770e8400-...",
  "businessId": "660e8400-...",
  "createdAt": "2026-04-29T10:30:00Z"
}
```

**Reglas:**
- SEMILLA: max 25 productos → 422
- La categoría debe pertenecer al mismo negocio → 404

---

### 4.2 Listar productos

```
GET /api/v1/businesses/{businessId}/products
GET /api/v1/businesses/{businessId}/products?categoryId={uuid}
Rol: cualquier usuario del negocio
```

---

### 4.3 Obtener producto

```
GET /api/v1/businesses/{businessId}/products/{id}
```

---

### 4.4 Actualizar producto

```
PUT /api/v1/businesses/{businessId}/products/{id}
Rol: ADMIN
```

Mismos campos que crear.

---

### 4.5 Eliminar producto

```
DELETE /api/v1/businesses/{businessId}/products/{id}
Rol: ADMIN
```

Soft delete. Las órdenes históricas conservan el nombre y precio del producto.

---

### 4.6 Toggle disponibilidad

```
PATCH /api/v1/businesses/{businessId}/products/{id}/toggle
Rol: ADMIN
```

Invierte `available` (true ↔ false). Sin request body. Retorna el producto actualizado.

---

### 4.7 Subir imagen

```
POST /api/v1/businesses/{businessId}/products/upload-image
Content-Type: multipart/form-data
Rol: ADMIN
```

**Request:** `file` (multipart, max 3MB, jpg/png/webp)

**Response 200:**
```json
{
  "imageUrl": "https://res.cloudinary.com/mezo/image/upload/v1/.../product.jpg"
}
```

**Flujo para el frontend:**
1. Subir imagen con este endpoint → obtener `imageUrl`
2. Crear/actualizar producto con `imageType: "IMAGE"` y `image: "{imageUrl}"`

---

## 5. Mesas

> Base URL: `/api/v1/businesses/{businessId}/tables`
> El backend **no gestiona estado de mesas** (libre/ocupada). Eso es lógica del frontend.

### 5.1 Crear mesa

```
POST /api/v1/businesses/{businessId}/tables
Rol: ADMIN
```

**Sin request body.** El número se asigna automáticamente (max actual + 1).

**Response 201:**
```json
{
  "id": "990e8400-...",
  "number": 5,
  "businessId": "660e8400-...",
  "createdAt": "2026-04-29T10:30:00Z"
}
```

**Reglas:**
- SEMILLA: max 4 mesas → 422

---

### 5.2 Listar mesas

```
GET /api/v1/businesses/{businessId}/tables
```

Retorna array ordenado por `number`.

---

### 5.3 Eliminar mesa

```
DELETE /api/v1/businesses/{businessId}/tables/{id}
Rol: ADMIN
```

---

## 6. Órdenes

> Base URL: `/api/v1/businesses/{businessId}/orders`

### 6.1 Crear orden

```
POST /api/v1/businesses/{businessId}/orders
Rol: ADMIN, CASHIER, WAITER
```

**Desde una mesa:**
```json
{
  "tableId": "990e8400-...",
  "paymentMethod": "NEQUI",
  "tip": 2000,
  "lines": [
    { "productId": "880e8400-...", "quantity": 2 },
    { "productId": "880e8401-...", "quantity": 1 }
  ]
}
```

**Directa desde POS (sin mesa):**
```json
{
  "tableId": null,
  "paymentMethod": "CASH",
  "tip": 0,
  "lines": [
    { "productId": "880e8400-...", "quantity": 1 }
  ]
}
```

| Campo | Tipo | Validación |
|-------|------|-----------|
| tableId | UUID | Opcional. `null` = orden directa POS |
| paymentMethod | string | Opcional. `CASH`, `BOLD`, `NEQUI`, `DAVIPLATA`, `TRANSFER` |
| tip | number | Opcional, default 0 |
| lines | array | Requerido, al menos 1 línea |
| lines[].productId | UUID | Requerido, producto debe existir y estar disponible |
| lines[].quantity | int | Requerido, mín 1 |

**Response 201:**
```json
{
  "id": "aa0e8400-...",
  "tableId": "990e8400-...",
  "status": "OPEN",
  "paymentMethod": "NEQUI",
  "tip": 2000,
  "total": 27000,
  "currency": "COP",
  "lines": [
    {
      "id": "line-01-...",
      "productId": "880e8400-...",
      "productName": "Cappuccino",
      "unitPrice": 8500,
      "quantity": 2,
      "subtotal": 17000
    },
    {
      "id": "line-02-...",
      "productId": "880e8401-...",
      "productName": "Brownie",
      "unitPrice": 8000,
      "quantity": 1,
      "subtotal": 8000
    }
  ],
  "createdBy": "550e8400-...",
  "createdAt": "2026-04-29T14:30:00Z"
}
```

**Nota:** `total` incluye la propina. `productName` y `unitPrice` son snapshots (no cambian si el producto se edita después).

---

### 6.2 Agregar productos a orden existente

```
POST /api/v1/businesses/{businessId}/orders/{id}/lines
Rol: ADMIN, CASHIER, WAITER
```

**Request:**
```json
{
  "lines": [
    { "productId": "880e8402-...", "quantity": 1 },
    { "productId": "880e8400-...", "quantity": 1 }
  ]
}
```

**Response 200:** La orden completa actualizada con todas las líneas (incluyendo las nuevas).

**Reglas:**
- Solo funciona en órdenes con status `OPEN` → 422 si no
- Agregar el mismo producto crea líneas separadas (NO suma quantity)
- El total se recalcula automáticamente

---

### 6.3 Eliminar línea de orden

```
DELETE /api/v1/businesses/{businessId}/orders/{id}/lines/{lineId}
Rol: ADMIN, CASHIER, WAITER
```

**Response 200:** La orden actualizada sin la línea eliminada.

Solo en órdenes `OPEN`.

---

### 6.4 Listar órdenes

```
GET /api/v1/businesses/{businessId}/orders
GET /api/v1/businesses/{businessId}/orders?status=OPEN
GET /api/v1/businesses/{businessId}/orders?tableId={uuid}
GET /api/v1/businesses/{businessId}/orders?status=OPEN&tableId={uuid}
Rol: ADMIN, CASHIER, WAITER, KITCHEN
```

---

### 6.5 Obtener orden

```
GET /api/v1/businesses/{businessId}/orders/{id}
Rol: ADMIN, CASHIER, WAITER, KITCHEN
```

---

### 6.6 Cambiar estado de orden

```
PATCH /api/v1/businesses/{businessId}/orders/{id}/status
Rol: ADMIN, CASHIER, KITCHEN
```

**Request:**
```json
{
  "status": "DELIVERED"
}
```

**Estados posibles:** `OPEN` → `PREPARING` → `READY` → `DELIVERED` → `CLOSED` / `CANCELLED`

**Flujo típico:**
1. Cajero/mesero crea orden → `OPEN`
2. Cocina la toma → `PREPARING`
3. Cocina la termina → `READY`
4. Mesero la entrega → `DELIVERED`
5. Cajero cierra la venta → `CLOSED` (via endpoint de ventas)

---

## 7. Ventas

> Base URL: `/api/v1/businesses/{businessId}/sales`

### 7.1 Cerrar venta

```
POST /api/v1/businesses/{businessId}/sales
Rol: ADMIN, CASHIER
```

Este endpoint soporta **dos flujos:**

#### Flujo A — Cerrar orden existente (viene de mesa)

```json
{
  "orderId": "aa0e8400-..."
}
```

La orden debe tener status `DELIVERED`. Se cierra automáticamente.

#### Flujo B — Venta directa POS (sin orden previa)

```json
{
  "paymentMethod": "CASH",
  "tip": 0,
  "lines": [
    { "productId": "880e8400-...", "quantity": 2 },
    { "productId": "880e8401-...", "quantity": 1 }
  ]
}
```

Crea la orden Y la venta en un solo request. No requiere doble llamado.

**Response 201 (ambos flujos):**
```json
{
  "id": "bb0e8400-...",
  "orderId": "aa0e8400-...",
  "total": 27000,
  "tip": 2000,
  "currency": "COP",
  "paymentMethod": "NEQUI",
  "tableId": "990e8400-...",
  "closedBy": "550e8400-...",
  "createdAt": "2026-04-29T15:15:00Z"
}
```

**Cuándo usar cada flujo:**
- **Mesa pide cuenta** → el cajero cierra con `orderId` (Flujo A)
- **Cliente paga directo en caja** → el cajero envía `lines` sin `orderId` (Flujo B)

---

### 7.2 Listar ventas

```
GET /api/v1/businesses/{businessId}/sales
GET /api/v1/businesses/{businessId}/sales?from=2026-04-01&to=2026-04-29
Rol: ADMIN, CASHIER
```

---

### 7.3 Obtener venta

```
GET /api/v1/businesses/{businessId}/sales/{id}
Rol: ADMIN, CASHIER
```

---

## 8. Reportes

> Base URL: `/api/v1/businesses/{businessId}/reports`
> Requiere plan **PRO** o **ELITE** → 422 si plan SEMILLA
> Rol: solo ADMIN

Todos los reportes usan el query param `?time=` en vez de `from/to`:

| `time` | Periodo |
|--------|---------|
| `DAY` | Hoy (00:00 → ahora) |
| `WEEK` | Últimos 7 días |
| `MONTH` | Últimas 4 semanas |
| `QUARTER` | Últimos 3 meses |
| `YEAR` | Últimos 12 meses |

### 8.1 Total vendido

```
GET /api/v1/businesses/{businessId}/reports/sales?time=MONTH
```

**Response 200:**
```json
{
  "time": "MONTH",
  "from": "2026-03-29",
  "to": "2026-04-29",
  "totalSales": 2450000,
  "totalTips": 180000,
  "currency": "COP",
  "salesCount": 342
}
```

---

### 8.2 Cantidad de ventas

```
GET /api/v1/businesses/{businessId}/reports/count?time=WEEK
```

**Response 200:**
```json
{
  "time": "WEEK",
  "from": "2026-04-22",
  "to": "2026-04-29",
  "salesCount": 89
}
```

---

### 8.3 Mejor hora

```
GET /api/v1/businesses/{businessId}/reports/peak-hour?time=MONTH
```

**Response 200:**
```json
{
  "time": "MONTH",
  "from": "2026-03-29",
  "to": "2026-04-29",
  "hour": 12,
  "label": "12:00 - 13:00",
  "salesCount": 89,
  "total": 680000,
  "currency": "COP"
}
```

---

### 8.4 Producto más vendido

```
GET /api/v1/businesses/{businessId}/reports/top-products?time=WEEK&limit=5
```

| Param | Default |
|-------|---------|
| limit | 10 |

**Response 200:**
```json
{
  "time": "WEEK",
  "from": "2026-04-22",
  "to": "2026-04-29",
  "products": [
    { "productId": "...", "name": "Cappuccino", "totalSold": 156, "revenue": 1326000 },
    { "productId": "...", "name": "Brownie", "totalSold": 98, "revenue": 784000 }
  ]
}
```

---

### 8.5 Método de pago más usado

```
GET /api/v1/businesses/{businessId}/reports/payment-methods?time=QUARTER
```

**Response 200:**
```json
{
  "time": "QUARTER",
  "from": "2026-01-29",
  "to": "2026-04-29",
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

## 9. Analítica (Gráficas)

> `GET /api/v1/businesses/{businessId}/analytics?time=`
> Requiere plan **PRO** o **ELITE**
> Rol: solo ADMIN

Retorna datos en serie de tiempo para alimentar gráficas:

| `time` | Agrupación | Puntos |
|--------|-----------|--------|
| `DAY` | por hora | ~24 |
| `WEEK` | por día | 7 |
| `MONTH` | por semana | 4 |
| `QUARTER` | por mes | 3 |
| `YEAR` | por mes | 12 |

### Ejemplo: ventas del día por hora

```
GET /api/v1/businesses/{businessId}/analytics?time=DAY
```

```json
{
  "time": "DAY",
  "currency": "COP",
  "data": [
    { "label": "7:00", "total": 85000, "count": 12 },
    { "label": "8:00", "total": 120000, "count": 18 },
    { "label": "12:00", "total": 180000, "count": 25 },
    { "label": "13:00", "total": 150000, "count": 20 }
  ]
}
```

### Ejemplo: ventas de la semana por día

```
GET /api/v1/businesses/{businessId}/analytics?time=WEEK
```

```json
{
  "time": "WEEK",
  "currency": "COP",
  "data": [
    { "label": "Lun", "date": "2026-04-23", "total": 350000, "count": 48 },
    { "label": "Mar", "date": "2026-04-24", "total": 420000, "count": 56 },
    { "label": "Mié", "date": "2026-04-25", "total": 380000, "count": 51 }
  ]
}
```

### Ejemplo: ventas del año por mes

```
GET /api/v1/businesses/{businessId}/analytics?time=YEAR
```

```json
{
  "time": "YEAR",
  "currency": "COP",
  "data": [
    { "label": "May 2025", "total": 3200000, "count": 410 },
    { "label": "Jun 2025", "total": 2900000, "count": 380 },
    { "label": "Abr 2026", "total": 2800000, "count": 350 }
  ]
}
```

---

## 10. Equipo

> Base URL: `/api/v1/businesses/{businessId}/team`
> Todos los endpoints: Rol ADMIN

### 10.1 Invitar miembro

```
POST /api/v1/businesses/{businessId}/team/invite
```

**Request:**
```json
{
  "email": "mesero@example.com",
  "role": "WAITER"
}
```

| Rol | Descripción |
|-----|------------|
| `ADMIN` | Acceso total |
| `CASHIER` | Crear órdenes + cerrar ventas |
| `WAITER` | Crear órdenes |
| `KITCHEN` | Ver órdenes (solo lectura) |

**Dos escenarios de respuesta:**

**Si el usuario ya existe y está verificado:**
```json
{
  "status": "ACTIVE",
  "teamMember": {
    "userId": "cc0e8400-...",
    "email": "mesero@example.com",
    "role": "WAITER",
    "businessId": "660e8400-...",
    "invitedBy": "550e8400-...",
    "createdAt": "2026-04-29T10:30:00Z"
  }
}
```

**Si el usuario no existe:**
```json
{
  "status": "PENDING",
  "invitation": {
    "id": "dd0e8400-...",
    "email": "mesero@example.com",
    "role": "WAITER",
    "businessId": "660e8400-...",
    "status": "PENDING",
    "createdAt": "2026-04-29T10:30:00Z"
  }
}
```

El usuario invitado recibe un email. Cuando se registra y verifica su OTP, automáticamente se vincula al negocio.

**Reglas:**
- SEMILLA: max 3 empleados → 422
- No se puede invitar al mismo email dos veces → 409

---

### 10.2 Listar equipo

```
GET /api/v1/businesses/{businessId}/team
```

```json
[
  {
    "userId": "cc0e8400-...",
    "email": "mesero@example.com",
    "role": "WAITER",
    "businessId": "660e8400-...",
    "invitedBy": "550e8400-...",
    "createdAt": "2026-04-29T10:30:00Z"
  }
]
```

---

### 10.3 Cambiar rol

```
PUT /api/v1/businesses/{businessId}/team/{userId}/role
```

**Request:**
```json
{
  "role": "CASHIER"
}
```

**Restricción:** No puedes cambiar tu propio rol → 422.

---

### 10.4 Eliminar miembro

```
DELETE /api/v1/businesses/{businessId}/team/{userId}
```

**Restricciones:**
- No puedes eliminarte a ti mismo → 422
- No puedes eliminar al owner del negocio → 422

---

## 11. Planes y Suscripciones

### 11.1 Listar planes (público)

```
GET /api/v1/plans
Auth: no requerida
```

```json
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

> `-1` = ilimitado

---

### 11.2 Plan actual

```
GET /api/v1/plans/current
Rol: ADMIN
```

```json
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

**Uso en frontend:** Verificar `isTrialActive` y `planExpiresAt` para mostrar banner de "X días restantes de prueba".

---

### 11.3 Suscribirse / Cambiar plan

```
POST /api/v1/plans/subscribe
Rol: ADMIN
```

**Request:**
```json
{
  "planType": "ELITE"
}
```

**Response 200:**
```json
{
  "paymentUrl": "https://checkout.wompi.co/l/abc123",
  "planType": "ELITE",
  "price": 199900,
  "currency": "COP"
}
```

**Flujo para el frontend:**
1. Llamar este endpoint → obtener `paymentUrl`
2. Redirigir al usuario a `paymentUrl` (checkout de Wompi)
3. El usuario paga con PSE, Nequi, tarjeta, etc.
4. Wompi redirige al usuario de vuelta a la app (URL configurada)
5. Wompi envía webhook al backend → el plan se actualiza automáticamente
6. En la pantalla de "éxito", hacer `GET /plans/current` para confirmar el nuevo plan

---

## Referencia Rápida

### Todos los endpoints

| Método | URL | Rol | Descripción |
|--------|-----|-----|-------------|
| POST | `/auth/register` | Público | Registrar |
| POST | `/auth/verify-otp` | Público | Verificar OTP |
| POST | `/auth/resend-otp` | Público | Reenviar OTP |
| POST | `/auth/login` | Público | Login |
| POST | `/auth/refresh` | Público | Refresh token |
| | | | |
| POST | `/businesses` | ADMIN | Crear negocio |
| GET | `/businesses` | Auth | Listar mis negocios |
| GET | `/businesses/{id}` | Auth | Obtener negocio |
| PUT | `/businesses/{id}` | ADMIN | Actualizar negocio |
| PATCH | `/businesses/{id}/status` | ADMIN | Abrir/cerrar |
| DELETE | `/businesses/{id}` | ADMIN | Eliminar negocio |
| | | | |
| POST | `/businesses/{bId}/categories` | ADMIN | Crear categoría |
| GET | `/businesses/{bId}/categories` | Auth | Listar categorías |
| PUT | `/businesses/{bId}/categories/{id}` | ADMIN | Actualizar |
| DELETE | `/businesses/{bId}/categories/{id}` | ADMIN | Eliminar |
| | | | |
| POST | `/businesses/{bId}/products` | ADMIN | Crear producto |
| GET | `/businesses/{bId}/products` | Auth | Listar (?categoryId=) |
| GET | `/businesses/{bId}/products/{id}` | Auth | Obtener |
| PUT | `/businesses/{bId}/products/{id}` | ADMIN | Actualizar |
| DELETE | `/businesses/{bId}/products/{id}` | ADMIN | Eliminar |
| PATCH | `/businesses/{bId}/products/{id}/toggle` | ADMIN | Toggle disponibilidad |
| POST | `/businesses/{bId}/products/upload-image` | ADMIN | Subir imagen |
| | | | |
| POST | `/businesses/{bId}/tables` | ADMIN | Crear mesa (sin body) |
| GET | `/businesses/{bId}/tables` | Auth | Listar mesas |
| DELETE | `/businesses/{bId}/tables/{id}` | ADMIN | Eliminar mesa |
| | | | |
| POST | `/businesses/{bId}/orders` | ADM/CAJ/MES | Crear orden |
| GET | `/businesses/{bId}/orders` | ADM/CAJ/MES/COC | Listar (?status=&tableId=) |
| GET | `/businesses/{bId}/orders/{id}` | ADM/CAJ/MES/COC | Obtener |
| POST | `/businesses/{bId}/orders/{id}/lines` | ADM/CAJ/MES | Agregar líneas |
| DELETE | `/businesses/{bId}/orders/{id}/lines/{lId}` | ADM/CAJ/MES | Eliminar línea |
| PATCH | `/businesses/{bId}/orders/{id}/status` | ADM/CAJ/COC | Cambiar estado |
| | | | |
| POST | `/businesses/{bId}/sales` | ADM/CAJ | Cerrar venta |
| GET | `/businesses/{bId}/sales` | ADM/CAJ | Listar (?from=&to=) |
| GET | `/businesses/{bId}/sales/{id}` | ADM/CAJ | Obtener |
| | | | |
| GET | `/businesses/{bId}/reports/sales` | ADMIN | Total vendido (?time=) |
| GET | `/businesses/{bId}/reports/count` | ADMIN | Cantidad ventas (?time=) |
| GET | `/businesses/{bId}/reports/peak-hour` | ADMIN | Mejor hora (?time=) |
| GET | `/businesses/{bId}/reports/top-products` | ADMIN | Top productos (?time=&limit=) |
| GET | `/businesses/{bId}/reports/payment-methods` | ADMIN | Métodos de pago (?time=) |
| | | | |
| GET | `/businesses/{bId}/analytics` | ADMIN | Gráficas (?time=) |
| | | | |
| POST | `/businesses/{bId}/team/invite` | ADMIN | Invitar miembro |
| GET | `/businesses/{bId}/team` | ADMIN | Listar equipo |
| PUT | `/businesses/{bId}/team/{uId}/role` | ADMIN | Cambiar rol |
| DELETE | `/businesses/{bId}/team/{uId}` | ADMIN | Eliminar miembro |
| | | | |
| GET | `/plans` | Público | Listar planes |
| GET | `/plans/current` | ADMIN | Plan actual |
| POST | `/plans/subscribe` | ADMIN | Suscribirse |

> **ADM** = ADMIN, **CAJ** = CASHIER, **MES** = WAITER, **COC** = KITCHEN
> Todos los URLs bajo `/businesses/{bId}/` validan que el usuario pertenezca al negocio (403 si no).

### Límites por plan

| Recurso | SEMILLA | PRO | ELITE |
|---------|---------|-----|-------|
| Negocios | 1 | 1 | Ilimitado |
| Mesas / negocio | 4 | Ilimitado | Ilimitado |
| Productos / negocio | 25 | Ilimitado | Ilimitado |
| Categorías / negocio | 3 | Ilimitado | Ilimitado |
| Empleados / negocio | 3 | Ilimitado | Ilimitado |
| Reportes | No | Si | Si |
| Analítica | No | Si | Si |
| Precio/mes | $39.900 | $99.900 | $199.900 |
| Trial | — | 30 días gratis | — |

### Matriz de permisos por rol

| Recurso | ADMIN | CASHIER | WAITER | KITCHEN |
|---------|-------|---------|--------|---------|
| Negocio (CRUD) | ✅ | ❌ | ❌ | ❌ |
| Productos (CRUD) | ✅ | ❌ | ❌ | ❌ |
| Categorías (CRUD) | ✅ | ❌ | ❌ | ❌ |
| Mesas (CRUD) | ✅ | ❌ | ❌ | ❌ |
| Órdenes (crear) | ✅ | ✅ | ✅ | ❌ |
| Órdenes (ver) | ✅ | ✅ | ✅ | ✅ |
| Órdenes (cambiar estado) | ✅ | ✅ | ❌ | ✅ |
| Ventas (cerrar) | ✅ | ✅ | ❌ | ❌ |
| Ventas (ver) | ✅ | ✅ | ❌ | ❌ |
| Reportes | ✅ | ❌ | ❌ | ❌ |
| Analítica | ✅ | ❌ | ❌ | ❌ |
| Equipo | ✅ | ❌ | ❌ | ❌ |
| Planes | ✅ | ❌ | ❌ | ❌ |
