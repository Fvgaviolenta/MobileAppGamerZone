package com.example.appgamerzone.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.COMPLETED,
    val discountCode: String? = null
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", "", "", emptyList(), 0.0, 0.0, 0.0)

    // Convertir a Map para Firestore
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "userId" to userId,
            "userName" to userName,
            "userEmail" to userEmail,
            "items" to items.map { it.toMap() },
            "subtotal" to subtotal,
            "discount" to discount,
            "total" to total,
            "date" to date,
            "status" to status.name,
            "discountCode" to discountCode
        )
    }

    companion object {
        // Convertir desde Map de Firestore
        fun fromMap(id: String, map: Map<String, Any?>): Order {
            @Suppress("UNCHECKED_CAST")
            val itemsList = (map["items"] as? List<Map<String, Any?>>)?.map {
                CartItem.fromMap(it)
            } ?: emptyList()

            return Order(
                id = id,
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                userEmail = map["userEmail"] as? String ?: "",
                items = itemsList,
                subtotal = map["subtotal"] as? Double ?: 0.0,
                discount = map["discount"] as? Double ?: 0.0,
                total = map["total"] as? Double ?: 0.0,
                date = map["date"] as? Long ?: System.currentTimeMillis(),
                status = try {
                    OrderStatus.valueOf(map["status"] as? String ?: "COMPLETED")
                } catch (e: Exception) {
                    OrderStatus.COMPLETED
                },
                discountCode = map["discountCode"] as? String
            )
        }
    }
}

enum class OrderStatus {
    COMPLETED,
    CANCELLED,
    PENDING
}

