package com.example.appgamerzone.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.appgamerzone.data.model.User
import com.example.appgamerzone.data.model.UserRole
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun register(user: User): Result<User> {
        return try {
            // Verificar si el email ya existe
            val existing = usersCollection
                .whereEqualTo("email", user.email)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(IllegalStateException("El correo ya está registrado"))
            }

            // Crear nuevo usuario
            val docRef = usersCollection.document()
            val newUser = user.copy(
                id = docRef.id,
                role = UserRole.USER // Siempre USER al registrarse
            )
            docRef.set(newUser.toMap()).await()

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(IllegalArgumentException("Credenciales inválidas"))
            }

            val document = querySnapshot.documents[0]
            val userData = document.data ?: return Result.failure(
                IllegalStateException("Datos de usuario no encontrados")
            )

            // CRÍTICO: Agregar el ID del documento al map
            val userDataWithId = userData.toMutableMap()
            userDataWithId["id"] = document.id

            val user = User.fromMap(userDataWithId)

            // Log para verificar
            android.util.Log.d("FirebaseAuthRepository", "🔑 Login exitoso - userId: ${user.id}, email: ${user.email}")

            Result.success(user)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAuthRepository", "❌ Error en login: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()

            if (!document.exists()) {
                return Result.failure(IllegalStateException("Usuario no encontrado"))
            }

            val userData = document.data ?: return Result.failure(
                IllegalStateException("Datos de usuario no encontrados")
            )

            val user = User.fromMap(userData)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        userId: String,
        fullName: String? = null,
        phone: String? = null,
        address: String? = null,
        password: String? = null
    ): Result<User> {
        return try {
            val updates = mutableMapOf<String, Any>()

            fullName?.let { updates["fullName"] = it }
            phone?.let { updates["phone"] = it }
            address?.let { updates["address"] = it }
            password?.let { updates["password"] = it }

            if (updates.isEmpty()) {
                return Result.failure(IllegalArgumentException("No hay cambios para actualizar"))
            }

            usersCollection.document(userId).update(updates).await()

            // Obtener usuario actualizado
            getUserById(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initializeAdminUser(): Result<User> {
        return try {
            // Verificar si ya existe el admin
            val existingAdmin = usersCollection
                .whereEqualTo("email", "admin@gamerzone.com")
                .get()
                .await()

            if (!existingAdmin.isEmpty) {
                val document = existingAdmin.documents[0]
                val userData = document.data ?: return Result.failure(
                    IllegalStateException("Datos de admin no encontrados")
                )
                return Result.success(User.fromMap(userData))
            }

            // Crear usuario admin
            val adminUser = User(
                id = "",
                email = "admin@gamerzone.com",
                password = "admin123",
                fullName = "Administrador",
                age = 30,
                phone = "+56912345678",
                address = "Santiago, Chile",
                role = UserRole.ADMIN,
                isDuocStudent = false,
                levelUpPoints = 1000,
                level = 10
            )

            val docRef = usersCollection.document()
            val newAdmin = adminUser.copy(id = docRef.id)
            docRef.set(newAdmin.toMap()).await()

            Result.success(newAdmin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

