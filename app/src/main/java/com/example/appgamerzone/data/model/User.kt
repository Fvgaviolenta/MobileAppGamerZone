package com.example.appgamerzone.data.model

// Enum para roles de usuario
enum class UserRole {
    USER,
    ADMIN
}

// model/User.kt
data class User(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val age: Int = 0,
    val phone: String = "",
    val address: String = "",
    val role: UserRole = UserRole.USER,
    val isDuocStudent: Boolean = false,
    val referralCode: String? = null,
    val levelUpPoints: Int = 0,
    val level: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Constructor sin argumentos para Firestore
    constructor() : this(
        id = "",
        email = "",
        password = "",
        fullName = "",
        age = 0,
        phone = "",
        address = "",
        role = UserRole.USER,
        isDuocStudent = false,
        referralCode = null,
        levelUpPoints = 0,
        level = 1,
        createdAt = System.currentTimeMillis()
    )

    // Convertir a Map para Firestore
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "email" to email,
            "password" to password,
            "fullName" to fullName,
            "age" to age,
            "phone" to phone,
            "address" to address,
            "role" to role.name,
            "isDuocStudent" to isDuocStudent,
            "referralCode" to referralCode,
            "levelUpPoints" to levelUpPoints,
            "level" to level,
            "createdAt" to createdAt
        )
    }

    companion object {
        // Convertir desde Map de Firestore
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                id = map["id"] as? String ?: "",
                email = map["email"] as? String ?: "",
                password = map["password"] as? String ?: "",
                fullName = map["fullName"] as? String ?: "",
                age = (map["age"] as? Long)?.toInt() ?: 0,
                phone = map["phone"] as? String ?: "",
                address = map["address"] as? String ?: "",
                role = try {
                    UserRole.valueOf(map["role"] as? String ?: "USER")
                } catch (e: Exception) {
                    UserRole.USER
                },
                isDuocStudent = map["isDuocStudent"] as? Boolean ?: false,
                referralCode = map["referralCode"] as? String,
                levelUpPoints = (map["levelUpPoints"] as? Long)?.toInt() ?: 0,
                level = (map["level"] as? Long)?.toInt() ?: 1,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}
