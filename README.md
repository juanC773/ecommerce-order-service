# ecommerce-order-service

Order Service para sistema de ecommerce. Gestiona órdenes y carritos de compra.

## Características

- Spring Boot 2.5.7 con Java 11
- Base de datos: PostgreSQL
- Service Discovery: Eureka Client
- Circuit Breaker: Resilience4j para tolerancia a fallos
- Actuator para health checks
- Soft Delete: Las órdenes se marcan como inactivas en lugar de eliminarse físicamente
- Gestión de estados de orden

## Endpoints

### Cart API
Prefijo: `/order-service`

```
GET    /api/carts           - Listar todos los carritos
GET    /api/carts/{cartId}  - Obtener carrito por ID
POST   /api/carts           - Crear carrito
PUT    /api/carts/{cartId}  - Actualizar carrito por ID
DELETE /api/carts/{cartId}  - Eliminar carrito
```

**Ejemplo de payload para crear carrito:**
```json
{
  "cartId": 1,
  "userId": 1
}
```

### Order API
Prefijo: `/order-service`

```
GET    /api/orders                    - Listar todas las órdenes activas
GET    /api/orders/{orderId}         - Obtener orden por ID
POST   /api/orders                   - Crear orden
PUT    /api/orders/{orderId}         - Actualizar orden por ID
PATCH  /api/orders/{orderId}/status  - Actualizar estado de orden
DELETE /api/orders/{orderId}         - Eliminar orden (soft delete)
```

**Ejemplo de payload para crear orden:**
```json
{
  "orderDate": "10-06-2025__13:12:22:606444",
  "orderDesc": "init",
  "orderFee": 5000.0,
  "cart": {
    "cartId": 2,
    "userId": 2
  }
}
```

## Testing

### Unit Tests (7+)
- OrderServiceImplTest: Tests de lógica de negocio de órdenes
- CartServiceImplTest: Tests de lógica de negocio de carritos
- OrderMappingHelperTest: Tests de mapeo de entidades de orden
- CartMappingHelperTest: Tests de mapeo de entidades de carrito

### Integration Tests (2+)
- OrderResourceIntegrationTest: Tests de integración de endpoints REST de órdenes
- CartResourceIntegrationTest: Tests de integración de endpoints REST de carritos

**Total: 7+ tests - Todos pasando**

```bash
./mvnw test
```

## Ejecutar

```bash
# Opción 1: Directamente
./mvnw spring-boot:run

# Opción 2: Compilar y ejecutar
./mvnw clean package
java -jar target/order-service-v0.1.0.jar
```

Service corre en: `http://localhost:8084/order-service`

## Configuración

### Circuit Breaker (Resilience4j)

El servicio está configurado con circuit breaker para tolerancia a fallos:

- Failure rate threshold: 50%
- Minimum number of calls: 5
- Sliding window size: 10
- Wait duration in open state: 5s
- Sliding window type: COUNT_BASED

### Service Discovery

El servicio se registra automáticamente en Eureka Server con el nombre `ORDER-SERVICE`.

### Health Checks

El servicio expone endpoints de health check a través de Spring Boot Actuator:

```
GET /order-service/actuator/health
```

## Funcionalidades Implementadas

- Gestión completa de carritos de compra (CRUD)
- Gestión completa de órdenes (CRUD)
- Soft Delete: Las órdenes se marcan como inactivas en lugar de eliminarse físicamente
- Actualización de estado de orden mediante PATCH
- Validaciones de campos requeridos
- Manejo de excepciones personalizado
- Circuit breaker para resiliencia
- Integración con Service Discovery (Eureka)
- Filtrado de órdenes activas en consultas

## Notas Importantes

### Carritos

- Los carritos están asociados a un usuario mediante `userId`
- Al crear un carrito, se valida que el usuario exista
- No se permite cambiar el usuario de un carrito existente (por eso no hay PUT en `/api/carts`)

### Órdenes

- Las órdenes están asociadas a un carrito
- Solo se muestran órdenes activas en las consultas (campo `isActive = true`)
- El soft delete marca la orden como inactiva pero no la elimina físicamente
- El estado de la orden se puede actualizar mediante el endpoint PATCH

## Estados de Orden

Los estados posibles de una orden pueden ser gestionados mediante el endpoint PATCH `/api/orders/{orderId}/status`. El servicio actualiza el estado de la orden según la lógica de negocio implementada.

## Comunicación con Otros Servicios

El Order Service puede comunicarse con otros microservicios a través del API Gateway:

- **User Service**: Para validar que el usuario existe al crear carritos
- **Product Service**: Para validar productos en órdenes (si se implementa)

Todas las comunicaciones se realizan a través del API Gateway y el Service Discovery (Eureka).
