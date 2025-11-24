package com.example.appgamerzone.viewmodel

import com.example.appgamerzone.data.model.Product
import com.example.appgamerzone.data.repository.FirebaseProductRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest : BehaviorSpec({
    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("un AdminViewModel inicializado") {
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
                name = "Xbox Series X",
                price = 499.99,
                category = "Consolas",
                description = "Consola de Microsoft",
                imageUrl = "url2",
                stock = 8,
                rating = 4.6,
                reviewCount = 150
            )
        )

        coEvery { mockRepository.getAllProducts() } returns Result.success(testProducts)
        coEvery { mockRepository.getCategories() } returns Result.success(listOf("Consolas", "Juegos"))

        val viewModel = AdminViewModel(mockRepository)

        When("se inicializa el ViewModel") {
            Then("debe cargar todos los productos") {
                viewModel.uiState.value.products shouldHaveSize 2
            }

            Then("debe cargar las categorías") {
                viewModel.uiState.value.categories shouldHaveSize 2
            }
        }

        When("se selecciona un producto para editar") {
            val productToEdit = testProducts.first()
            viewModel.selectProduct(productToEdit)

            Then("debe establecer el producto seleccionado") {
                viewModel.uiState.value.selectedProduct shouldBe productToEdit
                viewModel.uiState.value.isEditing shouldBe true
            }
        }

        When("se limpia la selección") {
            viewModel.selectProduct(testProducts.first())
            viewModel.clearSelection()

            Then("debe limpiar el producto seleccionado") {
                viewModel.uiState.value.selectedProduct shouldBe null
                viewModel.uiState.value.isEditing shouldBe false
            }
        }
    }

    Given("creación de productos") {
        val mockRepository = mockk<FirebaseProductRepository>()

        coEvery { mockRepository.getAllProducts() } returns Result.success(emptyList())
        coEvery { mockRepository.getCategories() } returns Result.success(emptyList())

        val viewModel = AdminViewModel(mockRepository)

        When("se crea un nuevo producto") {
            val newProduct = Product(
                id = "new1",
                name = "Nuevo Producto",
                price = 299.99,
                category = "Accesorios",
                description = "Descripción del producto",
                imageUrl = "url",
                stock = 50
            )

            coEvery { mockRepository.createProduct(newProduct) } returns Result.success(newProduct)

            viewModel.createProduct(newProduct)

            Then("debe llamar al repositorio para crear el producto") {
                coVerify { mockRepository.createProduct(newProduct) }
            }

            Then("debe mostrar mensaje de éxito") {
                viewModel.uiState.value.successMessage shouldNotBe null
            }
        }
    }

    Given("actualización de productos") {
        val mockRepository = mockk<FirebaseProductRepository>()

        val existingProduct = Product(
            id = "1",
            name = "Producto Original",
            price = 100.0,
            category = "Categoría 1",
            stock = 10
        )

        coEvery { mockRepository.getAllProducts() } returns Result.success(listOf(existingProduct))
        coEvery { mockRepository.getCategories() } returns Result.success(listOf("Categoría 1"))

        val viewModel = AdminViewModel(mockRepository)

        When("se actualiza un producto existente") {
            val updatedProduct = existingProduct.copy(
                name = "Producto Actualizado",
                price = 150.0,
                stock = 20
            )

            coEvery { mockRepository.updateProduct(updatedProduct) } returns Result.success(updatedProduct)

            viewModel.updateProduct(updatedProduct)

            Then("debe llamar al repositorio para actualizar") {
                coVerify { mockRepository.updateProduct(updatedProduct) }
            }

            Then("debe mostrar mensaje de éxito") {
                viewModel.uiState.value.successMessage shouldNotBe null
            }
        }
    }

    Given("eliminación de productos") {
        val mockRepository = mockk<FirebaseProductRepository>()

        val productToDelete = Product(
            id = "delete1",
            name = "Producto a Eliminar",
            price = 50.0,
            category = "Test",
            stock = 5
        )

        coEvery { mockRepository.getAllProducts() } returns Result.success(listOf(productToDelete))
        coEvery { mockRepository.getCategories() } returns Result.success(listOf("Test"))
        coEvery { mockRepository.deleteProduct("delete1") } returns Result.success(Unit)

        val viewModel = AdminViewModel(mockRepository)

        When("se elimina un producto") {
            viewModel.deleteProduct("delete1")

            Then("debe llamar al repositorio para eliminar") {
                coVerify { mockRepository.deleteProduct("delete1") }
            }
        }
    }

    Given("manejo de errores en operaciones CRUD") {
        val mockRepository = mockk<FirebaseProductRepository>()

        coEvery { mockRepository.getAllProducts() } returns Result.success(emptyList())
        coEvery { mockRepository.getCategories() } returns Result.success(emptyList())

        val viewModel = AdminViewModel(mockRepository)

        When("falla la creación de un producto") {
            coEvery { mockRepository.createProduct(any()) } returns Result.failure(
                Exception("Error al crear producto")
            )

            val newProduct = Product(
                id = "fail1",
                name = "Producto Fallido",
                price = 100.0,
                category = "Test",
                stock = 10
            )

            viewModel.createProduct(newProduct)

            Then("debe establecer el mensaje de error") {
                viewModel.uiState.value.error shouldBe "Error al crear producto"
            }
        }

        When("falla la actualización de un producto") {
            coEvery { mockRepository.updateProduct(any()) } returns Result.failure(
                Exception("Error al actualizar")
            )

            val product = Product(
                id = "1",
                name = "Producto",
                price = 100.0,
                category = "Test",
                stock = 10
            )

            viewModel.updateProduct(product)

            Then("debe establecer el mensaje de error") {
                viewModel.uiState.value.error shouldBe "Error al actualizar"
            }
        }

        When("falla la eliminación de un producto") {
            coEvery { mockRepository.deleteProduct(any()) } returns Result.failure(
                Exception("Error al eliminar")
            )

            viewModel.deleteProduct("1")

            Then("debe establecer el mensaje de error") {
                viewModel.uiState.value.error shouldBe "Error al eliminar"
            }
        }
    }

    Given("validación de productos") {
        val mockRepository = mockk<FirebaseProductRepository>()

        coEvery { mockRepository.getAllProducts() } returns Result.success(emptyList())
        coEvery { mockRepository.getCategories() } returns Result.success(emptyList())

        val viewModel = AdminViewModel(mockRepository)

        When("se intenta crear un producto con datos válidos") {
            val validProduct = Product(
                id = "valid1",
                name = "Producto Válido",
                price = 99.99,
                category = "Categoría",
                description = "Descripción completa",
                imageUrl = "https://example.com/image.jpg",
                stock = 100
            )

            coEvery { mockRepository.createProduct(validProduct) } returns Result.success(validProduct)
            viewModel.createProduct(validProduct)

            Then("debe procesar correctamente") {
                coVerify { mockRepository.createProduct(validProduct) }
            }
        }
    }

    Given("recarga de productos después de operaciones") {
        val mockRepository = mockk<FirebaseProductRepository>()

        val initialProducts = listOf(
            Product(id = "1", name = "Producto 1", price = 100.0, category = "Cat1", stock = 10)
        )

        val updatedProducts = listOf(
            Product(id = "1", name = "Producto 1", price = 100.0, category = "Cat1", stock = 10),
            Product(id = "2", name = "Producto 2", price = 200.0, category = "Cat2", stock = 20)
        )

        coEvery { mockRepository.getAllProducts() } returnsMany listOf(
            Result.success(initialProducts),
            Result.success(updatedProducts)
        )
        coEvery { mockRepository.getCategories() } returns Result.success(listOf("Cat1", "Cat2"))

        val viewModel = AdminViewModel(mockRepository)

        When("se crea un producto y se recarga la lista") {
            val newProduct = Product(
                id = "2",
                name = "Producto 2",
                price = 200.0,
                category = "Cat2",
                stock = 20
            )

            coEvery { mockRepository.createProduct(newProduct) } returns Result.success(newProduct)
            viewModel.createProduct(newProduct)

            Then("debe actualizar la lista de productos") {
                coVerify(atLeast = 2) { mockRepository.getAllProducts() }
            }
        }
    }

    Given("estado de carga durante operaciones") {
        val mockRepository = mockk<FirebaseProductRepository>()

        coEvery { mockRepository.getAllProducts() } returns Result.success(emptyList())
        coEvery { mockRepository.getCategories() } returns Result.success(emptyList())

        val viewModel = AdminViewModel(mockRepository)

        When("se realiza una operación") {
            val product = Product(
                id = "test",
                name = "Test",
                price = 100.0,
                category = "Test",
                stock = 10
            )

            coEvery { mockRepository.createProduct(product) } returns Result.success(product)
            viewModel.createProduct(product)

            Then("el estado de carga debe ser false después de completar") {
                viewModel.uiState.value.isLoading shouldBe false
            }
        }
    }
})

