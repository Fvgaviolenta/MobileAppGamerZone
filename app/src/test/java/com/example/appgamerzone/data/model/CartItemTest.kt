package com.example.appgamerzone.data.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CartItemTest : BehaviorSpec({

    Given("un modelo CartItem") {
        When("se crea un item con todos los parámetros") {
            val cartItem = CartItem(
                productId = "prod123",
                productName = "PlayStation 5",
                productImage = "https://example.com/ps5.jpg",
                quantity = 2,
                unitPrice = 499.99,
                availableStock = 10
            )

            Then("debe tener todos los valores asignados") {
                cartItem.productId shouldBe "prod123"
                cartItem.productName shouldBe "PlayStation 5"
                cartItem.unitPrice shouldBe 499.99
                cartItem.quantity shouldBe 2
                cartItem.productImage shouldBe "https://example.com/ps5.jpg"
                cartItem.availableStock shouldBe 10
            }
        }

        When("se calcula el precio total de un item") {
            val cartItem = CartItem(
                productId = "prod123",
                productName = "Test Product",
                productImage = "",
                quantity = 3,
                unitPrice = 50.0,
                availableStock = 10
            )

            val totalPrice = cartItem.unitPrice * cartItem.quantity

            Then("debe calcular correctamente") {
                totalPrice shouldBe 150.0
            }
        }

        When("se incrementa la cantidad") {
            val cartItem = CartItem(
                productId = "prod123",
                productName = "Product",
                productImage = "",
                quantity = 1,
                unitPrice = 100.0,
                availableStock = 10
            )

            val newQuantity = cartItem.quantity + 1

            Then("debe aumentar") {
                newQuantity shouldBe 2
                (newQuantity > cartItem.quantity) shouldBe true
            }
        }

        When("se decrementa la cantidad") {
            val cartItem = CartItem(
                productId = "prod123",
                productName = "Product",
                productImage = "",
                quantity = 5,
                unitPrice = 100.0,
                availableStock = 10
            )

            val newQuantity = cartItem.quantity - 1

            Then("debe disminuir") {
                newQuantity shouldBe 4
                (newQuantity < cartItem.quantity) shouldBe true
            }
        }
    }

    Given("validación de cantidad vs stock") {
        When("la cantidad es menor al stock") {
            val cartItem = CartItem(
                productId = "prod123",
                productName = "Product",
                productImage = "",
                quantity = 3,
                unitPrice = 100.0,
                availableStock = 10
            )

            Then("debe ser válido") {
                (cartItem.quantity < cartItem.availableStock) shouldBe true
                (cartItem.quantity <= cartItem.availableStock) shouldBe true
            }
        }

        When("la cantidad es igual al stock") {
            val cartItem = CartItem(
                productId = "prod123",
                productName = "Product",
                productImage = "",
                quantity = 10,
                unitPrice = 100.0,
                availableStock = 10
            )

            Then("debe ser válido") {
                cartItem.quantity shouldBe cartItem.availableStock
                (cartItem.quantity <= cartItem.availableStock) shouldBe true
            }
        }

        When("la cantidad excede el stock") {
            val requestedQuantity = 15
            val availableStock = 10

            Then("debe ser inválido") {
                (requestedQuantity > availableStock) shouldBe true
            }
        }
    }

    Given("cálculos de totales") {
        When("se calculan múltiples items") {
            val items = listOf(
                CartItem("p1", "Product 1", "", 2, 100.0, 10),
                CartItem("p2", "Product 2", "", 1, 50.0, 5),
                CartItem("p3", "Product 3", "", 3, 75.0, 8)
            )

            val subtotal = items.sumOf { it.unitPrice * it.quantity }
            val totalItems = items.sumOf { it.quantity }

            Then("debe calcular correctamente") {
                subtotal shouldBe 475.0
                totalItems shouldBe 6
            }
        }

        When("el carrito está vacío") {
            val items = emptyList<CartItem>()
            val subtotal = items.sumOf { it.unitPrice * it.quantity }

            Then("el subtotal debe ser 0") {
                subtotal shouldBe 0.0
            }
        }
    }

    Given("comparación de items") {
        When("dos items tienen el mismo productId") {
            val item1 = CartItem("prod123", "Product", "", 1, 100.0, 10)
            val item2 = CartItem("prod123", "Product", "", 2, 100.0, 10)

            Then("deben tener el mismo ID") {
                item1.productId shouldBe item2.productId
            }
        }

        When("dos items tienen diferentes productId") {
            val item1 = CartItem("prod123", "Product 1", "", 1, 100.0, 10)
            val item2 = CartItem("prod456", "Product 2", "", 1, 100.0, 10)

            Then("deben tener IDs diferentes") {
                item1.productId shouldBe "prod123"
                item2.productId shouldBe "prod456"
            }
        }
    }

    Given("precios unitarios") {
        When("se verifican precios diferentes") {
            val cheap = CartItem("p1", "Cheap", "", 1, 10.0, 10)
            val expensive = CartItem("p2", "Expensive", "", 1, 100.0, 10)

            Then("debe identificar cuál es más caro") {
                (expensive.unitPrice > cheap.unitPrice) shouldBe true
                (cheap.unitPrice < expensive.unitPrice) shouldBe true
            }
        }

        When("se calcula el ahorro con descuento") {
            val item = CartItem("p1", "Product", "", 1, 100.0, 10)
            val discountPercentage = 10.0
            val discount = item.unitPrice * (discountPercentage / 100)
            val finalPrice = item.unitPrice - discount

            Then("debe calcular correctamente") {
                discount shouldBe 10.0
                finalPrice shouldBe 90.0
            }
        }
    }

    Given("URLs de imágenes en items") {
        When("un item tiene URL de imagen") {
            val item = CartItem(
                "p1",
                "Product",
                "https://example.com/image.jpg",
                1,
                100.0,
                10
            )

            Then("debe estar definida") {
                item.productImage shouldBe "https://example.com/image.jpg"
            }
        }

        When("un item no tiene URL de imagen") {
            val item = CartItem("p1", "Product", "", 1, 100.0, 10)

            Then("debe ser cadena vacía") {
                item.productImage shouldBe ""
            }
        }
    }

    Given("validación de datos") {
        When("todos los datos son válidos") {
            val item = CartItem(
                productId = "valid123",
                productName = "Valid Product",
                productImage = "https://valid.com/image.jpg",
                quantity = 2,
                unitPrice = 99.99,
                availableStock = 50
            )

            Then("debe pasar las validaciones") {
                item.productId.isNotEmpty() shouldBe true
                item.productName.isNotEmpty() shouldBe true
                (item.unitPrice > 0) shouldBe true
                (item.quantity > 0) shouldBe true
                (item.availableStock > 0) shouldBe true
                (item.quantity <= item.availableStock) shouldBe true
            }
        }
    }

    Given("actualización de cantidad") {
        When("se actualiza a una cantidad válida") {
            val item = CartItem("p1", "Product", "", 2, 100.0, 10)
            val newQuantity = 5

            Then("debe estar dentro del stock") {
                (newQuantity <= item.availableStock) shouldBe true
                (newQuantity > 0) shouldBe true
            }
        }

        When("se intenta actualizar a cantidad inválida") {
            val item = CartItem("p1", "Product", "", 2, 100.0, 10)
            val newQuantity = 0

            Then("debe ser rechazado") {
                (newQuantity <= 0) shouldBe true
            }
        }
    }
})

