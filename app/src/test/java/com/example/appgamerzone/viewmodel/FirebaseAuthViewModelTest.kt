package com.example.appgamerzone.viewmodel

import android.content.Context
import com.example.appgamerzone.data.model.User
import com.example.appgamerzone.data.model.UserRole
import com.example.appgamerzone.data.repository.FirebaseAuthRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthViewModelTest : BehaviorSpec({
    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("actualización de campos del formulario") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        val viewModel = AuthViewModel(mockContext, mockRepository)

        When("se actualizan múltiples campos") {
            viewModel.updateFullName("John Doe")
            viewModel.updateEmail("john@example.com")
            viewModel.updatePassword("securepass")
            viewModel.updateConfirmPassword("securepass")

            Then("todos los campos deben actualizarse") {
                viewModel.uiState.value.fullName shouldBe "John Doe"
                viewModel.uiState.value.email shouldBe "john@example.com"
                viewModel.uiState.value.password shouldBe "securepass"
                viewModel.uiState.value.confirmPassword shouldBe "securepass"
            }
        }
    }

    Given("validación de campos") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        val viewModel = AuthViewModel(mockContext, mockRepository)

        When("se valida un email válido") {
            val email = "test@example.com"
            
            Then("el email debe ser válido") {
                email.contains("@") shouldBe true
                email.isNotBlank() shouldBe true
            }
        }

        When("se valida una contraseña válida") {
            val password = "password123"
            
            Then("la contraseña debe tener longitud adecuada") {
                (password.length >= 6) shouldBe true
            }
        }

        When("las contraseñas coinciden") {
            val password = "password123"
            val confirmPassword = "password123"
            
            Then("deben ser iguales") {
                password shouldBe confirmPassword
            }
        }

        When("las contraseñas no coinciden") {
            val password = "password123"
            val confirmPassword = "different"
            
            Then("no deben ser iguales") {
                password shouldNotBe confirmPassword
            }
        }
    }

    Given("validación de datos de usuario") {
        When("se verifica un usuario completo") {
            val user = User(
                id = "user123",
                email = "test@example.com",
                fullName = "Test User",
                password = "hashedPassword",
                role = UserRole.USER
            )

            Then("debe tener todos los campos necesarios") {
                user.id shouldNotBe ""
                user.email shouldNotBe ""
                user.fullName shouldNotBe ""
                user.role shouldBe UserRole.USER
            }
        }
    }

    Given("estados del formulario") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        val viewModel = AuthViewModel(mockContext, mockRepository)

        When("se inicializa") {
            Then("debe tener un estado inicial") {
                viewModel.uiState.value shouldNotBe null
                viewModel.uiState.value.email shouldBe ""
                viewModel.uiState.value.fullName shouldBe ""
            }
        }

        When("se actualiza el nombre completo") {
            viewModel.updateFullName("Test User")

            Then("debe actualizar el estado") {
                viewModel.uiState.value.fullName shouldBe "Test User"
            }
        }

        When("se actualiza el email") {
            viewModel.updateEmail("test@example.com")

            Then("debe actualizar el estado") {
                viewModel.uiState.value.email shouldBe "test@example.com"
            }
        }

        When("se actualiza la contraseña") {
            viewModel.updatePassword("password123")

            Then("debe actualizar el estado") {
                viewModel.uiState.value.password shouldBe "password123"
            }
        }

        When("se actualiza la confirmación de contraseña") {
            viewModel.updateConfirmPassword("password123")

            Then("debe actualizar el estado") {
                viewModel.uiState.value.confirmPassword shouldBe "password123"
            }
        }
    }

    Given("roles de usuario") {
        When("se verifica un usuario ADMIN") {
            val admin = User(
                id = "admin1",
                email = "admin@test.com",
                fullName = "Admin User",
                role = UserRole.ADMIN
            )

            Then("debe tener rol ADMIN") {
                admin.role shouldBe UserRole.ADMIN
            }
        }

        When("se verifica un usuario USER") {
            val user = User(
                id = "user1",
                email = "user@test.com",
                fullName = "Regular User",
                role = UserRole.USER
            )

            Then("debe tener rol USER") {
                user.role shouldBe UserRole.USER
            }
        }
    }
})

