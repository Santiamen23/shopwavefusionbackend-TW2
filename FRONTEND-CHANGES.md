# Cambios necesarios en el frontend (proyecto shopwave-frontend)

## 1. DELETE /api/admin/products/{id}/delete

El backend ahora responde con códigos distintos según el caso:

| Código | Significado | Acción recomendada |
|---|---|---|
| 200 OK | Producto soft-deleted | Toast "Producto eliminado" |
| 409 Conflict | Producto en N carritos activos | Mostrar `response.message` ("El producto está en 3 carrito(s) activo(s)...") y sugerir vaciar esos carritos |
| 404 Not Found | Ya estaba soft-deleted o nunca existió | Toast "El producto ya no existe" |
| 500 | Error inesperado | Mostrar mensaje genérico |

Ejemplo de manejo (pseudocódigo):

```js
try {
  await api.delete(`/admin/products/${id}/delete`);
  toast.success("Producto eliminado");
  refresh();
} catch (err) {
  if (err.response?.status === 409) {
    toast.warning(err.response.data.message);
  } else if (err.response?.status === 404) {
    toast.info("El producto ya no existe");
    refresh();
  } else {
    toast.error("No se pudo eliminar el producto");
  }
}
```

## 2. GET /api/cart

El backend ya NO devuelve cart_items con `product: null`. Pero por defensa adicional en el frontend:

```js
const visibleItems = (cart.cartItems || []).filter(item => item.product != null);
```

Si llega un item con `product: null` (por ejemplo, entre deploys), no lo renderices y fuerza un refetch.

## 3. DELETE /api/cart/items/{id} — no silenciar errores

Antes:
```js
try { await api.delete(`/cart/items/${id}`); } catch (e) {}
```

Después:
```js
try {
  await api.delete(`/cart/items/${id}`);
  removeItemFromState(id);
} catch (err) {
  // Cualquier fallo (404, 500, red) ⇒ refetch el carrito entero
  await refetchCart();
}
```

Razón: si el backend rechaza el delete (item ya borrado, size inválido, etc.), el item
sigue existiendo en el state del frontend hasta que se reconcilie con el backend.

## 4. POST /api/cart/add

El backend ahora rechaza con 400 si:
- `size` es null o vacío
- `quantity <= 0`

Mostrar el `message` de la respuesta en lugar de un error genérico.

## 5. Nuevos endpoints admin (opcional, recomendado para panel admin)

```js
// Listar cart_items huérfanos (product borrado o null)
GET /api/admin/cart-items/orphaned

// Borrar un cart_item huérfano (no requiere ser dueño)
DELETE /api/admin/cart-items/{cartItemId}
```

Útil para una pantalla de "Limpieza" en el panel de admin.
