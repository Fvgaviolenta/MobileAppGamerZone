package com.example.appgamerzone.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appgamerzone.data.model.CartItem
import com.example.appgamerzone.data.model.CartItemSimple
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

    /**
     * Obtiene el carrito del usuario
     * 1. Lee los IDs y cantidades de Firebase
     * 2. Busca la información completa de cada producto
     * 3. Construye los CartItems completos
     */
    suspend fun getCart(userId: String): Result<List<CartItem>> {
        return try {
            Log.d(TAG, "=== INICIO getCart ===")
            Log.d(TAG, "getCart - userId: $userId")

            val document = cartsCollection.document(userId).get().await()

            if (!document.exists()) {
                Log.d(TAG, "getCart - No existe documento de carrito")
                return Result.success(emptyList())
            }

            // Obtener la lista de items simples (solo ID y cantidad)
            @Suppress("UNCHECKED_CAST")
            val itemsSimple = (document.data?.get("items") as? List<Map<String, Any?>>)?.map {
                CartItemSimple.fromMap(it)
            } ?: emptyList()

            Log.d(TAG, "getCart - Items simples encontrados: ${itemsSimple.size}")

            // Construir CartItems completos buscando info de productos
            val cartItems = mutableListOf<CartItem>()

            for (simpleItem in itemsSimple) {
                Log.d(TAG, "getCart - Buscando producto: ${simpleItem.productId}")

                val productResult = productRepository.getProductById(simpleItem.productId)

                if (productResult.isSuccess) {
                    val product = productResult.getOrNull()!!
                    val cartItem = CartItem(
                        productId = product.id,
                        productName = product.name,
                        productImage = product.imageUrl,
                        quantity = simpleItem.quantity,
                        unitPrice = product.price,
                        availableStock = product.stock
                    )
                    cartItems.add(cartItem)
                    Log.d(TAG, "getCart - Producto agregado: ${product.name}, qty: ${simpleItem.quantity}")
                } else {
                    Log.e(TAG, "getCart - Error al obtener producto ${simpleItem.productId}")
                }
            }

            Log.d(TAG, "getCart - CartItems completos: ${cartItems.size}")
            Log.d(TAG, "=== FIN getCart ===")

            Result.success(cartItems)
        } catch (e: Exception) {
            Log.e(TAG, "getCart - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Agrega un producto al carrito
     * 1. Verifica stock disponible
     * 2. Obtiene carrito simple (solo IDs)
     * 3. Agrega/actualiza el producto
     * 4. Guarda solo IDs y cantidades en Firebase
     */
    suspend fun addToCart(userId: String, productId: String, quantity: Int = 1): Result<List<CartItem>> {
        return try {
            Log.d(TAG, "=== INICIO addToCart ===")
            Log.d(TAG, "addToCart - userId: $userId, productId: $productId, quantity: $quantity")

            // Verificar que el producto existe y tiene stock
            val productResult = productRepository.getProductById(productId)
            val product = productResult.getOrThrow()

            Log.d(TAG, "addToCart - Producto encontrado: ${product.name}, Stock: ${product.stock}")

            if (product.stock < quantity) {
                Log.w(TAG, "addToCart - Stock insuficiente")
                return Result.failure(IllegalStateException("Stock insuficiente. Disponible: ${product.stock}"))
            }

            // Obtener carrito simple actual
            val currentSimpleCart = getSimpleCart(userId)
            Log.d(TAG, "addToCart - Carrito simple actual: ${currentSimpleCart.size} items")

            // Buscar si el producto ya está en el carrito
            val existingIndex = currentSimpleCart.indexOfFirst { it.productId == productId }

            val updatedSimpleCart = currentSimpleCart.toMutableList()

            if (existingIndex != -1) {
                // Producto ya existe, actualizar cantidad
                val existing = currentSimpleCart[existingIndex]
                val newQuantity = existing.quantity + quantity

                Log.d(TAG, "addToCart - Producto existe, actualizando cantidad: $newQuantity")

                if (newQuantity > product.stock) {
                    return Result.failure(
                        IllegalStateException("No se puede agregar más. Stock disponible: ${product.stock}")
                    )
                }

                updatedSimpleCart[existingIndex] = CartItemSimple(productId, newQuantity)
            } else {
                // Producto nuevo, agregar
                Log.d(TAG, "addToCart - Producto nuevo, agregando")
                updatedSimpleCart.add(CartItemSimple(productId, quantity))
            }

            // Guardar carrito simple en Firebase
            saveSimpleCart(userId, updatedSimpleCart)
            Log.d(TAG, "addToCart - Carrito guardado: ${updatedSimpleCart.size} items")

            // Retornar CartItems completos
            val result = getCart(userId)
            Log.d(TAG, "=== FIN addToCart ===")

            result
        } catch (e: Exception) {
            Log.e(TAG, "addToCart - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    suspend fun updateCartItemQuantity(
        userId: String,
        productId: String,
        newQuantity: Int
    ): Result<List<CartItem>> {
        return try {
            Log.d(TAG, "updateCartItemQuantity - productId: $productId, newQuantity: $newQuantity")

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

            // Actualizar carrito simple
            val currentSimpleCart = getSimpleCart(userId).toMutableList()
            val itemIndex = currentSimpleCart.indexOfFirst { it.productId == productId }

            if (itemIndex == -1) {
                return Result.failure(IllegalStateException("Producto no encontrado en el carrito"))
            }

            currentSimpleCart[itemIndex] = CartItemSimple(productId, newQuantity)
            saveSimpleCart(userId, currentSimpleCart)

            getCart(userId)
        } catch (e: Exception) {
            Log.e(TAG, "updateCartItemQuantity - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un producto del carrito
     */
    suspend fun removeFromCart(userId: String, productId: String): Result<List<CartItem>> {
        return try {
            Log.d(TAG, "removeFromCart - productId: $productId")

            val currentSimpleCart = getSimpleCart(userId).toMutableList()
            currentSimpleCart.removeIf { it.productId == productId }

            saveSimpleCart(userId, currentSimpleCart)

            getCart(userId)
        } catch (e: Exception) {
            Log.e(TAG, "removeFromCart - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Limpia el carrito
     */
    suspend fun clearCart(userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "clearCart - userId: $userId")
            cartsCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "clearCart - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Realiza el checkout (compra)
     */
    suspend fun checkout(
        userId: String,
        user: User,
        discountCode: String? = null
    ): Result<Order> {
        return try {
            Log.d(TAG, "=== INICIO checkout ===")

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

            // Calcular totales
            val subtotal = cartItems.sumOf { it.subtotal }
            val discount = calculateDiscount(subtotal, discountCode)
            val total = subtotal - discount

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
            Log.d(TAG, "checkout - Orden creada: ${order.id}")

            // Descontar stock
            for (item in cartItems) {
                productRepository.decreaseStock(item.productId, item.quantity)
                Log.d(TAG, "checkout - Stock descontado para: ${item.productName}")
            }

            // Limpiar carrito
            clearCart(userId)

            Log.d(TAG, "=== FIN checkout ===")
            Result.success(order)
        } catch (e: Exception) {
            Log.e(TAG, "checkout - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene las órdenes de un usuario
     */
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
            Log.e(TAG, "getUserOrders - Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== FUNCIONES PRIVADAS ==========

    /**
     * Obtiene el carrito simple (solo IDs y cantidades) de Firebase
     */
    private suspend fun getSimpleCart(userId: String): List<CartItemSimple> {
        return try {
            val document = cartsCollection.document(userId).get().await()

            if (!document.exists()) {
                return emptyList()
            }

            @Suppress("UNCHECKED_CAST")
            val items = (document.data?.get("items") as? List<Map<String, Any?>>)?.map {
                CartItemSimple.fromMap(it)
            } ?: emptyList()

            Log.d(TAG, "getSimpleCart - Items: ${items.size}")
            items
        } catch (e: Exception) {
            Log.e(TAG, "getSimpleCart - Error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Guarda el carrito simple en Firebase
     * Solo guarda IDs de productos y cantidades
     */
    private suspend fun saveSimpleCart(userId: String, items: List<CartItemSimple>) {
        try {
            Log.d(TAG, "saveSimpleCart - userId: $userId, items: ${items.size}")

            val cartData = hashMapOf(
                "userId" to userId,
                "items" to items.map { it.toMap() },
                "updatedAt" to System.currentTimeMillis()
            )

            cartsCollection.document(userId).set(cartData).await()
            Log.d(TAG, "saveSimpleCart - Guardado exitoso")

            // Verificar que se guardó
            val verification = cartsCollection.document(userId).get().await()
            if (verification.exists()) {
                Log.d(TAG, "saveSimpleCart - Verificación OK")
            } else {
                Log.e(TAG, "saveSimpleCart - ERROR: No se guardó el documento")
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveSimpleCart - Error: ${e.message}", e)
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


