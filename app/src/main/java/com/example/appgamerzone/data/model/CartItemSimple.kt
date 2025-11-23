package com.example.appgamerzone.data.model

/**
 * Modelo simplificado para persistir en Firebase
 * Solo guarda ID del producto y cantidad
 */
data class CartItemSimple(
    val productId: String = "",
    val quantity: Int = 0
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", 0)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "productId" to productId,
            "quantity" to quantity
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): CartItemSimple {
            return CartItemSimple(
                productId = map["productId"] as? String ?: "",
                quantity = (map["quantity"] as? Long)?.toInt() ?: 0
            )
        }
    }
}

