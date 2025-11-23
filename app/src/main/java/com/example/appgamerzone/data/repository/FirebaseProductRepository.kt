package com.example.appgamerzone.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.appgamerzone.data.model.Product
import kotlinx.coroutines.tasks.await

class FirebaseProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val querySnapshot = productsCollection.get().await()
            val products = querySnapshot.documents.mapNotNull { document ->
                document.data?.let { Product.fromMap(document.id, it) }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        return try {
            val querySnapshot = productsCollection
                .whereEqualTo("category", category)
                .get()
                .await()

            val products = querySnapshot.documents.mapNotNull { document ->
                document.data?.let { Product.fromMap(document.id, it) }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductById(productId: String): Result<Product> {
        return try {
            val document = productsCollection.document(productId).get().await()

            if (!document.exists()) {
                return Result.failure(IllegalStateException("Producto no encontrado"))
            }

            val productData = document.data ?: return Result.failure(
                IllegalStateException("Datos de producto no encontrados")
            )

            val product = Product.fromMap(document.id, productData)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProduct(product: Product): Result<Product> {
        return try {
            val docRef = productsCollection.document()
            val newProduct = product.copy(id = docRef.id)
            docRef.set(newProduct.toMap()).await()
            Result.success(newProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Product> {
        return try {
            if (product.id.isEmpty()) {
                return Result.failure(IllegalArgumentException("ID de producto inválido"))
            }

            productsCollection.document(product.id).set(product.toMap()).await()
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStock(productId: String, newStock: Int): Result<Unit> {
        return try {
            productsCollection.document(productId)
                .update("stock", newStock)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decreaseStock(productId: String, quantity: Int): Result<Unit> {
        return try {
            val product = getProductById(productId).getOrThrow()

            if (product.stock < quantity) {
                return Result.failure(IllegalStateException("Stock insuficiente"))
            }

            val newStock = product.stock - quantity
            updateStock(productId, newStock)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<String>> {
        return try {
            val querySnapshot = productsCollection.get().await()
            val categories = querySnapshot.documents
                .mapNotNull { it.data?.get("category") as? String }
                .distinct()
                .sorted()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initializeSampleProducts(): Result<List<Product>> {
        return try {
            // Verificar si ya hay productos
            val existing = productsCollection.limit(1).get().await()
            if (!existing.isEmpty) {
                return getAllProducts()
            }

            // Crear productos de ejemplo
            val sampleProducts = listOf(
                Product(
                    name = "PlayStation 5",
                    price = 599990.0,
                    category = "Consolas",
                    description = "Consola de última generación con gráficos 4K y SSD ultrarrápido",
                    imageUrl = "https://i.imgur.com/8JYrs6D.png",
                    stock = 10,
                    rating = 4.8,
                    reviewCount = 1250
                ),
                Product(
                    name = "Xbox Series X",
                    price = 549990.0,
                    category = "Consolas",
                    description = "La Xbox más potente con rendimiento 4K real",
                    imageUrl = "https://i.imgur.com/xq6sZHL.png",
                    stock = 8,
                    rating = 4.7,
                    reviewCount = 980
                ),
                Product(
                    name = "Nintendo Switch OLED",
                    price = 399990.0,
                    category = "Consolas",
                    description = "Consola híbrida con pantalla OLED de 7 pulgadas",
                    imageUrl = "https://i.imgur.com/n7u9s1T.png",
                    stock = 15,
                    rating = 4.9,
                    reviewCount = 2100
                ),
                Product(
                    name = "The Last of Us Part II",
                    price = 39990.0,
                    category = "Juegos",
                    description = "Aventura épica exclusiva de PlayStation",
                    imageUrl = "https://i.imgur.com/bqClxhV.png",
                    stock = 25,
                    rating = 4.9,
                    reviewCount = 5600
                ),
                Product(
                    name = "Zelda: Tears of the Kingdom",
                    price = 59990.0,
                    category = "Juegos",
                    description = "La secuela épica de Breath of the Wild",
                    imageUrl = "https://i.imgur.com/RZGj9nZ.png",
                    stock = 20,
                    rating = 5.0,
                    reviewCount = 8900
                ),
                Product(
                    name = "Control DualSense",
                    price = 69990.0,
                    category = "Accesorios",
                    description = "Control inalámbrico con respuesta háptica",
                    imageUrl = "https://i.imgur.com/nVGz5Gk.png",
                    stock = 30,
                    rating = 4.6,
                    reviewCount = 3200
                ),
                Product(
                    name = "Auriculares Gaming RGB",
                    price = 89990.0,
                    category = "Accesorios",
                    description = "Auriculares con sonido envolvente 7.1 y micrófono",
                    imageUrl = "https://i.imgur.com/xZnqJKM.png",
                    stock = 18,
                    rating = 4.5,
                    reviewCount = 1800
                ),
                Product(
                    name = "Teclado Mecánico RGB",
                    price = 129990.0,
                    category = "Accesorios",
                    description = "Teclado mecánico para gaming con switches Cherry MX",
                    imageUrl = "https://i.imgur.com/QK5xJlM.png",
                    stock = 12,
                    rating = 4.7,
                    reviewCount = 2400
                )
            )

            val createdProducts = mutableListOf<Product>()
            for (product in sampleProducts) {
                val result = createProduct(product)
                result.getOrNull()?.let { createdProducts.add(it) }
            }

            Result.success(createdProducts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

