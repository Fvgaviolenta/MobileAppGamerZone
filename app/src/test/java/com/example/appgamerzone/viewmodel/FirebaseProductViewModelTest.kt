package com.example.appgamerzone.viewmodel

import com.example.appgamerzone.data.model.Product
import com.example.appgamerzone.data.repository.FirebaseProductRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseProductViewModelTest : BehaviorSpec({
    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("un ProductViewModel inicializado") {
        val mockRepository = mockk<FirebaseProductRepository>()

        val testProducts = listOf(
            Product(
                id = "1",
                name = "PlayStation 5",
                price = 499.99,
                category = "Consolas",
                description = "Consola de última generación",
                imageUrl = "url1",
                stock = 10,
                rating = 4.5,
                reviewCount = 100
            ),
            Product(
                id = "2",
                name = "The Last of Us Part II",
                price = 59.99,
                category = "Juegos",
                description = "Juego de acción y aventura",
                imageUrl = "url2",
                stock = 50,
                rating = 4.8,
                reviewCount = 200
            ),
            Product(
                id = "3",
                name = "Xbox Series X",
                price = 499.99,
                category = "Consolas",
                description = "Consola de Microsoft",
                imageUrl = "url3",
                stock = 8,
                rating = 4.6,
                reviewCount = 150
            )
        )

        coEvery { mockRepository.getAllProducts() } returns Result.success(testProducts)
        coEvery { mockRepository.getCategories() } returns Result.success(listOf("Consolas", "Juegos", "Accesorios"))

        val viewModel = FirebaseProductViewModel(mockRepository)

        When("se inicializa el ViewModel") {
            Then("debe cargar todos los productos") {
                viewModel.uiState.value.products shouldHaveSize 3
                viewModel.uiState.value.filteredProducts shouldHaveSize 3
            }

            Then("debe cargar las categorías") {
                viewModel.uiState.value.categories shouldHaveSize 3
                viewModel.uiState.value.categories shouldContain "Consolas"
                viewModel.uiState.value.categories shouldContain "Juegos"
            }
        }

        When("se selecciona una categoría") {
            viewModel.selectCategory("Consolas")

            Then("debe filtrar los productos por esa categoría") {
                viewModel.uiState.value.selectedCategory shouldBe "Consolas"
                viewModel.uiState.value.filteredProducts shouldHaveSize 2
                viewModel.uiState.value.filteredProducts.all { it.category == "Consolas" } shouldBe true
            }
        }

        When("se actualiza la búsqueda") {
            viewModel.updateSearchQuery("PlayStation")

            Then("debe filtrar productos por nombre") {
                viewModel.uiState.value.searchQuery shouldBe "PlayStation"
                viewModel.uiState.value.filteredProducts.any { it.name.contains("PlayStation") } shouldBe true
            }
        }

        When("se busca con texto vacío") {
            viewModel.updateSearchQuery("")

            Then("debe mostrar todos los productos") {
                viewModel.uiState.value.filteredProducts shouldHaveSize 3
            }
        }

        When("se combina búsqueda y filtro de categoría") {
            viewModel.selectCategory("Consolas")
            viewModel.updateSearchQuery("Xbox")

            Then("debe aplicar ambos filtros") {
                viewModel.uiState.value.filteredProducts shouldHaveSize 1
                viewModel.uiState.value.filteredProducts.first().name shouldBe "Xbox Series X"
            }
        }

        When("se limpia el filtro de categoría") {
            viewModel.selectCategory("Consolas")
            viewModel.selectCategory(null)

            Then("debe mostrar todos los productos") {
                viewModel.uiState.value.selectedCategory shouldBe null
                viewModel.uiState.value.filteredProducts shouldHaveSize 3
            }
        }
    }

    Given("carga de productos con error") {
        val mockRepository = mockk<FirebaseProductRepository>()

        coEvery { mockRepository.getAllProducts() } returns Result.failure(Exception("Error de conexión"))
        coEvery { mockRepository.getCategories() } returns Result.success(emptyList())

        val viewModel = FirebaseProductViewModel(mockRepository)

        When("falla la carga de productos") {
            Then("debe establecer el estado de error") {
                viewModel.uiState.value.error shouldBe "Error de conexión"
                viewModel.uiState.value.products.shouldBeEmpty()
            }
        }
    }

    Given("búsqueda de productos") {
        val mockRepository = mockk<FirebaseProductRepository>()

        val testProducts = listOf(
            Product(id = "1", name = "Super Mario Bros", price = 49.99, category = "Juegos", stock = 20),
            Product(id = "2", name = "Mario Kart 8", price = 59.99, category = "Juegos", stock = 15),
            Product(id = "3", name = "The Legend of Zelda", price = 59.99, category = "Juegos", stock = 10)
        )

        coEvery { mockRepository.getAllProducts() } returns Result.success(testProducts)
        coEvery { mockRepository.getCategories() } returns Result.success(listOf("Juegos"))

        val viewModel = FirebaseProductViewModel(mockRepository)

        When("se busca 'Mario'") {
            viewModel.updateSearchQuery("Mario")

            Then("debe encontrar productos con Mario en el nombre") {
                viewModel.uiState.value.filteredProducts shouldHaveSize 2
                viewModel.uiState.value.filteredProducts.all { it.name.contains("Mario") } shouldBe true
            }
        }

        When("se busca con minúsculas") {
            viewModel.updateSearchQuery("mario")

            Then("debe ser case-insensitive") {
                viewModel.uiState.value.filteredProducts shouldHaveSize 2
            }
        }

        When("se busca algo que no existe") {
            viewModel.updateSearchQuery("Pokemon")

            Then("no debe encontrar resultados") {
                viewModel.uiState.value.filteredProducts.shouldBeEmpty()
            }
        }
    }

    Given("recarga de productos") {
        val mockRepository = mockk<FirebaseProductRepository>()

        val initialProducts = listOf(
            Product(id = "1", name = "Producto 1", price = 100.0, category = "Cat1", stock = 10)
        )

        val updatedProducts = listOf(
            Product(id = "1", name = "Producto 1", price = 100.0, category = "Cat1", stock = 10),
            Product(id = "2", name = "Producto 2", price = 200.0, category = "Cat2", stock = 5)
        )

        coEvery { mockRepository.getAllProducts() } returnsMany listOf(
            Result.success(initialProducts),
            Result.success(updatedProducts)
        )
        coEvery { mockRepository.getCategories() } returns Result.success(listOf("Cat1", "Cat2"))

        val viewModel = FirebaseProductViewModel(mockRepository)

        When("se recarga la lista de productos") {
            viewModel.loadProducts()

            Then("debe actualizar con los nuevos productos") {
                viewModel.uiState.value.products shouldHaveSize 2
            }
        }
    }

    Given("estado de carga") {
        val mockRepository = mockk<FirebaseProductRepository>()

        coEvery { mockRepository.getAllProducts() } returns Result.success(emptyList())
        coEvery { mockRepository.getCategories() } returns Result.success(emptyList())

        val viewModel = FirebaseProductViewModel(mockRepository)

        When("se están cargando productos") {
            Then("isLoading debe ser false después de cargar") {
                viewModel.uiState.value.isLoading shouldBe false
            }
        }
    }
})

