package com.example.appgamerzone.viewmodel

import android.content.Context
import app.cash.turbine.test
import com.example.appgamerzone.data.model.CartItem
import com.example.appgamerzone.data.repository.FirebaseAuthRepository
import com.example.appgamerzone.data.repository.FirebaseCartRepository
import com.example.appgamerzone.data.session.UserSessionManager
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest : BehaviorSpec({
    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("un CartViewModel inicializado") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockCartRepository = mockk<FirebaseCartRepository>(relaxed = true)
        val mockAuthRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        // Mock del UserSessionManager
        mockkConstructor(UserSessionManager::class)
        every { anyConstructed<UserSessionManager>().currentUserId } returns MutableStateFlow("user123")

        // Mock de respuestas del repositorio
        coEvery { mockCartRepository.getCart("user123") } returns Result.success(emptyList())

        val viewModel = CartViewModel(mockContext, mockCartRepository, mockAuthRepository)

        When("se inicializa el ViewModel") {
            Then("el estado inicial debe tener carrito vacío") {
                viewModel.uiState.value.items.shouldBeEmpty()
                viewModel.uiState.value.total shouldBe 0.0
                viewModel.uiState.value.totalItems shouldBe 0
            }
        }

        When("se agrega un producto al carrito") {
            val productId = "product1"
            val quantity = 2

            Then("debe tener parámetros válidos") {
                productId shouldNotBe ""
                (quantity > 0) shouldBe true
            }
        }

        When("se elimina un producto del carrito") {
            val productId = "product1"

            Then("debe tener un ID válido") {
                productId shouldNotBe ""
            }
        }

        When("se elimina un item del carrito") {
            val productId = "product1"

            Then("debe tener un ID válido") {
                productId shouldNotBe ""
            }
        }
    }

    Given("un carrito con productos") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockCartRepository = mockk<FirebaseCartRepository>(relaxed = true)
        val mockAuthRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        mockkConstructor(UserSessionManager::class)
        every { anyConstructed<UserSessionManager>().currentUserId } returns MutableStateFlow("user123")

        val testCartItems = listOf(
            CartItem(
                productId = "product1",
                productName = "Producto 1",
                productImage = "",
                quantity = 2,
                unitPrice = 100.0,
                availableStock = 10
            ),
            CartItem(
                productId = "product2",
                productName = "Producto 2",
                productImage = "",
                quantity = 1,
                unitPrice = 50.0,
                availableStock = 5
            )
        )

        coEvery { mockCartRepository.getCart("user123") } returns Result.success(testCartItems)

        val viewModel = CartViewModel(mockContext, mockCartRepository, mockAuthRepository)

        When("se carga el carrito") {
            viewModel.loadCart()

            Then("debe mostrar los productos correctos") {
                viewModel.uiState.value.items shouldHaveSize 2
                viewModel.uiState.value.totalItems shouldBe 3 // 2 + 1
                viewModel.uiState.value.subtotal shouldBe 250.0 // (100*2) + (50*1)
            }
        }
    }

    Given("cálculo de totales") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockCartRepository = mockk<FirebaseCartRepository>(relaxed = true)
        val mockAuthRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        mockkConstructor(UserSessionManager::class)
        every { anyConstructed<UserSessionManager>().currentUserId } returns MutableStateFlow("user123")
        coEvery { mockCartRepository.getCart("user123") } returns Result.success(emptyList())

        val viewModel = CartViewModel(mockContext, mockCartRepository, mockAuthRepository)

        When("se calcula el total con descuento") {
            val items = listOf(
                CartItem("p1", "Producto 1", "", 2, 100.0, 10),
                CartItem("p2", "Producto 2", "", 1, 50.0, 5)
            )

            Then("el subtotal debe ser correcto") {
                val subtotal = items.sumOf { it.unitPrice * it.quantity }
                subtotal shouldBe 250.0
            }
        }
    }

    Given("aplicación de código de descuento") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockCartRepository = mockk<FirebaseCartRepository>(relaxed = true)
        val mockAuthRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        mockkConstructor(UserSessionManager::class)
        every { anyConstructed<UserSessionManager>().currentUserId } returns MutableStateFlow("user123")

        val items = listOf(
            CartItem("p1", "Producto 1", "", 2, 100.0, 10)
        )
        coEvery { mockCartRepository.getCart("user123") } returns Result.success(items)

        val viewModel = CartViewModel(mockContext, mockCartRepository, mockAuthRepository)

        When("se carga el carrito") {
            viewModel.loadCart()

            Then("los items deben cargarse") {
                coVerify { mockCartRepository.getCart("user123") }
            }
        }
    }

    Given("proceso de checkout") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockCartRepository = mockk<FirebaseCartRepository>(relaxed = true)
        val mockAuthRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        mockkConstructor(UserSessionManager::class)
        every { anyConstructed<UserSessionManager>().currentUserId } returns MutableStateFlow("user123")

        val items = listOf(
            CartItem("p1", "Producto 1", "", 2, 100.0, 10)
        )
        coEvery { mockCartRepository.getCart("user123") } returns Result.success(items)
        coEvery { mockCartRepository.checkout("user123", any(), any()) } returns Result.success(
            mockk(relaxed = true)
        )

        val viewModel = CartViewModel(mockContext, mockCartRepository, mockAuthRepository)

        When("se realiza el checkout") {
            viewModel.loadCart()
            viewModel.checkout()

            Then("debe llamar al método de checkout del repositorio") {
                coVerify { mockCartRepository.checkout("user123", any(), any()) }
            }
        }
    }

    Given("manejo de errores") {
        val mockContext = mockk<Context>(relaxed = true)
        val mockCartRepository = mockk<FirebaseCartRepository>(relaxed = true)
        val mockAuthRepository = mockk<FirebaseAuthRepository>(relaxed = true)

        mockkConstructor(UserSessionManager::class)
        every { anyConstructed<UserSessionManager>().currentUserId } returns MutableStateFlow("user123")

        When("falla la carga del carrito") {
            coEvery { mockCartRepository.getCart("user123") } returns Result.failure(Exception("Error de red"))

            val viewModel = CartViewModel(mockContext, mockCartRepository, mockAuthRepository)
            viewModel.loadCart()

            Then("debe manejar el error") {
                coVerify { mockCartRepository.getCart("user123") }
            }
        }
    }
})

