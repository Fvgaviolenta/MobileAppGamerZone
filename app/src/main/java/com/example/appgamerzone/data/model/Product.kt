package com.example.appgamerzone.data.model


data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val stock: Int = 0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0
) {
    // Constructor sin argumentos para Firestore
    constructor() : this(
        id = "",
        name = "",
        price = 0.0,
        category = "",
        description = "",
        imageUrl = "",
        stock = 0,
        rating = 0.0,
        reviewCount = 0
    )

    // Convertir a Map para Firestore
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "price" to price,
            "category" to category,
            "description" to description,
            "imageUrl" to imageUrl,
            "stock" to stock,
            "rating" to rating,
            "reviewCount" to reviewCount
        )
    }

    companion object {
        // Convertir desde Map de Firestore
        fun fromMap(id: String, map: Map<String, Any?>): Product {
            // Firebase puede guardar números como Long o Double
            val price = when (val priceValue = map["price"]) {
                is Double -> priceValue
                is Long -> priceValue.toDouble()
                is Int -> priceValue.toDouble()
                is Number -> priceValue.toDouble()
                else -> 0.0
            }

            val rating = when (val ratingValue = map["rating"]) {
                is Double -> ratingValue
                is Long -> ratingValue.toDouble()
                is Int -> ratingValue.toDouble()
                is Number -> ratingValue.toDouble()
                else -> 0.0
            }

            return Product(
                id = id,
                name = map["name"] as? String ?: "",
                price = price,
                category = map["category"] as? String ?: "",
                description = map["description"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                stock = (map["stock"] as? Long)?.toInt() ?: (map["stock"] as? Int) ?: 0,
                rating = rating,
                reviewCount = (map["reviewCount"] as? Long)?.toInt() ?: (map["reviewCount"] as? Int) ?: 0
            )
        }
    }
}

