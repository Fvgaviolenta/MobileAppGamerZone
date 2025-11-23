package com.example.appgamerzone.data.model

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val availableStock: Int = 0
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", "", 0, 0.0, 0)

    val subtotal: Double
        get() = quantity * unitPrice

    // Convertir a Map para Firestore
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "productId" to productId,
            "productName" to productName,
            "productImage" to productImage,
            "quantity" to quantity,
            "unitPrice" to unitPrice,
            "availableStock" to availableStock
        )
    }

    companion object {
        // Convertir desde Map de Firestore
        fun fromMap(map: Map<String, Any?>): CartItem {
            return CartItem(
                productId = map["productId"] as? String ?: "",
                productName = map["productName"] as? String ?: "",
                productImage = map["productImage"] as? String ?: "",
                quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                unitPrice = map["unitPrice"] as? Double ?: 0.0,
                availableStock = (map["availableStock"] as? Long)?.toInt() ?: 0
            )
        }
    }
}

