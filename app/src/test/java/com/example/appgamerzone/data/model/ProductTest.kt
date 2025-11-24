package com.example.appgamerzone.data.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ProductTest : BehaviorSpec({

    Given("un modelo Product") {
        When("se crea un producto con constructor completo") {
            val product = Product(
                id = "prod123",
                name = "PlayStation 5",
                price = 499.99,
                category = "Consolas",
                description = "Consola de última generación",
                imageUrl = "https://example.com/ps5.jpg",
                stock = 10,
                rating = 4.8,
                reviewCount = 1500
            )

            Then("debe tener todos los valores asignados") {
                product.id shouldBe "prod123"
                product.name shouldBe "PlayStation 5"
                product.price shouldBe 499.99
                product.category shouldBe "Consolas"
                product.description shouldBe "Consola de última generación"
                product.imageUrl shouldBe "https://example.com/ps5.jpg"
                product.stock shouldBe 10
                product.rating shouldBe 4.8
                product.reviewCount shouldBe 1500
            }
        }

        When("se crea un producto con constructor por defecto") {
            val product = Product()

            Then("debe tener valores por defecto") {
                product.id shouldBe ""
                product.name shouldBe ""
                product.price shouldBe 0.0
                product.category shouldBe ""
                product.stock shouldBe 0
                product.rating shouldBe 0.0
                product.reviewCount shouldBe 0
            }
        }

        When("se convierte un Product a Map") {
            val product = Product(
                id = "prod123",
                name = "Test Product",
                price = 99.99,
                category = "Test Category",
                description = "Test Description",
                imageUrl = "https://test.com/image.jpg",
                stock = 50,
                rating = 4.5,
                reviewCount = 100
            )

            val map = product.toMap()

            Then("debe contener todos los campos") {
                map["id"] shouldBe "prod123"
                map["name"] shouldBe "Test Product"
                map["price"] shouldBe 99.99
                map["category"] shouldBe "Test Category"
                map["description"] shouldBe "Test Description"
                map["imageUrl"] shouldBe "https://test.com/image.jpg"
                map["stock"] shouldBe 50
                map["rating"] shouldBe 4.5
                map["reviewCount"] shouldBe 100
            }
        }

        When("se crea un Product desde un Map con números Long") {
            val map = mapOf(
                "id" to "prod123",
                "name" to "Test Product",
                "price" to 99.99,
                "category" to "Test",
                "description" to "Description",
                "imageUrl" to "url",
                "stock" to 50L,
                "rating" to 4.5,
                "reviewCount" to 100L
            )

            val product = Product.fromMap("prod123", map)

            Then("debe manejar la conversión correctamente") {
                product.id shouldBe "prod123"
                product.name shouldBe "Test Product"
                product.price shouldBe 99.99
                product.stock shouldBe 50
                product.reviewCount shouldBe 100
            }
        }

        When("se crea un Product desde un Map con números Int") {
            val map = mapOf(
                "id" to "prod123",
                "name" to "Test Product",
                "price" to 99,
                "category" to "Test",
                "description" to "Description",
                "imageUrl" to "url",
                "stock" to 50,
                "rating" to 4.5,
                "reviewCount" to 100
            )

            val product = Product.fromMap("prod123", map)

            Then("debe manejar la conversión correctamente") {
                product.stock shouldBe 50
                product.reviewCount shouldBe 100
            }
        }

        When("se copian valores con copy()") {
            val original = Product(
                id = "prod123",
                name = "Original Name",
                price = 100.0,
                category = "Original",
                stock = 10
            )

            val modified = original.copy(
                name = "Modified Name",
                price = 150.0,
                stock = 20
            )

            Then("debe crear una nueva instancia con cambios") {
                modified.id shouldBe original.id
                modified.name shouldBe "Modified Name"
                modified.price shouldBe 150.0
                modified.stock shouldBe 20
                original.name shouldBe "Original Name"
                original.price shouldBe 100.0
            }
        }
    }

    Given("validación de stock") {
        When("un producto tiene stock") {
            val product = Product(
                name = "Product",
                price = 100.0,
                category = "Test",
                stock = 10
            )

            Then("debe ser mayor a 0") {
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

            Then("debe ser 0") {
                product.stock shouldBe 0
            }
        }

        When("se reduce el stock") {
            val product = Product(
                name = "Product",
                price = 100.0,
                category = "Test",
                stock = 10
            )

            val newStock = product.stock - 3

            Then("debe disminuir correctamente") {
                newStock shouldBe 7
                (newStock < product.stock) shouldBe true
            }
        }
    }

    Given("validación de precios") {
        When("un producto tiene precio válido") {
            val product = Product(
                name = "Product",
                price = 99.99,
                category = "Test",
                stock = 10
            )

            Then("debe ser mayor a 0") {
                (product.price > 0) shouldBe true
                product.price shouldBe 99.99
            }
        }

        When("se comparan precios") {
            val cheap = Product(name = "Cheap", price = 10.0, category = "Test", stock = 10)
            val expensive = Product(name = "Expensive", price = 100.0, category = "Test", stock = 10)

            Then("debe identificar cuál es más caro") {
                (expensive.price > cheap.price) shouldBe true
            }
        }
    }

    Given("rating y reseñas") {
        When("un producto tiene rating alto") {
            val product = Product(
                name = "High Rated",
                price = 100.0,
                category = "Test",
                stock = 10,
                rating = 4.8,
                reviewCount = 500
            )

            Then("debe tener valores correctos") {
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

        When("se calcula el promedio de rating") {
            val product1 = Product(name = "P1", price = 10.0, category = "T", stock = 5, rating = 4.5, reviewCount = 10)
            val product2 = Product(name = "P2", price = 10.0, category = "T", stock = 5, rating = 4.0, reviewCount = 5)

            val avgRating = (product1.rating * product1.reviewCount + product2.rating * product2.reviewCount) /
                           (product1.reviewCount + product2.reviewCount)

            Then("debe calcular correctamente") {
                avgRating shouldBe 4.333333333333333
            }
        }
    }

    Given("categorías de productos") {
        When("se asignan categorías") {
            val consola = Product(name = "PS5", price = 499.0, category = "Consolas", stock = 10)
            val juego = Product(name = "Game", price = 59.0, category = "Juegos", stock = 20)
            val accesorio = Product(name = "Control", price = 69.0, category = "Accesorios", stock = 15)

            Then("cada uno debe tener su categoría") {
                consola.category shouldBe "Consolas"
                juego.category shouldBe "Juegos"
                accesorio.category shouldBe "Accesorios"
            }
        }

        When("se filtran por categoría") {
            val products = listOf(
                Product(name = "P1", price = 10.0, category = "Consolas", stock = 5),
                Product(name = "P2", price = 20.0, category = "Juegos", stock = 10),
                Product(name = "P3", price = 30.0, category = "Consolas", stock = 15)
            )

            val consolas = products.filter { it.category == "Consolas" }

            Then("debe filtrar correctamente") {
                consolas.size shouldBe 2
                consolas.all { it.category == "Consolas" } shouldBe true
            }
        }
    }

    Given("URLs de imágenes") {
        When("un producto tiene URL de imagen") {
            val product = Product(
                name = "Product",
                price = 100.0,
                category = "Test",
                imageUrl = "https://example.com/image.jpg",
                stock = 10
            )

            Then("debe estar definida") {
                product.imageUrl shouldNotBe ""
                product.imageUrl shouldBe "https://example.com/image.jpg"
            }
        }

        When("un producto no tiene URL de imagen") {
            val product = Product(
                name = "No Image",
                price = 100.0,
                category = "Test",
                imageUrl = "",
                stock = 10
            )

            Then("debe ser cadena vacía") {
                product.imageUrl shouldBe ""
            }
        }
    }

    Given("descripción de productos") {
        When("un producto tiene descripción") {
            val product = Product(
                name = "Product",
                price = 100.0,
                category = "Test",
                description = "Una descripción detallada del producto",
                stock = 10
            )

            Then("debe estar definida") {
                product.description shouldNotBe ""
                product.description shouldBe "Una descripción detallada del producto"
            }
        }

        When("un producto no tiene descripción") {
            val product = Product(
                name = "No Description",
                price = 100.0,
                category = "Test",
                description = "",
                stock = 10
            )

            Then("debe ser cadena vacía") {
                product.description shouldBe ""
            }
        }
    }
})

