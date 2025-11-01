# CART-api

prefijo `/order-service`

Obtener todos los carritos

GET `/api/carts` 

Funciona bien

Obtener un carrito por id

GET `/api/carts/{cartId}` 

Crear carrito

POST `/api/carts` 

Maneja secundariamente las ordenenes lo cual esta mal, recibe el id como parametro entonces llega a sobreescribir, no tiene en cuenta si el usuario al que le crea el carrito existe realmente

Editar carrito

PUT `/api/carts` 

No se revisa, porque el carro solo tiene el id del usuario y no tiene sentido que se le cambie el carro de un usuario a otro, se elimina este endpoint

Eliminar carrito

DELETE `/api/carts`

Funciona bien

ejemplo de payload

```json
{
  "cartId": 1,
  "userId": 1
}
```

# Order API

Obtener todas las ordenes

GET `api/orders`

funciona bien

Obtener orden por id

GET `api/orders/{orderId}`

Crear orden

POST `api/orders`

✅ CORREGIDO: Ya no recibe el id, se establece como null para evitar sobreescribir. Valida que el cart exista antes de crear la orden.

Editar orden por id

PUT `api/orders/{orderId}`

✅ CORREGIDO: Ahora preserva correctamente el cart asociado y evita referencias circulares usando `mapForUpdate`.

Actualizar estado de orden

PATCH `api/orders/{orderId}/status`

Permite actualizar el estado de la orden siguiendo la secuencia: CREATED → ORDERED → IN_PAYMENT

Eliminar orden

DELETE `api/orders/{orderId}`

✅ CORREGIDO: Implementa soft delete (desactiva la orden en lugar de borrarla físicamente). No permite eliminar órdenes que ya están en estado IN_PAYMENT.

Ejemplo de payload

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

## Mejoras implementadas

- ✅ Soft delete con campo `isActive`
- ✅ Gestión de estados de orden (`OrderStatus`: CREATED, ORDERED, IN_PAYMENT)
- ✅ Validación de existencia de cart antes de crear orden
- ✅ Preservación correcta de referencias en actualizaciones
- ✅ Método `updateStatus()` para transiciones de estado controladas
