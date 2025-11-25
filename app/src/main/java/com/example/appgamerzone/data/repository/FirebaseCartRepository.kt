package com.example.appgamerzone.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appgamerzone.data.model.CartItem
import com.example.appgamerzone.data.model.Order
import com.example.appgamerzone.data.model.OrderStatus
import com.example.appgamerzone.data.model.User
import kotlinx.coroutines.tasks.await

class FirebaseCartRepository(
    private val productRepository: FirebaseProductRepository
) {
    companion object {
        private const val TAG = "FirebaseCartRepository"
    }

    private val db = FirebaseFirestore.getInstance()
    private val cartsCollection = db.collection("carts")
    private val ordersCollection = db.collection("orders")

    suspend fun getCart(userId: String): Result<List<CartItem>> {
        return try {
            Log.d(TAG, "getCart - userId: $userId")
            val document = cartsCollection.document(userId).get().await()

            if (!document.exists()) {
                Log.d(TAG, "getCart - No existe documento de carrito para userId: $userId")
                return Result.success(emptyList())
            }

            @Suppress("UNCHECKED_CAST")
            val itemsList = (document.data?.get("items") as? List<Map<String, Any?>>)?.map {
                CartItem.fromMap(it)
            } ?: emptyList()

            Log.d(TAG, "getCart - Items encontrados: ${itemsList.size}")
            Result.success(itemsList)
        } catch (e: Exception) {
            Log.e(TAG, "getCart - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun addToCart(userId: String, productId: String, quantity: Int = 1): Result<List<CartItem>> {
        return try {
            Log.d(TAG, "addToCart - userId: $userId, productId: $productId, quantity: $quantity")

            // Verificar stock disponible
            val productResult = productRepository.getProductById(productId)
            val product = productResult.getOrThrow()

            Log.d(TAG, "addToCart - Producto: ${product.name}, Stock: ${product.stock}")

            if (product.stock < quantity) {
                Log.w(TAG, "addToCart - Stock insuficiente")
                return Result.failure(IllegalStateException("Stock insuficiente. Disponible: ${product.stock}"))
            }

            // Obtener carrito actual
            val currentCart = getCart(userId).getOrElse { emptyList() }.toMutableList()
            Log.d(TAG, "addToCart - Carrito actual tiene ${currentCart.size} items")

            // Verificar si el producto ya está en el carrito
            val existingItemIndex = currentCart.indexOfFirst { it.productId == productId }

            if (existingItemIndex != -1) {
                // Actualizar cantidad
                val existingItem = currentCart[existingItemIndex]
                val newQuantity = existingItem.quantity + quantity

                Log.d(TAG, "addToCart - Producto ya existe, nueva cantidad: $newQuantity")

                if (newQuantity > product.stock) {
                    return Result.failure(
                        IllegalStateException("No se puede agregar más. Stock disponible: ${product.stock}")
                    )
                }

                currentCart[existingItemIndex] = existingItem.copy(
                    quantity = newQuantity,
                    availableStock = product.stock
                )
            } else {
                // Agregar nuevo item
                Log.d(TAG, "addToCart - Agregando nuevo item al carrito")
                val newItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    productImage = product.imageUrl,
                    quantity = quantity,
                    unitPrice = product.price,
                    availableStock = product.stock
                )
                currentCart.add(newItem)
            }

            // Guardar carrito
            saveCart(userId, currentCart)
            Log.d(TAG, "addToCart - Carrito guardado con ${currentCart.size} items")

            Result.success(currentCart)
        } catch (e: Exception) {
            Log.e(TAG, "addToCart - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateCartItemQuantity(
        userId: String,
        productId: String,
        newQuantity: Int
    ): Result<List<CartItem>> {
        return try {
            if (newQuantity < 1) {
                return removeFromCart(userId, productId)
            }

            // Verificar stock
            val productResult = productRepository.getProductById(productId)
            val product = productResult.getOrThrow()

            if (product.stock < newQuantity) {
                return Result.failure(
                    IllegalStateException("Stock insuficiente. Disponible: ${product.stock}")
                )
            }

            val currentCart = getCart(userId).getOrElse { emptyList() }.toMutableList()
            val itemIndex = currentCart.indexOfFirst { it.productId == productId }

            if (itemIndex == -1) {
                return Result.failure(IllegalStateException("Producto no encontrado en el carrito"))
            }

            currentCart[itemIndex] = currentCart[itemIndex].copy(
                quantity = newQuantity,
                availableStock = product.stock
            )

            saveCart(userId, currentCart)
            Result.success(currentCart)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(userId: String, productId: String): Result<List<CartItem>> {
        return try {
            val currentCart = getCart(userId).getOrElse { emptyList() }.toMutableList()
            currentCart.removeIf { it.productId == productId }

            saveCart(userId, currentCart)
            Result.success(currentCart)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart(userId: String): Result<Unit> {
        return try {
            cartsCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkout(
        userId: String,
        user: User,
        discountCode: String? = null,
        discountPercentage: Double = 0.0
    ): Result<Order> {
        return try {
            val cartItems = getCart(userId).getOrThrow()

            if (cartItems.isEmpty()) {
                return Result.failure(IllegalStateException("El carrito está vacío"))
            }

            // Validar stock de todos los productos
            for (item in cartItems) {
                val product = productRepository.getProductById(item.productId).getOrThrow()
                if (product.stock < item.quantity) {
                    return Result.failure(
                        IllegalStateException("Stock insuficiente para ${product.name}. Disponible: ${product.stock}")
                    )
                }
            }

            // Calcular totales usando el porcentaje de descuento
            val subtotal = cartItems.sumOf { it.subtotal }
            val discount = if (discountPercentage > 0) {
                subtotal * (discountPercentage / 100.0)
            } else {
                0.0
            }
            val total = subtotal - discount

            Log.d(TAG, "checkout - Subtotal: $subtotal, Descuento: $discount ($discountPercentage%), Total: $total")

            // Crear orden
            val orderRef = ordersCollection.document()
            val order = Order(
                id = orderRef.id,
                userId = userId,
                userName = user.fullName,
                userEmail = user.email,
                items = cartItems,
                subtotal = subtotal,
                discount = discount,
                total = total,
                date = System.currentTimeMillis(),
                status = OrderStatus.COMPLETED,
                discountCode = discountCode
            )

            // Guardar orden
            orderRef.set(order.toMap()).await()

            // Descontar stock
            for (item in cartItems) {
                productRepository.decreaseStock(item.productId, item.quantity)
            }

            // Limpiar carrito
            clearCart(userId)

            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserOrders(userId: String): Result<List<Order>> {
        return try {
            val querySnapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val orders = querySnapshot.documents.mapNotNull { document ->
                document.data?.let { Order.fromMap(document.id, it) }
            }.sortedByDescending { it.date }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveCart(userId: String, items: List<CartItem>) {
        try {
            Log.d(TAG, "saveCart - userId: $userId, items: ${items.size}")
            val cartData = hashMapOf(
                "userId" to userId,
                "items" to items.map { it.toMap() },
                "updatedAt" to System.currentTimeMillis()
            )
            cartsCollection.document(userId).set(cartData).await()
            Log.d(TAG, "saveCart - Carrito guardado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "saveCart - Error al guardar: ${e.message}", e)
            throw e
        }
    }

    private fun calculateDiscount(subtotal: Double, discountCode: String?): Double {
        return when (discountCode?.uppercase()) {
            "GAMER10" -> subtotal * 0.10
            "GAMER20" -> subtotal * 0.20
            "DUOC50" -> subtotal * 0.50
            else -> 0.0
        }
    }

    fun validateDiscountCode(code: String): Boolean {
        return code.uppercase() in listOf("GAMER10", "GAMER20", "DUOC50")
    }
}


