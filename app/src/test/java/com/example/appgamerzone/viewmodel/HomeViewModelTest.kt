package com.example.appgamerzone.viewmodel

import com.example.appgamerzone.data.model.Product
import com.example.appgamerzone.data.model.ProductCategory
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BehaviorSpec({
    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("un HomeViewModel inicializado") {
        val viewModel = HomeViewModel()

        When("se inicializa el ViewModel") {
            Then("el estado inicial debe tener valores por defecto") {
                viewModel.uiState.value.featuredProducts.size shouldBe 0
                viewModel.uiState.value.categories.size shouldBe 0
                viewModel.uiState.value.isLoading shouldBe true
                viewModel.uiState.value.userPoints shouldBe 0
            }
        }

        When("se cargan productos destacados") {
            viewModel.loadFeaturedProducts()

            Then("debe mostrar productos destacados") {
                viewModel.uiState.value.featuredProducts.shouldNotBeEmpty()
                viewModel.uiState.value.isLoading shouldBe false
            }
        }

        When("se cargan categorías") {
            viewModel.loadCategories()

            Then("debe mostrar categorías") {
                viewModel.uiState.value.categories.shouldNotBeEmpty()
            }
        }
    }

    Given("productos destacados") {
        val viewModel = HomeViewModel()

        When("se obtienen productos destacados de ejemplo") {
            viewModel.loadFeaturedProducts()

            Then("debe incluir productos populares") {
                val products = viewModel.uiState.value.featuredProducts
                products.shouldNotBeEmpty()

                // Verificar que hay al menos un producto
                products.size shouldBe 2

                // Verificar que los productos tienen datos válidos
                products.forEach { product ->
                    product.name.isNotEmpty() shouldBe true
                    (product.price > 0) shouldBe true
                    product.category.isNotEmpty() shouldBe true
                }
            }
        }
    }

    Given("categorías de productos") {
        val viewModel = HomeViewModel()

        When("se obtienen categorías de ejemplo") {
            viewModel.loadCategories()

            Then("debe incluir las categorías principales") {
                val categories = viewModel.uiState.value.categories
                categories.shouldNotBeEmpty()

                // Verificar que hay categorías
                categories shouldHaveSize 4

                // Verificar estructura de categorías
                categories.forEach { category ->
                    category.name.isNotEmpty() shouldBe true
                    category.icon.isNotEmpty() shouldBe true
                }
            }
        }

        When("se verifican las categorías específicas") {
            viewModel.loadCategories()
            val categories = viewModel.uiState.value.categories

            Then("debe incluir Consolas, Computadores, Accesorios y Sillas") {
                categories.any { it.name == "Consolas" } shouldBe true
                categories.any { it.name == "Computadores" } shouldBe true
                categories.any { it.name == "Accesorios" } shouldBe true
                categories.any { it.name == "Sillas" } shouldBe true
            }
        }
    }

    Given("estado de UI") {
        val viewModel = HomeViewModel()

        When("se está cargando") {
            Then("isLoading debe ser true inicialmente") {
                viewModel.uiState.value.isLoading shouldBe true
            }
        }

        When("termina de cargar productos") {
            viewModel.loadFeaturedProducts()

            Then("isLoading debe ser false") {
                viewModel.uiState.value.isLoading shouldBe false
            }
        }
    }

    Given("puntos del usuario") {
        val viewModel = HomeViewModel()

        When("se inicializa") {
            Then("los puntos deben ser 0 por defecto") {
                viewModel.uiState.value.userPoints shouldBe 0
            }
        }
    }

    Given("HomeUiState") {
        When("se crea un estado por defecto") {
            val state = HomeUiState()

            Then("debe tener valores iniciales correctos") {
                state.featuredProducts.size shouldBe 0
                state.categories.size shouldBe 0
                state.isLoading shouldBe true
                state.userPoints shouldBe 0
            }
        }

        When("se crea un estado con datos") {
            val products = listOf(
                Product(id = "1", name = "Product 1", price = 100.0, category = "Cat1", stock = 10)
            )
            val categories = listOf(
                ProductCategory("Consolas", "🎮")
            )

            val state = HomeUiState(
                featuredProducts = products,
                categories = categories,
                isLoading = false,
                userPoints = 100
            )

            Then("debe contener los datos correctos") {
                state.featuredProducts shouldHaveSize 1
                state.categories shouldHaveSize 1
                state.isLoading shouldBe false
                state.userPoints shouldBe 100
            }
        }
    }

    Given("validación de productos destacados") {
        val viewModel = HomeViewModel()
        viewModel.loadFeaturedProducts()

        When("se verifican los productos") {
            val products = viewModel.uiState.value.featuredProducts

            Then("deben tener IDs únicos") {
                val ids = products.map { it.id }
                ids.distinct().size shouldBe products.size
            }

            Then("deben tener nombres válidos") {
                products.all { it.name.isNotBlank() } shouldBe true
            }

            Then("deben tener precios válidos") {
                products.all { it.price > 0 } shouldBe true
            }

            Then("deben tener categorías asignadas") {
                products.all { it.category.isNotBlank() } shouldBe true
            }
        }
    }

    Given("validación de categorías") {
        val viewModel = HomeViewModel()
        viewModel.loadCategories()

        When("se verifican las categorías") {
            val categories = viewModel.uiState.value.categories

            Then("deben tener nombres únicos") {
                val names = categories.map { it.name }
                names.distinct().size shouldBe categories.size
            }

            Then("deben tener iconos asignados") {
                categories.all { it.icon.isNotBlank() } shouldBe true
            }
        }
    }

    Given("ProductCategory") {
        When("se crea una categoría") {
            val category = ProductCategory("Consolas", "🎮")

            Then("debe tener los valores correctos") {
                category.name shouldBe "Consolas"
                category.icon shouldBe "🎮"
            }
        }

        When("se comparan categorías") {
            val category1 = ProductCategory("Consolas", "🎮")
            val category2 = ProductCategory("Juegos", "🎯")

            Then("deben ser diferentes") {
                category1.name shouldBe "Consolas"
                category2.name shouldBe "Juegos"
                category1.name != category2.name shouldBe true
            }
        }
    }

    Given("carga asíncrona") {
        val viewModel = HomeViewModel()

        When("se cargan productos y categorías simultáneamente") {
            viewModel.loadFeaturedProducts()
            viewModel.loadCategories()

            Then("ambos deben cargarse correctamente") {
                viewModel.uiState.value.featuredProducts.shouldNotBeEmpty()
                viewModel.uiState.value.categories.shouldNotBeEmpty()
            }
        }
    }

    Given("actualización de estado") {
        val viewModel = HomeViewModel()

        When("se actualiza el estado múltiples veces") {
            viewModel.loadCategories()
            val firstCategoriesCount = viewModel.uiState.value.categories.size

            viewModel.loadFeaturedProducts()
            val productsCount = viewModel.uiState.value.featuredProducts.size

            Then("debe mantener consistencia") {
                viewModel.uiState.value.categories.size shouldBe firstCategoriesCount
                viewModel.uiState.value.featuredProducts.size shouldBe productsCount
            }
        }
    }
})

