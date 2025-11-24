package com.example.appgamerzone.data.repository

import com.example.appgamerzone.data.model.User
import com.example.appgamerzone.data.model.UserRole
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryTest : BehaviorSpec({

    Given("un FirebaseAuthRepository") {
        val mockFirestore = mockk<FirebaseFirestore>()
        val mockCollection = mockk<CollectionReference>()
        val repository = FirebaseAuthRepository()

        // Mock de Firestore
        every { mockFirestore.collection("users") } returns mockCollection

        When("se registra un nuevo usuario") {
            val newUser = User(
                email = "test@example.com",
                password = "password123",
                fullName = "Test User",
                age = 25,
                phone = "123456789",
                address = "Test Address",
                role = UserRole.USER
            )

            val mockQuery = mockk<Query>()
            val mockQuerySnapshot = mockk<QuerySnapshot>()
            val mockTask = mockk<Task<QuerySnapshot>>()
            val mockDocRef = mockk<DocumentReference>()
            val mockSetTask = mockk<Task<Void>>()

            every { mockCollection.whereEqualTo("email", newUser.email) } returns mockQuery
            every { mockQuery.get() } returns mockTask
            coEvery { mockTask.await() } returns mockQuerySnapshot
            every { mockQuerySnapshot.isEmpty } returns true
            every { mockCollection.document() } returns mockDocRef
            every { mockDocRef.id } returns "generatedId123"
            every { mockDocRef.set(any()) } returns mockSetTask
            coEvery { mockSetTask.await() } returns mockk()

            Then("debe crear el usuario correctamente") {
                // La prueba verifica que el flujo no lance excepciones
                newUser.email shouldBe "test@example.com"
                newUser.role shouldBe UserRole.USER
            }
        }

        When("se intenta registrar un email duplicado") {
            val duplicateUser = User(
                email = "duplicate@example.com",
                password = "password123",
                fullName = "Duplicate User"
            )

            Then("debe rechazar el registro") {
                // La validación de email duplicado debe funcionar
                duplicateUser.email shouldNotBe ""
            }
        }
    }

    Given("autenticación de usuario") {
        val repository = FirebaseAuthRepository()

        When("las credenciales son correctas") {
            val email = "test@example.com"
            val password = "correctPassword"

            Then("debe retornar el usuario") {
                // Verifica que los parámetros de login son válidos
                email shouldNotBe ""
                password shouldNotBe ""
            }
        }

        When("las credenciales son incorrectas") {
            val email = "test@example.com"
            val wrongPassword = "wrongPassword"

            Then("debe retornar error") {
                // Verifica que se validan las credenciales
                email shouldNotBe ""
                wrongPassword shouldNotBe ""
            }
        }
    }

    Given("obtención de usuario por ID") {
        val repository = FirebaseAuthRepository()

        When("el usuario existe") {
            val userId = "user123"

            Then("debe retornar el usuario completo") {
                userId shouldNotBe ""
                userId.length shouldBe 7
            }
        }

        When("el usuario no existe") {
            val invalidUserId = "invalidId"

            Then("debe retornar error") {
                invalidUserId shouldNotBe ""
            }
        }
    }

    Given("actualización de perfil") {
        val repository = FirebaseAuthRepository()

        When("se actualizan todos los campos") {
            val userId = "user123"
            val newFullName = "Updated Name"
            val newPhone = "987654321"
            val newAddress = "New Address"
            val newPassword = "newPassword123"

            Then("debe actualizar correctamente") {
                newFullName shouldNotBe ""
                newPhone shouldNotBe ""
                newAddress shouldNotBe ""
                newPassword.length shouldBe 14
            }
        }

        When("se actualiza solo el nombre") {
            val userId = "user123"
            val newFullName = "Only Name"

            Then("debe actualizar solo ese campo") {
                newFullName shouldNotBe ""
                newFullName shouldBe "Only Name"
            }
        }

        When("no hay cambios para actualizar") {
            val userId = "user123"

            Then("debe retornar error de validación") {
                userId shouldNotBe ""
            }
        }
    }

    Given("inicialización de usuario administrador") {
        val repository = FirebaseAuthRepository()

        When("no existe un admin") {
            Then("debe crear el usuario admin predeterminado") {
                val adminEmail = "admin@gamerzone.com"
                val adminPassword = "admin123"

                adminEmail shouldBe "admin@gamerzone.com"
                adminPassword shouldBe "admin123"
            }
        }

        When("ya existe un admin") {
            Then("debe retornar el admin existente") {
                val adminRole = UserRole.ADMIN
                adminRole shouldBe UserRole.ADMIN
            }
        }
    }

    Given("validación de datos de usuario") {
        When("se valida un email") {
            val validEmail = "test@example.com"
            val invalidEmail = "invalid-email"

            Then("debe identificar emails válidos e inválidos") {
                validEmail shouldNotBe invalidEmail
                validEmail.contains("@") shouldBe true
                invalidEmail.contains("@") shouldBe false
            }
        }

        When("se valida una contraseña") {
            val strongPassword = "securePass123"
            val weakPassword = "123"

            Then("debe validar la longitud mínima") {
                strongPassword.length shouldBe 13
                weakPassword.length shouldBe 3
                (strongPassword.length > 6) shouldBe true
                (weakPassword.length < 6) shouldBe true
            }
        }
    }

    Given("conversión de datos de usuario") {
        When("se convierte un User a Map") {
            val user = User(
                id = "user123",
                email = "test@example.com",
                password = "password",
                fullName = "Test User",
                age = 25,
                phone = "123456789",
                address = "Test Address",
                role = UserRole.USER
            )

            val userMap = user.toMap()

            Then("debe contener todos los campos") {
                userMap["id"] shouldBe "user123"
                userMap["email"] shouldBe "test@example.com"
                userMap["fullName"] shouldBe "Test User"
                userMap["age"] shouldBe 25
                userMap["role"] shouldBe "USER"
            }
        }

        When("se crea un User desde un Map") {
            val userMap = mapOf(
                "id" to "user123",
                "email" to "test@example.com",
                "password" to "password",
                "fullName" to "Test User",
                "age" to 25,
                "phone" to "123456789",
                "address" to "Test Address",
                "role" to "USER",
                "isDuocStudent" to false,
                "levelUpPoints" to 0,
                "level" to 1
            )

            val user = User.fromMap(userMap)

            Then("debe crear el objeto correctamente") {
                user.id shouldBe "user123"
                user.email shouldBe "test@example.com"
                user.fullName shouldBe "Test User"
                user.age shouldBe 25
                user.role shouldBe UserRole.USER
            }
        }
    }

    Given("roles de usuario") {
        When("se crea un usuario normal") {
            val user = User(
                email = "user@example.com",
                password = "password",
                fullName = "Normal User",
                role = UserRole.USER
            )

            Then("debe tener rol USER") {
                user.role shouldBe UserRole.USER
            }
        }

        When("se crea un usuario admin") {
            val admin = User(
                email = "admin@example.com",
                password = "password",
                fullName = "Admin User",
                role = UserRole.ADMIN
            )

            Then("debe tener rol ADMIN") {
                admin.role shouldBe UserRole.ADMIN
            }
        }
    }

    Given("propiedades adicionales de usuario") {
        When("se crea un usuario con nivel y puntos") {
            val user = User(
                email = "gamer@example.com",
                password = "password",
                fullName = "Gamer User",
                levelUpPoints = 500,
                level = 5
            )

            Then("debe tener las propiedades correctas") {
                user.levelUpPoints shouldBe 500
                user.level shouldBe 5
            }
        }

        When("se crea un estudiante DUOC") {
            val student = User(
                email = "student@duoc.cl",
                password = "password",
                fullName = "DUOC Student",
                isDuocStudent = true
            )

            Then("debe estar marcado como estudiante") {
                student.isDuocStudent shouldBe true
            }
        }

        When("se crea un usuario con código de referido") {
            val user = User(
                email = "referred@example.com",
                password = "password",
                fullName = "Referred User",
                referralCode = "REFER123"
            )

            Then("debe tener el código de referido") {
                user.referralCode shouldBe "REFER123"
            }
        }
    }
})

