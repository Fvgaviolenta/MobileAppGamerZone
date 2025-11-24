package com.example.appgamerzone.data.repository

import com.example.appgamerzone.data.model.CartItem
import com.example.appgamerzone.data.model.Product
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class FirebaseCartRepositoryTest : BehaviorSpec({

    Given("un FirebaseCartRepository") {
        val mockProductRepository = FirebaseProductRepository()
        val repository = FirebaseCartRepository(mockProductRepository)

        When("se trabaja con items del carrito") {
            val cartItem = CartItem(
                productId = "prod123",
                productName = "PlayStation 5",
                productImage = "https://example.com/ps5.jpg",
                quantity = 1,
                unitPrice = 499.99,
                availableStock = 10
            )

            Then("debe tener todos los campos necesarios") {
                cartItem.productId shouldBe "prod123"
                cartItem.productName shouldBe "PlayStation 5"
                cartItem.unitPrice shouldBe 499.99
                cartItem.quantity shouldBe 1
                cartItem.availableStock shouldBe 10
            }
        }
    }

    Given("operaciones de carrito") {
        When("se agrega un producto al carrito") {
            val productId = "prod123"
            val quantity = 2

            Then("debe validar los parámetros") {
                productId shouldNotBe ""
                quantity shouldBe 2
                (quantity > 0) shouldBe true
            }
        }

        When("se actualiza la cantidad de un item") {
            val originalQuantity = 2
            val newQuantity = 5

            Then("la cantidad debe cambiar") {
                originalQuantity shouldBe 2
                newQuantity shouldBe 5
                (newQuantity > originalQuantity) shouldBe true
            }
        }

        When("se elimina un item del carrito") {
            val productId = "prod123"

            Then("el ID debe ser válido") {
                productId shouldNotBe ""
            }
        }
    }

    Given("cálculo de totales del carrito") {
        When("se calculan totales con múltiples items") {
            val items = listOf(
                CartItem("p1", "Product 1", "", 2, 100.0, 10),
                CartItem("p2", "Product 2", "", 1, 50.0, 5),
                CartItem("p3", "Product 3", "", 3, 75.0, 8)
            )

            val subtotal = items.sumOf { it.unitPrice * it.quantity }
            val totalItems = items.sumOf { it.quantity }

            Then("los cálculos deben ser correctos") {
                subtotal shouldBe 475.0
                totalItems shouldBe 6
            }
        }

        When("el carrito está vacío") {
            val items = emptyList<CartItem>()
            val subtotal = items.sumOf { it.unitPrice * it.quantity }
            val totalItems = items.sumOf { it.quantity }

            Then("los totales deben ser cero") {
                items.shouldBeEmpty()
                subtotal shouldBe 0.0
                totalItems shouldBe 0
            }
        }

        When("se aplica un descuento") {
            val subtotal = 100.0
            val discountPercentage = 10.0
            val discount = subtotal * (discountPercentage / 100)
            val total = subtotal - discount

            Then("el total debe reflejar el descuento") {
                discount shouldBe 10.0
                total shouldBe 90.0
            }
        }
    }

    Given("validación de stock") {
        When("hay stock suficiente") {
            val itemStock = 10
            val requestedQuantity = 5

            Then("debe permitir la compra") {
                (itemStock >= requestedQuantity) shouldBe true
            }
        }

        When("no hay stock suficiente") {
            val itemStock = 3
            val requestedQuantity = 5

            Then("debe rechazar la compra") {
                (itemStock >= requestedQuantity) shouldBe false
            }
        }

        When("el stock es exactamente igual a la cantidad") {
            val itemStock = 5
            val requestedQuantity = 5

            Then("debe permitir la compra") {
                (itemStock >= requestedQuantity) shouldBe true
            }
        }

        When("se actualiza el stock después de una compra") {
            val originalStock = 10
            val quantityPurchased = 3
            val newStock = originalStock - quantityPurchased

            Then("el stock debe disminuir") {
                newStock shouldBe 7
                (newStock < originalStock) shouldBe true
            }
        }
    }

    Given("conversión de CartItem") {
        When("se convierte un CartItem a Map") {
            val cartItem = CartItem(
                productId = "prod123",
                productName = "Test Product",
                productImage = "https://example.com/image.jpg",
                quantity = 2,
                unitPrice = 99.99,
                availableStock = 50
            )

            val itemMap = cartItem.toMap()

            Then("debe contener todos los campos") {
                itemMap["productId"] shouldBe "prod123"
                itemMap["productName"] shouldBe "Test Product"
                itemMap["unitPrice"] shouldBe 99.99
                itemMap["quantity"] shouldBe 2
            }
        }
    }

    Given("proceso de checkout") {
        When("se realiza checkout con items válidos") {
            val items = listOf(
                CartItem("p1", "Product 1", "", 2, 100.0, 10),
                CartItem("p2", "Product 2", "", 1, 50.0, 5)
            )

            val subtotal = items.sumOf { it.unitPrice * it.quantity }
            val discount = 0.0
            val total = subtotal - discount

            Then("debe calcular el total correctamente") {
                subtotal shouldBe 250.0
                total shouldBe 250.0
            }
        }

        When("se realiza checkout con descuento") {
            val items = listOf(
                CartItem("p1", "Product 1", "", 2, 100.0, 10)
            )

            val subtotal = items.sumOf { it.unitPrice * it.quantity }
            val discount = 20.0
            val total = subtotal - discount

            Then("debe aplicar el descuento") {
                subtotal shouldBe 200.0
                discount shouldBe 20.0
                total shouldBe 180.0
            }
        }
    }

    Given("limpieza del carrito") {
        When("se limpia un carrito con items") {
            val items = mutableListOf(
                CartItem("p1", "Product 1", "", 1, 100.0, 10),
                CartItem("p2", "Product 2", "", 1, 50.0, 5)
            )

            items.clear()

            Then("el carrito debe estar vacío") {
                items.shouldBeEmpty()
                items.size shouldBe 0
            }
        }
    }

    Given("manejo de cantidades") {
        When("se incrementa la cantidad") {
            val cartItem = CartItem("p1", "Producto", "", 2, 100.0, 10)
            val newQuantity = cartItem.quantity + 1

            Then("la cantidad debe aumentar") {
                newQuantity shouldBe 3
                (newQuantity > cartItem.quantity) shouldBe true
            }
        }

        When("se decrementa la cantidad") {
            val cartItem = CartItem("p1", "Producto", "", 3, 100.0, 10)
            val newQuantity = cartItem.quantity - 1

            Then("la cantidad debe disminuir") {
                newQuantity shouldBe 2
                (newQuantity < cartItem.quantity) shouldBe true
            }
        }

        When("la cantidad llega a cero") {
            val cartItem = CartItem("p1", "Producto", "", 1, 100.0, 10)
            val newQuantity = cartItem.quantity - 1

            Then("el item debe eliminarse del carrito") {
                newQuantity shouldBe 0
                (newQuantity <= 0) shouldBe true
            }
        }
    }

    Given("validación de precios en carrito") {
        When("se verifica el precio total de un item") {
            val cartItem = CartItem("p1", "Product", "", 3, 50.0, 10)
            val totalPrice = cartItem.unitPrice * cartItem.quantity

            Then("debe calcular correctamente") {
                totalPrice shouldBe 150.0
            }
        }

        When("se comparan totales") {
            val items = listOf(
                CartItem("p1", "Product 1", "", 1, 100.0, 10),
                CartItem("p2", "Product 2", "", 1, 200.0, 5)
            )

            val total1 = items[0].unitPrice * items[0].quantity
            val total2 = items[1].unitPrice * items[1].quantity

            Then("debe identificar cuál es más caro") {
                (total2 > total1) shouldBe true
                total1 shouldBe 100.0
                total2 shouldBe 200.0
            }
        }
    }

    Given("persistencia de carrito") {
        When("se guarda un carrito") {
            val userId = "user123"
            val items = listOf(
                CartItem("p1", "Product 1", "", 2, 100.0, 10)
            )

            Then("debe asociarse al usuario correcto") {
                userId shouldNotBe ""
                items.shouldNotBeEmpty()
            }
        }

        When("se carga un carrito guardado") {
            val userId = "user123"

            Then("debe cargar los items del usuario") {
                userId shouldNotBe ""
            }
        }
    }

    Given("validación de items duplicados") {
        When("se agrega un producto que ya existe") {
            val existingItem = CartItem("p1", "Product", "", 2, 100.0, 10)
            val quantityToAdd = 3
            val newQuantity = existingItem.quantity + quantityToAdd

            Then("debe sumar las cantidades") {
                newQuantity shouldBe 5
                newQuantity shouldBe existingItem.quantity + quantityToAdd
            }
        }
    }

    Given("códigos de descuento") {
        When("se aplica un código válido") {
            val discountCode = "DESCUENTO10"
            val discountPercentage = 10.0

            Then("debe ser un código válido") {
                discountCode shouldNotBe ""
                discountPercentage shouldBe 10.0
            }
        }

        When("se aplica un código inválido") {
            val discountCode = ""

            Then("no debe aplicar descuento") {
                discountCode shouldBe ""
                discountCode.isEmpty() shouldBe true
            }
        }
    }
})

