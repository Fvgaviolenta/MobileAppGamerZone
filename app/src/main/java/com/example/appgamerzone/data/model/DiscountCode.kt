package com.example.appgamerzone.data.model

/**
 * Modelo de Código de Descuento
 *
 * Estructura en Firebase:
 * Collection: "discountCodes"
 * Document ID: auto-generado
 *
 * Ejemplo de documento:
 * {
 *   "code": "GAMER20",
 *   "discountPercentage": 20.0,
 *   "isActive": true,
 *   "description": "Descuento del 20% en toda la tienda",
 *   "expirationDate": "2025-12-31",
 *   "usageLimit": 100,
 *   "usageCount": 45
 * }
 */
data class DiscountCode(
    val id: String = "",
    val code: String = "",                      // Código único (ej: "GAMER20", "DUOC50")
    val discountPercentage: Double = 0.0,      // Porcentaje de descuento (ej: 20.0 para 20%)
    val isActive: Boolean = true,               // Si el código está activo
    val description: String = "",               // Descripción del descuento
    val expirationDate: String = "",            // Fecha de expiración (formato: "YYYY-MM-DD")
    val usageLimit: Int = -1,                   // Límite de usos (-1 = ilimitado)
    val usageCount: Int = 0                     // Contador de usos actuales
) {
    companion object {
        /**
         * Convierte un documento de Firestore a objeto DiscountCode
         */
        fun fromMap(id: String, map: Map<String, Any>): DiscountCode {
            return DiscountCode(
                id = id,
                code = map["code"] as? String ?: "",
                discountPercentage = (map["discountPercentage"] as? Number)?.toDouble() ?: 0.0,
                isActive = map["isActive"] as? Boolean ?: true,
                description = map["description"] as? String ?: "",
                expirationDate = map["expirationDate"] as? String ?: "",
                usageLimit = (map["usageLimit"] as? Number)?.toInt() ?: -1,
                usageCount = (map["usageCount"] as? Number)?.toInt() ?: 0
            )
        }
    }

    /**
     * Convierte el objeto DiscountCode a Map para Firebase
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "code" to code,
            "discountPercentage" to discountPercentage,
            "isActive" to isActive,
            "description" to description,
            "expirationDate" to expirationDate,
            "usageLimit" to usageLimit,
            "usageCount" to usageCount
        )
    }

    /**
     * Valida si el código puede ser usado
     */
    fun isValid(): Boolean {
        // Verificar si está activo
        if (!isActive) return false

        // Verificar si ha alcanzado el límite de usos
        if (usageLimit > 0 && usageCount >= usageLimit) return false

        // Verificar si ha expirado (comparación simple de strings YYYY-MM-DD)
        if (expirationDate.isNotEmpty()) {
            try {
                val calendar = java.util.Calendar.getInstance()
                val today = String.format(
                    "%04d-%02d-%02d",
                    calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH) + 1,
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                )
                if (expirationDate < today) return false
            } catch (e: Exception) {
                // Si hay error al comparar fechas, asumimos que no ha expirado
                return true
            }
        }

        return true
    }
}

