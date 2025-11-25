package com.example.appgamerzone.data.repository

import android.util.Log
import com.example.appgamerzone.data.model.DiscountCode
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para gestión de códigos de descuento en Firebase
 */
class FirebaseDiscountRepository {
    private val db = FirebaseFirestore.getInstance()
    private val discountCodesCollection = db.collection("discountCodes")

    companion object {
        private const val TAG = "DiscountRepository"
    }

    /**
     * Valida un código de descuento
     * @param code Código a validar (ej: "GAMER20")
     * @return Result con el DiscountCode si es válido, o null si no existe/no es válido
     */
    suspend fun validateDiscountCode(code: String): Result<DiscountCode?> {
        return try {
            Log.d(TAG, "Validando código: $code")

            val querySnapshot = discountCodesCollection
                .whereEqualTo("code", code.uppercase().trim())
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Log.d(TAG, "Código no encontrado: $code")
                Result.success(null)
            } else {
                val document = querySnapshot.documents[0]
                val discountCode = document.data?.let { DiscountCode.fromMap(document.id, it) }

                if (discountCode != null && discountCode.isValid()) {
                    Log.d(TAG, "Código válido: ${discountCode.code} - ${discountCode.discountPercentage}%")
                    Result.success(discountCode)
                } else {
                    Log.d(TAG, "Código no válido o expirado: $code")
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al validar código: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Incrementa el contador de usos de un código
     */
    suspend fun incrementUsageCount(discountCodeId: String): Result<Unit> {
        return try {
            val docRef = discountCodesCollection.document(discountCodeId)
            val document = docRef.get().await()

            if (document.exists()) {
                val currentCount = (document.data?.get("usageCount") as? Number)?.toInt() ?: 0
                docRef.update("usageCount", currentCount + 1).await()
                Log.d(TAG, "Incrementado uso del código: $discountCodeId")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al incrementar uso: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Crea un nuevo código de descuento (solo para admin)
     */
    suspend fun createDiscountCode(discountCode: DiscountCode): Result<DiscountCode> {
        return try {
            val docRef = discountCodesCollection.add(discountCode.toMap()).await()
            val createdCode = discountCode.copy(id = docRef.id)
            Log.d(TAG, "Código creado: ${createdCode.code}")
            Result.success(createdCode)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear código: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los códigos de descuento (para admin)
     */
    suspend fun getAllDiscountCodes(): Result<List<DiscountCode>> {
        return try {
            val querySnapshot = discountCodesCollection.get().await()
            val codes = querySnapshot.documents.mapNotNull { document ->
                document.data?.let { DiscountCode.fromMap(document.id, it) }
            }
            Result.success(codes)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener códigos: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza un código de descuento (para admin)
     */
    suspend fun updateDiscountCode(discountCode: DiscountCode): Result<Unit> {
        return try {
            discountCodesCollection
                .document(discountCode.id)
                .set(discountCode.toMap())
                .await()
            Log.d(TAG, "Código actualizado: ${discountCode.code}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar código: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un código de descuento (para admin)
     */
    suspend fun deleteDiscountCode(discountCodeId: String): Result<Unit> {
        return try {
            discountCodesCollection.document(discountCodeId).delete().await()
            Log.d(TAG, "Código eliminado: $discountCodeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar código: ${e.message}", e)
            Result.failure(e)
        }
    }
}

