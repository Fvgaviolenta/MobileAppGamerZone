package com.example.appgamerzone.data

import com.example.appgamerzone.data.repository.FirebaseAuthRepository
import com.example.appgamerzone.data.repository.FirebaseProductRepository

// ============================================
// CLASE COMENTADA: Ya no se usa inicialización automática
// Los productos y usuarios deben crearse manualmente
// ============================================
/*
class FirebaseInitializer {
    private val authRepository = FirebaseAuthRepository()
    private val productRepository = FirebaseProductRepository()

    suspend fun initialize(): Result<String> {
        return try {
            // Inicializar usuario admin
            val adminResult = authRepository.initializeAdminUser()
            if (adminResult.isFailure) {
                return Result.failure(adminResult.exceptionOrNull() ?: Exception("Error al crear admin"))
            }

            // Inicializar productos de ejemplo
            val productsResult = productRepository.initializeSampleProducts()
            if (productsResult.isFailure) {
                return Result.failure(productsResult.exceptionOrNull() ?: Exception("Error al crear productos"))
            }

            Result.success("Firebase inicializado correctamente")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
*/

