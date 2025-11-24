package com.example.appgamerzone.data.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class UserTest : BehaviorSpec({

    Given("un modelo User") {
        When("se crea un usuario con constructor completo") {
            val user = User(
                id = "user123",
                email = "test@example.com",
                password = "password123",
                fullName = "Test User",
                age = 25,
                phone = "123456789",
                address = "Test Address",
                role = UserRole.USER,
                isDuocStudent = false,
                referralCode = null,
                levelUpPoints = 100,
                level = 5,
                createdAt = 1234567890L
            )

            Then("debe tener todos los valores asignados") {
                user.id shouldBe "user123"
                user.email shouldBe "test@example.com"
                user.fullName shouldBe "Test User"
                user.age shouldBe 25
                user.phone shouldBe "123456789"
                user.address shouldBe "Test Address"
                user.role shouldBe UserRole.USER
                user.levelUpPoints shouldBe 100
                user.level shouldBe 5
            }
        }

        When("se crea un usuario con constructor por defecto") {
            val user = User()

            Then("debe tener valores por defecto") {
                user.id shouldBe ""
                user.email shouldBe ""
                user.fullName shouldBe ""
                user.age shouldBe 0
                user.role shouldBe UserRole.USER
                user.level shouldBe 1
                user.levelUpPoints shouldBe 0
            }
        }

        When("se convierte un User a Map") {
            val user = User(
                id = "user123",
                email = "test@example.com",
                password = "password123",
                fullName = "Test User",
                age = 25,
                phone = "123456789",
                address = "Test Address",
                role = UserRole.ADMIN,
                isDuocStudent = true,
                referralCode = "REFER123",
                levelUpPoints = 500,
                level = 10
            )

            val map = user.toMap()

            Then("debe contener todos los campos") {
                map["id"] shouldBe "user123"
                map["email"] shouldBe "test@example.com"
                map["fullName"] shouldBe "Test User"
                map["age"] shouldBe 25
                map["phone"] shouldBe "123456789"
                map["address"] shouldBe "Test Address"
                map["role"] shouldBe "ADMIN"
                map["isDuocStudent"] shouldBe true
                map["referralCode"] shouldBe "REFER123"
                map["levelUpPoints"] shouldBe 500
                map["level"] shouldBe 10
            }
        }

        When("se crea un User desde un Map") {
            val map = mapOf(
                "id" to "user123",
                "email" to "test@example.com",
                "password" to "password123",
                "fullName" to "Test User",
                "age" to 25,
                "phone" to "123456789",
                "address" to "Test Address",
                "role" to "ADMIN",
                "isDuocStudent" to true,
                "referralCode" to "REFER123",
                "levelUpPoints" to 500,
                "level" to 10,
                "createdAt" to 1234567890L
            )

            val user = User.fromMap(map)

            Then("debe crear el objeto correctamente") {
                user.id shouldBe "user123"
                user.email shouldBe "test@example.com"
                user.fullName shouldBe "Test User"
                user.age shouldBe 25
                user.role shouldBe UserRole.ADMIN
                user.isDuocStudent shouldBe true
                user.levelUpPoints shouldBe 500
                user.level shouldBe 10
            }
        }

        When("se copian valores con copy()") {
            val original = User(
                id = "user123",
                email = "original@example.com",
                fullName = "Original Name"
            )

            val modified = original.copy(
                email = "modified@example.com",
                fullName = "Modified Name"
            )

            Then("debe crear una nueva instancia con cambios") {
                modified.id shouldBe original.id
                modified.email shouldBe "modified@example.com"
                modified.fullName shouldBe "Modified Name"
                original.email shouldBe "original@example.com"
            }
        }
    }

    Given("roles de usuario") {
        When("se comparan roles") {
            val userRole = UserRole.USER
            val adminRole = UserRole.ADMIN

            Then("deben ser diferentes") {
                userRole shouldBe UserRole.USER
                adminRole shouldBe UserRole.ADMIN
                userRole shouldNotBe adminRole
            }
        }

        When("se convierte un rol a String") {
            val role = UserRole.ADMIN

            Then("debe ser el nombre del enum") {
                role.name shouldBe "ADMIN"
            }
        }
    }

    Given("validación de datos de usuario") {
        When("se valida un email") {
            val user = User(email = "test@example.com")

            Then("debe contener @") {
                user.email.contains("@") shouldBe true
            }
        }

        When("se valida la edad") {
            val user = User(age = 25)

            Then("debe ser un número positivo") {
                (user.age > 0) shouldBe true
            }
        }

        When("se valida el nivel") {
            val user = User(level = 5, levelUpPoints = 500)

            Then("debe tener valores consistentes") {
                user.level shouldBe 5
                user.levelUpPoints shouldBe 500
                (user.level > 0) shouldBe true
            }
        }
    }

    Given("usuarios con características especiales") {
        When("es un estudiante DUOC") {
            val student = User(
                email = "student@duoc.cl",
                isDuocStudent = true
            )

            Then("debe estar marcado como estudiante") {
                student.isDuocStudent shouldBe true
            }
        }

        When("tiene código de referido") {
            val user = User(
                email = "user@example.com",
                referralCode = "FRIEND123"
            )

            Then("debe tener el código") {
                user.referralCode shouldBe "FRIEND123"
                user.referralCode shouldNotBe null
            }
        }

        When("no tiene código de referido") {
            val user = User(
                email = "user@example.com",
                referralCode = null
            )

            Then("debe ser null") {
                user.referralCode shouldBe null
            }
        }
    }

    Given("timestamp de creación") {
        When("se crea un usuario") {
            val user = User(email = "test@example.com")

            Then("debe tener un timestamp") {
                (user.createdAt > 0) shouldBe true
            }
        }

        When("se comparan timestamps") {
            val user1 = User(email = "user1@example.com", createdAt = 1000L)
            val user2 = User(email = "user2@example.com", createdAt = 2000L)

            Then("debe poder determinar cuál fue creado primero") {
                (user2.createdAt > user1.createdAt) shouldBe true
            }
        }
    }
})

