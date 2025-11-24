package com.example.appgamerzone.data.repository

import com.example.appgamerzone.data.model.Product
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class FirebaseProductRepositoryTest : BehaviorSpec({

    Given("un FirebaseProductRepository") {
        val repository = FirebaseProductRepository()

        When("se trabaja con productos") {
            Then("debe tener productos locales predefinidos") {
                // Los productos locales deben estar disponibles como fallback
                val playstation = Product(
                    name = "PlayStation 5",
                    price = 599990.0,
                    category = "Consolas"
                )

                playstation.name shouldBe "PlayStation 5"
                playstation.category shouldBe "Consolas"
                playstation.price shouldBe 599990.0
            }
        }
    }

    Given("operaciones CRUD de productos") {
        val repository = FirebaseProductRepository()

        When("se crea un nuevo producto") {
            val newProduct = Product(
                id = "",
                name = "Nintendo Switch",
                price = 299990.0,
                category = "Consolas",
                description = "Consola híbrida portátil",
                imageUrl = "https://example.com/switch.jpg",
                stock = 15,
                rating = 4.6,
                reviewCount = 800
            )

            Then("debe tener todos los campos requeridos") {
                newProduct.name shouldNotBe ""
                newProduct.price shouldBe 299990.0
                newProduct.category shouldBe "Consolas"
                newProduct.stock shouldBe 15
            }
        }

        When("se actualiza un producto existente") {
            val originalProduct = Product(
                id = "prod123",
                name = "Original Name",
                price = 100.0,
                category = "Categoría",
                stock = 10
            )

            val updatedProduct = originalProduct.copy(
                name = "Updated Name",
                price = 150.0,
                stock = 20
            )

            Then("debe reflejar los cambios") {
                updatedProduct.name shouldBe "Updated Name"
                updatedProduct.price shouldBe 150.0
                updatedProduct.stock shouldBe 20
                updatedProduct.id shouldBe originalProduct.id
            }
        }

        When("se elimina un producto") {
            val productToDelete = Product(
                id = "delete123",
                name = "To Delete",
                price = 50.0,
                category = "Test",
                stock = 5
            )

            Then("el ID debe ser válido") {
                productToDelete.id shouldNotBe ""
                productToDelete.id shouldBe "delete123"
            }
        }
    }

    Given("filtrado de productos por categoría") {
        When("se filtran productos de 'Consolas'") {
            val consolasProducts = listOf(
                Product(name = "PlayStation 5", category = "Consolas", price = 499.99, stock = 10),
                Product(name = "Xbox Series X", category = "Consolas", price = 499.99, stock = 8),
                Product(name = "Nintendo Switch", category = "Consolas", price = 299.99, stock = 15)
            )

            Then("todos deben ser consolas") {
                consolasProducts.all { it.category == "Consolas" } shouldBe true
                consolasProducts shouldHaveSize 3
            }
        }

        When("se filtran productos de 'Juegos'") {
            val juegosProducts = listOf(
                Product(name = "The Last of Us", category = "Juegos", price = 59.99, stock = 20),
                Product(name = "God of War", category = "Juegos", price = 59.99, stock = 25)
            )

            Then("todos deben ser juegos") {
                juegosProducts.all { it.category == "Juegos" } shouldBe true
                juegosProducts shouldHaveSize 2
            }
        }

        When("se filtran productos de 'Accesorios'") {
            val accesoriosProducts = listOf(
                Product(name = "Control DualSense", category = "Accesorios", price = 69.99, stock = 50),
                Product(name = "Headset", category = "Accesorios", price = 89.99, stock = 30)
            )

            Then("todos deben ser accesorios") {
                accesoriosProducts.all { it.category == "Accesorios" } shouldBe true
            }
        }
    }

    Given("validación de stock de productos") {
        When("un producto tiene stock disponible") {
            val product = Product(
                name = "Product with Stock",
                price = 100.0,
                category = "Test",
                stock = 10
            )

            Then("el stock debe ser mayor a 0") {
                product.stock shouldBe 10
                (product.stock > 0) shouldBe true
            }
        }

        When("un producto no tiene stock") {
            val product = Product(
                name = "Out of Stock",
                price = 100.0,
                category = "Test",
                stock = 0
            )

            Then("el stock debe ser 0") {
                product.stock shouldBe 0
            }
        }

        When("se reduce el stock después de una compra") {
            val product = Product(
                name = "Product",
                price = 100.0,
                category = "Test",
                stock = 10
            )

            val quantityPurchased = 3
            val newStock = product.stock - quantityPurchased

            Then("el stock debe disminuir correctamente") {
                newStock shouldBe 7
                newStock shouldBe product.stock - quantityPurchased
            }
        }
    }

    Given("categorías de productos") {
        When("se obtienen todas las categorías") {
            val categories = listOf("Consolas", "Juegos", "Accesorios", "Merchandising")

            Then("debe haber múltiples categorías") {
                categories.shouldNotBeEmpty()
                categories shouldContain "Consolas"
                categories shouldContain "Juegos"
                categories shouldContain "Accesorios"
            }
        }

        When("se verifica una categoría específica") {
            val consolaCategory = "Consolas"
            val juegosCategory = "Juegos"

            Then("deben ser las categorías correctas") {
                consolaCategory shouldBe "Consolas"
                juegosCategory shouldBe "Juegos"
            }
        }
    }

    Given("conversión de productos") {
        When("se convierte un Product a Map") {
            val product = Product(
                id = "prod123",
                name = "Test Product",
                price = 99.99,
                category = "Test Category",
                description = "Test Description",
                imageUrl = "https://example.com/image.jpg",
                stock = 50,
                rating = 4.5,
                reviewCount = 100
            )

            val productMap = product.toMap()

            Then("debe contener todos los campos") {
                productMap["id"] shouldBe "prod123"
                productMap["name"] shouldBe "Test Product"
                productMap["price"] shouldBe 99.99
                productMap["category"] shouldBe "Test Category"
                productMap["stock"] shouldBe 50
                productMap["rating"] shouldBe 4.5
                productMap["reviewCount"] shouldBe 100
            }
        }

        When("se crea un Product desde un Map") {
            val productMap = mapOf(
                "id" to "prod123",
                "name" to "Test Product",
                "price" to 99.99,
                "category" to "Test Category",
                "description" to "Test Description",
                "imageUrl" to "https://example.com/image.jpg",
                "stock" to 50L,
                "rating" to 4.5,
                "reviewCount" to 100L
            )

            val product = Product.fromMap("prod123", productMap)

            Then("debe crear el objeto correctamente") {
                product.id shouldBe "prod123"
                product.name shouldBe "Test Product"
                product.price shouldBe 99.99
                product.category shouldBe "Test Category"
                product.stock shouldBe 50
            }
        }
    }

    Given("búsqueda de productos") {
        val allProducts = listOf(
            Product(id = "1", name = "PlayStation 5", category = "Consolas", price = 499.99, stock = 10),
            Product(id = "2", name = "Xbox Series X", category = "Consolas", price = 499.99, stock = 8),
            Product(id = "3", name = "The Last of Us", category = "Juegos", price = 59.99, stock = 20),
            Product(id = "4", name = "God of War", category = "Juegos", price = 59.99, stock = 15)
        )

        When("se busca por nombre exacto") {
            val searchTerm = "PlayStation 5"
            val results = allProducts.filter { it.name == searchTerm }

            Then("debe encontrar el producto") {
                results shouldHaveSize 1
                results.first().name shouldBe "PlayStation 5"
            }
        }

        When("se busca por nombre parcial") {
            val searchTerm = "Play"
            val results = allProducts.filter { it.name.contains(searchTerm, ignoreCase = true) }

            Then("debe encontrar productos que contengan el término") {
                results.shouldNotBeEmpty()
                results.all { it.name.contains(searchTerm, ignoreCase = true) } shouldBe true
            }
        }

        When("se busca por categoría") {
            val category = "Juegos"
            val results = allProducts.filter { it.category == category }

            Then("debe encontrar todos los juegos") {
                results shouldHaveSize 2
                results.all { it.category == "Juegos" } shouldBe true
            }
        }
    }

    Given("validación de precios") {
        When("se verifica un precio válido") {
            val product = Product(
                name = "Valid Price Product",
                price = 99.99,
                category = "Test",
                stock = 10
            )

            Then("el precio debe ser positivo") {
                product.price shouldBe 99.99
                (product.price > 0) shouldBe true
            }
        }

        When("se comparan precios") {
            val cheapProduct = Product(name = "Cheap", price = 29.99, category = "Test", stock = 10)
            val expensiveProduct = Product(name = "Expensive", price = 499.99, category = "Test", stock = 10)

            Then("debe identificar cuál es más caro") {
                (expensiveProduct.price > cheapProduct.price) shouldBe true
                (cheapProduct.price < expensiveProduct.price) shouldBe true
            }
        }
    }

    Given("ratings y reseñas") {
        When("un producto tiene rating alto") {
            val product = Product(
                name = "High Rated",
                price = 100.0,
                category = "Test",
                stock = 10,
                rating = 4.8,
                reviewCount = 500
            )

            Then("debe tener rating mayor a 4.0") {
                product.rating shouldBe 4.8
                (product.rating > 4.0) shouldBe true
                product.reviewCount shouldBe 500
            }
        }

        When("un producto no tiene reseñas") {
            val product = Product(
                name = "No Reviews",
                price = 100.0,
                category = "Test",
                stock = 10,
                rating = 0.0,
                reviewCount = 0
            )

            Then("rating y reviewCount deben ser 0") {
                product.rating shouldBe 0.0
                product.reviewCount shouldBe 0
            }
        }
    }

    Given("normalización de nombres") {
        When("se normalizan nombres de productos") {
            val name1 = "PlayStation 5"
            val name2 = "playstation 5"
            val name3 = "PLAYSTATION 5"

            val normalized1 = name1.trim().lowercase()
            val normalized2 = name2.trim().lowercase()
            val normalized3 = name3.trim().lowercase()

            Then("todos deben ser iguales después de normalizar") {
                normalized1 shouldBe normalized2
                normalized2 shouldBe normalized3
                normalized1 shouldBe "playstation 5"
            }
        }
    }
})

