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
}

